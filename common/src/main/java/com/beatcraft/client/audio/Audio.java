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

public class Audio {

    public enum Mode { STREAM, FULL, INSTANT, ERROR }

    private final int source;
    private final int[] buffers;
    private final int wallFilter;

    private Path filePath;
    private int formatId;
    private int sampleRate;
    private int channels;

    private static final int STREAM_BUFFER_SIZE = 64 * 1024;
    private static final int NUM_STREAM_BUFFERS = 4;
    private static final int QUEUE_CAPACITY = 16;
    private static final float SEEK_THRESHOLD = 0.25f;

    private final BlockingQueue<ByteBuffer> pcmQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicBoolean fullReady = new AtomicBoolean(false);
    private final AtomicBoolean shouldStopDecoder = new AtomicBoolean(false);
    private final AtomicBoolean streamEOF = new AtomicBoolean(false);

    private ByteBuffer fullData;
    public int fullBuffer = 0;

    private boolean loaded = false;
    private boolean playing = false;
    private boolean paused = false;
    private volatile boolean closed = false;

    private float lastSeekTarget = -1f;
    private volatile float targetSeekPosition = -1f;

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

    // ===== FULL blocking decode =====
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

    // ===== STREAM decode =====
    private void startStreamDecodeAsync() {
        shouldStopDecoder.set(false);
        streamEOF.set(false);

        CompletableFuture.runAsync(() -> {
            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream stream = new JOrbisAudioStream(is)) {

                float currentSeekTarget = targetSeekPosition;

                // Fast-forward if seeking
                if (currentSeekTarget > 0) {
                    int bytesToSkip = (int)(currentSeekTarget * sampleRate * channels * 2);
                    int skipped = 0;

                    while (skipped < bytesToSkip && !shouldStopDecoder.get() && !closed) {
                        int toRead = Math.min(STREAM_BUFFER_SIZE, bytesToSkip - skipped);
                        ByteBuffer pcm = stream.read(toRead);
                        if (!pcm.hasRemaining()) break;
                        skipped += pcm.remaining();
                    }

                    targetSeekPosition = -1f;
                }

                while (!shouldStopDecoder.get() && !closed) {
                    ByteBuffer pcm = stream.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) {
                        streamEOF.set(true);
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

    private void startInstantDecodeAsync() {
        shouldStopDecoder.set(false);
        streamEOF.set(false);

        CompletableFuture.runAsync(() -> {
            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream s = new JOrbisAudioStream(is);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                while (!shouldStopDecoder.get() && !closed) {
                    ByteBuffer pcm = s.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) {
                        streamEOF.set(true);
                        break;
                    }

                    // Feed both the streaming queue and the full buffer
                    byte[] tmp = new byte[pcm.remaining()];
                    pcm.get(tmp);
                    out.write(tmp);

                    // Also queue for immediate streaming
                    ByteBuffer copy = BufferUtils.createByteBuffer(tmp.length).put(tmp).flip();
                    try {
                        pcmQueue.put(copy);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
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

    // ===== Stream helpers =====
    private void fillStreamBuffers() {
        if (closed) return;

        // Unqueue processed buffers and refill them
        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = AL10.alSourceUnqueueBuffers(source);
            ByteBuffer pcm = pcmQueue.poll();
            if (pcm != null) {
                AL10.alBufferData(buf, formatId, pcm, sampleRate);
                AL10.alSourceQueueBuffers(source, buf);
            }
        }

        // Queue initial buffers if not loaded yet
        if (!loaded) {
            for (int i = 0; i < NUM_STREAM_BUFFERS; i++) {
                ByteBuffer pcm = pcmQueue.poll();
                if (pcm == null) break;
                AL10.alBufferData(buffers[i], formatId, pcm, sampleRate);
                AL10.alSourceQueueBuffers(source, buffers[i]);
            }

            // Mark as loaded once we have at least one buffer queued
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            if (queued > 0) {
                loaded = true;
            }
        }

        // Restart playback if it stopped but should be playing
        int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        if (queued > 0 && state != AL10.AL_PLAYING && playing && !paused) {
            AL10.alSourcePlay(source);
        }
    }

    // ===== Public API =====
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

        // Only seek if difference is significant
        if (Math.abs(seconds - lastSeekTarget) < SEEK_THRESHOLD) {
            return;
        }
        lastSeekTarget = seconds;

        if (mode == Mode.FULL) {
            // Direct seek in full buffer
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
        } else if (mode == Mode.INSTANT && fullReady.get()) {
            // Can seek directly once full buffer is ready
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
        } else {
            // STREAM mode or INSTANT before full buffer ready
            float currentPos = loaded ? AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET) : 0f;

            if (seconds < currentPos || !loaded) {
                // Seeking backwards or initial seek - must restart and fast-forward
                shouldStopDecoder.set(true);
                targetSeekPosition = seconds;

                AL10.alSourceStop(source);

                // Unqueue all buffers
                int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
                while (queued-- > 0) {
                    AL10.alSourceUnqueueBuffers(source);
                }

                pcmQueue.clear();
                loaded = false;

                // Restart decode with seek target
                if (mode == Mode.STREAM) {
                    startStreamDecodeAsync();
                } else if (mode == Mode.INSTANT) {
                    startInstantDecodeAsync();
                }
            } else {
                // Seeking forward - fast-forward by draining queue
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
        Beatcraft.LOGGER.info("closed: {}, paused: {}, loaded: {}", closed, paused, loaded);
        if (closed) return;

        // If resuming from pause, use resume logic
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

        // STREAM/INSTANT streaming - always fill buffers when not using full buffer
        if (mode == Mode.STREAM || (mode == Mode.INSTANT && !fullReady.get())) {
            fillStreamBuffers();
        }

        // Switch INSTANT to FULL buffer once ready
        if (mode == Mode.INSTANT && fullReady.get() && fullData != null && fullBuffer == 0) {
            boolean wasPlaying = (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING);
            float currentTime = AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET);

            fullBuffer = AL10.alGenBuffers();
            AL10.alBufferData(fullBuffer, formatId, fullData, sampleRate);

            AL10.alSourceStop(source);
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) {
                AL10.alSourceUnqueueBuffers(source);
            }
            pcmQueue.clear();

            AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, currentTime);

            if (wasPlaying && playing && !paused) {
                AL10.alSourcePlay(source);
            }
        }

        // Pause if Minecraft paused or controller stopped
        if (Minecraft.getInstance().isPaused() || !controller.isPlaying()) {
            if (!paused) pause();
            return;
        } else if (paused) {
            lastSeekTarget = -2;
            seek(currentSeconds);
            play();
        }

        // Auto-play when loaded
        if (!playing && loaded) play();
    }

    public void close() {
        if (closed) return;
        closed = true;
        shouldStopDecoder.set(true);
        AL10.alSourceStop(source);
        AL10.alDeleteSources(source);
        if (fullBuffer != 0) AL10.alDeleteBuffers(fullBuffer);
        if (buffers.length > 0) AL10.alDeleteBuffers(buffers);
        EXTEfx.alDeleteFilters(wallFilter);
    }
}