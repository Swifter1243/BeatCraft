package com.beatcraft.client.audio;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.JOrbisAudioStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class Audio {

    public enum Mode { STREAM, FULL, INSTANT, ERROR }

    private final int source;
    private final int[] buffers;
    private final int wallFilter;
    private boolean wasInWall;

    private Path filePath;
    private int formatId;
    private int sampleRate;
    private int channels;

    private static final int STREAM_BUFFER_SIZE = 64 * 1024;
    private static final int NUM_STREAM_BUFFERS = 4;
    private static final int QUEUE_CAPACITY = 16;
    private static final float SEEK_THRESHOLD = 0.25f;

    private final BlockingQueue<ByteBuffer> pcmQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final BlockingQueue<ByteBuffer> seekQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicBoolean fullReady = new AtomicBoolean(false);
    private final AtomicBoolean shouldStopDecoder = new AtomicBoolean(false);
    private final AtomicBoolean shouldStopSeekDecoder = new AtomicBoolean(false);
    private final AtomicBoolean usingSeekStream = new AtomicBoolean(false);

    private CompletableFuture<?> decodeFuture;
    private CompletableFuture<?> seekDecodeFuture;

    private ByteBuffer fullData;
    public int fullBuffer = 0;

    private boolean loaded = false;
    private boolean playing = false;
    private volatile boolean paused = false;
    private volatile boolean closed = false;

    private float lastSeekTarget = -1f;
    private volatile float targetSeekPosition = -1f;
    private volatile float currentStreamPosition = 0f;

    private final Mode mode;

    private Audio(int source, int[] buffers, Mode mode) {
        this.source = source;
        this.buffers = buffers;
        this.mode = mode;

        wallFilter = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(wallFilter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        EXTEfx.alFilterf(wallFilter, EXTEfx.AL_LOWPASS_GAIN, 1.0f);
        EXTEfx.alFilterf(wallFilter, EXTEfx.AL_LOWPASS_GAINHF, 0.1f);
    }

    public Audio reload() {
        close();
        return loadFromFile(this.filePath.toString(), this.mode);
    }

    public static Audio loadFromFile(String path, Mode mode) {
        Path file = Path.of(path);

        AudioFormat format;
        try (InputStream probeIn = Files.newInputStream(file);
            JOrbisAudioStream probe = new JOrbisAudioStream(probeIn)) {
            format = probe.getFormat();
        } catch (Exception e) {
            Beatcraft.LOGGER.error("Failed to probe audio {}", path, e);
            return errorAudio();
        }

        int formatId;
        try {
            formatId = getFormatID(format);
        } catch (IllegalArgumentException e) {
            Beatcraft.LOGGER.error("Unsupported audio format {}", path, e);
            return errorAudio();
        }

        int source = AL10.alGenSources();
        int[] buffers = (mode == Mode.STREAM || mode == Mode.INSTANT) ? new int[NUM_STREAM_BUFFERS] : new int[0];
        if (mode == Mode.STREAM || mode == Mode.INSTANT) AL10.alGenBuffers(buffers);

        Audio audio = new Audio(source, buffers, mode);
        audio.filePath = file;
        audio.formatId = formatId;
        audio.sampleRate = (int) format.getSampleRate();
        audio.channels = format.getChannels();

        switch (mode) {
            case FULL -> audio.loadFullBlocking();
            case STREAM -> audio.startStreamDecodeAsync();
            case INSTANT -> audio.startInstantDecodeAsync();
        }

        return audio;
    }

    private static Audio errorAudio() {
        Audio a = new Audio(0, new int[0], Mode.ERROR);
        a.closed = true;
        return a;
    }

    private static int getFormatID(AudioFormat format) {
        if (format.getChannels() == 1 && format.getSampleSizeInBits() == 16) return AL10.AL_FORMAT_MONO16;
        if (format.getChannels() == 2 && format.getSampleSizeInBits() == 16) return AL10.AL_FORMAT_STEREO16;
        throw new IllegalArgumentException();
    }

    private void loadFullBlocking() {
        try (InputStream is = Files.newInputStream(filePath);
             JOrbisAudioStream s = new JOrbisAudioStream(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            while (true) {
                ByteBuffer pcm = s.read(STREAM_BUFFER_SIZE);
                if (!pcm.hasRemaining()) break;

                byte[] tmp = new byte[pcm.remaining()];
                pcm.get(tmp);
                out.write(tmp);
            }

            byte[] pcmBytes = out.toByteArray();
            fullData = BufferUtils.createByteBuffer(pcmBytes.length).put(pcmBytes).flip();
            fullBuffer = AL10.alGenBuffers();
            AL10.alBufferData(fullBuffer, formatId, fullData, sampleRate);
            AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);
            loaded = true;

        } catch (Exception e) {
            Beatcraft.LOGGER.error("Failed to fully load audio", e);
        }
    }

    private void startStreamDecodeAsync() {
        shouldStopDecoder.set(false);

        decodeFuture = CompletableFuture.runAsync(() -> {
            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream stream = new JOrbisAudioStream(is)) {

                float currentSeekTarget = targetSeekPosition;

                if (currentSeekTarget > 0) {
                    int bytesToSkip = (int)(currentSeekTarget * sampleRate * channels * 2);
                    int skipped = 0;

                    while (skipped < bytesToSkip && !shouldStopDecoder.get() && !closed) {
                        int toRead = Math.min(STREAM_BUFFER_SIZE, bytesToSkip - skipped);
                        ByteBuffer pcm = stream.read(toRead);
                        if (!pcm.hasRemaining()) break;
                        skipped += pcm.remaining();
                    }

                    currentStreamPosition = currentSeekTarget;
                    targetSeekPosition = -1f;
                }

                while (!shouldStopDecoder.get() && !closed) {
                    ByteBuffer pcm = stream.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) {
                        break;
                    }
                    ByteBuffer copy = BufferUtils.createByteBuffer(pcm.remaining()).put(pcm).flip();

                    try {
                        pcmQueue.put(copy);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

            } catch (Exception e) {
                if (!closed) {
                    Beatcraft.LOGGER.error("STREAM decode failed", e);
                }
            }
        });
    }

    private void waitWhilePaused() {
        while (paused && !shouldStopDecoder.get() && !closed) {
            LockSupport.parkNanos(5_000_000);
        }
    }


    private void startInstantDecodeAsync() {
        shouldStopDecoder.set(false);

        decodeFuture = CompletableFuture.runAsync(() -> {
            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream s = new JOrbisAudioStream(is);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                while (!shouldStopDecoder.get() && !closed) {
                    waitWhilePaused();
                    ByteBuffer pcm = s.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) {
                        break;
                    }

                    byte[] tmp = new byte[pcm.remaining()];
                    pcm.get(tmp);
                    out.write(tmp);

                    if (!usingSeekStream.get()) {
                        ByteBuffer copy = BufferUtils.createByteBuffer(tmp.length).put(tmp).flip();
                        try {
                            pcmQueue.put(copy);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                byte[] pcmBytes = out.toByteArray();
                fullData = BufferUtils.createByteBuffer(pcmBytes.length).put(pcmBytes).flip();
                fullReady.set(true);

            } catch (Exception e) {
                if (!closed) {
                    Beatcraft.LOGGER.error("FULL decode for INSTANT failed", e);
                }
            }
        });
    }

    private void startSeekStreamDecodeAsync(float seekSeconds) {
        shouldStopSeekDecoder.set(false);
        usingSeekStream.set(true);
        currentStreamPosition = seekSeconds;

        seekDecodeFuture = CompletableFuture.runAsync(() -> {
            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream stream = new JOrbisAudioStream(is)) {

                int bytesToSkip = (int)(seekSeconds * sampleRate * channels * 2);
                int skipped = 0;

                while (skipped < bytesToSkip && !shouldStopSeekDecoder.get() && !closed) {
                    int toRead = Math.min(STREAM_BUFFER_SIZE, bytesToSkip - skipped);
                    ByteBuffer pcm = stream.read(toRead);
                    if (!pcm.hasRemaining()) break;
                    skipped += pcm.remaining();
                }

                while (!shouldStopSeekDecoder.get() && !closed && !fullReady.get()) {
                    ByteBuffer pcm = stream.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) break;

                    ByteBuffer copy = BufferUtils.createByteBuffer(pcm.remaining()).put(pcm).flip();

                    try {
                        seekQueue.put(copy);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

            } catch (Exception e) {
                if (!closed) {
                    Beatcraft.LOGGER.error("Seek stream decode failed", e);
                }
                usingSeekStream.set(false);
            }
        });
    }

    private void fillStreamBuffers() {
        if (closed) return;

        BlockingQueue<ByteBuffer> activeQueue = usingSeekStream.get() ? seekQueue : pcmQueue;

        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = AL10.alSourceUnqueueBuffers(source);
            ByteBuffer pcm = activeQueue.poll();
            if (pcm != null) {
                AL10.alBufferData(buf, formatId, pcm, sampleRate);
                AL10.alSourceQueueBuffers(source, buf);
            }
        }

        if (!loaded) {
            for (int i = 0; i < NUM_STREAM_BUFFERS; i++) {
                ByteBuffer pcm = activeQueue.poll();
                if (pcm == null) break;
                AL10.alBufferData(buffers[i], formatId, pcm, sampleRate);
                AL10.alSourceQueueBuffers(source, buffers[i]);
            }

            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            if (queued > 0) {
                loaded = true;
            }
        }

        int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (queued > 0 && state != AL10.AL_PLAYING && playing && !paused) {
            AL10.alSourcePlay(source);
        }
    }

    public boolean isOk() { return mode != Mode.ERROR && !closed; }
    public boolean isPlaying() { return playing && !paused && !closed; }
    public boolean isPaused() { return paused && !closed; }

    public void setVolume(float volume) {
        if (!closed) AL10.alSourcef(source, AL10.AL_GAIN, Math.max(0f, volume));
    }

    public void setSpeed(float speed) {
        if (!closed) AL10.alSourcef(source, AL10.AL_PITCH, speed);
    }

    public void seek(float seconds) {
        if (closed) return;

        if (Math.abs(seconds - lastSeekTarget) < SEEK_THRESHOLD) {
            return;
        }
        lastSeekTarget = seconds;

        if (mode == Mode.FULL) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
        } else if (mode == Mode.INSTANT && fullReady.get()) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
        } else if (mode == Mode.INSTANT && !fullReady.get()) {
            if (seconds < currentStreamPosition || Math.abs(seconds - currentStreamPosition) > SEEK_THRESHOLD) {
                shouldStopSeekDecoder.set(true);

                AL10.alSourceStop(source);

                int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
                while (queued-- > 0) {
                    AL10.alSourceUnqueueBuffers(source);
                }

                seekQueue.clear();
                loaded = false;

                startSeekStreamDecodeAsync(seconds);
            } else {
                BlockingQueue<ByteBuffer> activeQueue = usingSeekStream.get() ? seekQueue : pcmQueue;
                float delta = seconds - currentStreamPosition;
                int bytesToDrain = (int)(delta * sampleRate * channels * 2);
                int drained = 0;

                while (drained < bytesToDrain) {
                    ByteBuffer pcm = activeQueue.poll();
                    if (pcm == null) break;
                    drained += pcm.remaining();
                }

                currentStreamPosition = seconds;
            }
        } else {
            float currentPos = AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET);

            if (seconds < currentPos) {
                shouldStopDecoder.set(true);
                targetSeekPosition = seconds;

                AL10.alSourceStop(source);

                int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
                while (queued-- > 0) {
                    AL10.alSourceUnqueueBuffers(source);
                }

                pcmQueue.clear();
                loaded = false;

                startStreamDecodeAsync();
            } else {
                float delta = seconds - currentPos;
                int bytesToDrain = (int)(delta * sampleRate * channels * 2);
                int drained = 0;

                while (drained < bytesToDrain) {
                    ByteBuffer pcm = pcmQueue.poll();
                    if (pcm == null) break;
                    drained += pcm.remaining();
                }
            }
        }
    }

    public void play() {
        if (closed) return;

        if (paused) {
            paused = false;
            if (loaded) {
                int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
                if (state == AL10.AL_PAUSED) {
                    AL10.alSourcePlay(source);
                }
            }
            playing = true;
            return;
        }

        playing = true;

        if (loaded) {
            int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_PLAYING) {
                AL10.alSourcePlay(source);
            }
        }
    }

    public void pause() {
        if (closed || !playing) return;
        paused = true;
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_PLAYING) {
            AL10.alSourcePause(source);
        }
    }

    public void update(float beat, float currentSeconds, BeatmapController controller) {
        if (closed || mode == Mode.ERROR) return;

        if (mode == Mode.STREAM || (mode == Mode.INSTANT && !fullReady.get())) {
            fillStreamBuffers();
        }

        if (mode == Mode.INSTANT && fullReady.get() && fullData != null && fullBuffer == 0) {
            boolean wasPlaying = (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING);

            shouldStopSeekDecoder.set(true);
            usingSeekStream.set(false);

            fullBuffer = AL10.alGenBuffers();
            AL10.alBufferData(fullBuffer, formatId, fullData, sampleRate);

            AL10.alSourceStop(source);
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) {
                AL10.alSourceUnqueueBuffers(source);
            }
            pcmQueue.clear();
            seekQueue.clear();

            AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, currentSeconds);

            if (wasPlaying && playing && !paused) {
                AL10.alSourcePlay(source);
            }
        }

        if (Minecraft.getInstance().isPaused() || !controller.isPlaying()) {
            if (!paused) pause();
            return;
        } else {
            if (paused) {
                play();
            }
            if (controller.isInWall && !wasInWall) {
                applyFx();
            } else if (wasInWall && !controller.isInWall) {
                clearFx();
            }
            wasInWall = controller.isInWall;
        }



        if (!playing && loaded) play();
    }

    public void applyFx() {
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, wallFilter);
    }

    public void clearFx() {
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, 0);
    }

    public void close() {
        if (closed) return;
        closed = true;
        shouldStopDecoder.set(true);
        shouldStopSeekDecoder.set(true);
        if (decodeFuture != null) {
            decodeFuture.cancel(true);
            decodeFuture = null;
        }
        if (seekDecodeFuture != null) {
            seekDecodeFuture.cancel(true);
            seekDecodeFuture = null;
        }
        AL10.alSourceStop(source);
        AL10.alDeleteSources(source);
        if (fullBuffer != 0) AL10.alDeleteBuffers(fullBuffer);
        if (buffers.length > 0) AL10.alDeleteBuffers(buffers);
        EXTEfx.alDeleteFilters(wallFilter);
    }
}