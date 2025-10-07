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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Audio {

    public enum Mode {
        STREAM,
        INSTANT,
        FULL,
        FULL_ASYNC,
        ERROR,
    }

    public final int[] buffer;
    public final int source;
    public int fullBuffer = 0;
    public boolean loaded;
    public Mode mode;
    public boolean closed = false;

    public Path filePath;
    public int formatId;
    public int sampleRate;
    public int channels;
    public int sampleSizeInBits;

    private boolean playing;
    private boolean paused;

    private final int wallFilter;

    private static final int STREAM_BUFFER_SIZE = 64 * 1024;
    private static final int CACHE_BUFFER_COUNT = 6;
    private static final int FORWARD_SEEK_CACHE_BUFFERS = 3;

    // Instant streaming state
    private Deque<byte[]> audioCache;
    private Queue<Runnable> pendingALOperations;
    private long streamPosition;
    private long cacheStartPosition;
    private boolean transitionToFull;
    private Audio fullAudio;
    private CompletableFuture<Void> backgroundTask;

    public Audio(int[] buffer, int source, Mode mode) {
        this(buffer, source, true, mode);
    }

    public Audio(int[] buffer, int source, boolean loaded, Mode mode) {
        this.buffer = buffer;
        this.source = source;
        this.loaded = loaded;
        this.mode = mode;

        wallFilter = EXTEfx.alGenFilters();

        EXTEfx.alFilteri(wallFilter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        EXTEfx.alFilterf(wallFilter, EXTEfx.AL_LOWPASS_GAIN, 1.0f);
        EXTEfx.alFilterf(wallFilter, EXTEfx.AL_LOWPASS_GAINHF, 0.1f);

        if (mode == Mode.INSTANT) {
            audioCache = new ArrayDeque<>();
            pendingALOperations = new ConcurrentLinkedQueue<>();
            streamPosition = 0;
            cacheStartPosition = 0;
            transitionToFull = false;
        }
    }

    private static Audio erroredAudio(String path) {
        var a = new Audio(new int[0], 0, false, Mode.ERROR);
        a.closed = true;

        Beatcraft.LOGGER.error(
            """
                
                ///
                /// Song Failed to load: {}
                /// This is most likely due to the song's encoding.
                /// Check here for how to fix: https://github.com/Swifter1243/BeatCraft/wiki/Troubleshooting#encoding-issues
                ///
                """,
            path
        );

        return a;
    }

    private static int getFormatID(AudioFormat format) {
        AudioFormat.Encoding encoding = format.getEncoding();
        var channels = format.getChannels();
        var sampleSizeInBits = format.getSampleSizeInBits();

        if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED) || encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (channels == 1) {
                if (sampleSizeInBits == 8) return AL10.AL_FORMAT_MONO8;
                if (sampleSizeInBits == 16) return AL10.AL_FORMAT_MONO16;
            } else if (channels == 2) {
                if (sampleSizeInBits == 8) return AL10.AL_FORMAT_STEREO8;
                if (sampleSizeInBits == 16) return AL10.AL_FORMAT_STEREO16;
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + format);
    }

    public static Audio loadFromFile(String path, Mode mode) {
        Path filePath = Path.of(path);
        int formatId;
        int sampleRate;

        AudioFormat format;
        try (InputStream probeIn = Files.newInputStream(filePath);
             JOrbisAudioStream probeStream = new JOrbisAudioStream(probeIn)) {

            format = probeStream.getFormat();

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to probe audio {}: {}", path, e.getMessage());
            return erroredAudio(path);
        }

        try {
            formatId = getFormatID(format);
            sampleRate = (int) format.getSampleRate();
        } catch (IllegalArgumentException ex) {
            Beatcraft.LOGGER.error("Unsupported audio format in {}: {}", path, ex.getMessage());
            return erroredAudio(path);
        }

        var source = AL10.alGenSources();
        var streamBuffers = new int[]{0, 0, 0, 0};
        AL10.alGenBuffers(streamBuffers);

        Audio audio = new Audio(streamBuffers, source, false, mode);
        audio.filePath = filePath;
        audio.formatId = formatId;
        audio.sampleRate = sampleRate;
        audio.channels = format.getChannels();
        audio.sampleSizeInBits = format.getSampleSizeInBits();

        switch (mode) {
            case FULL -> {
                audio.loadFull();
            }
            case FULL_ASYNC -> {
                CompletableFuture.runAsync(audio::loadFull);
            }
            case INSTANT -> {
                audio.loadInstant();
            }
            case STREAM -> {
                audio.loadStream();
            }
        }


        return audio;

    }

    private byte[] decodeFile() throws IOException {
        try (InputStream is = Files.newInputStream(filePath);
             JOrbisAudioStream s = new JOrbisAudioStream(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ByteBuffer read;
            byte[] tmp = new byte[STREAM_BUFFER_SIZE];
            while (true) {
                read = s.read(STREAM_BUFFER_SIZE);
                if (!read.hasRemaining()) break;
                int len = read.remaining();
                if (len > tmp.length) tmp = new byte[len];
                read.get(tmp, 0, len);
                out.write(tmp, 0, len);
            }
            return out.toByteArray();
        }
    }

    private void loadFull() {
        try {
            var pcm = decodeFile();
            if (closed) return;

            fullBuffer = AL10.alGenBuffers();
            var b = BufferUtils.createByteBuffer(pcm.length).put(pcm);
            b.flip();
            AL10.alBufferData(fullBuffer, formatId, b, sampleRate);
            AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);

            loaded = true;

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to fully load audio", e);
        }
    }

    private void loadInstant() {
        try (InputStream is = Files.newInputStream(filePath);
             JOrbisAudioStream stream = new JOrbisAudioStream(is)) {

            byte[] tmp = new byte[STREAM_BUFFER_SIZE];

            // Load first 2 buffers
            for (int i = 0; i < 2; i++) {
                ByteBuffer read = stream.read(STREAM_BUFFER_SIZE);
                if (!read.hasRemaining()) break;

                byte[] chunk = new byte[read.remaining()];
                read.get(chunk);
                audioCache.add(chunk);
            }

            if (audioCache.isEmpty()) {
                Beatcraft.LOGGER.error("Failed to load initial audio buffers for instant mode");
                return;
            }

            // Queue first buffer
            byte[] firstChunk = audioCache.peekFirst();
            ByteBuffer bb = BufferUtils.createByteBuffer(firstChunk.length).put(firstChunk);
            bb.flip();
            AL10.alBufferData(buffer[0], formatId, bb, sampleRate);
            AL10.alSourceQueueBuffers(source, buffer[0]);

            streamPosition = firstChunk.length;
            loaded = true;

            // Start background streaming and full load task
            backgroundTask = CompletableFuture.runAsync(() -> {
                streamAndLoadFull(stream);
            });

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to start instant audio loading", e);
        }
    }

    private void streamAndLoadFull(JOrbisAudioStream stream) {
        try {
            // Continue streaming
            byte[] tmp = new byte[STREAM_BUFFER_SIZE];
            while (!closed && !transitionToFull) {
                ByteBuffer read = stream.read(STREAM_BUFFER_SIZE);
                if (!read.hasRemaining()) break;

                byte[] chunk = new byte[read.remaining()];
                read.get(chunk);

                synchronized (audioCache) {
                    audioCache.add(chunk);

                    // Maintain cache size limit
                    while (audioCache.size() > CACHE_BUFFER_COUNT) {
                        audioCache.removeFirst();
                        cacheStartPosition += audioCache.peekFirst().length;
                    }
                }

                // Small sleep to avoid busy waiting
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Audio streaming error", e);
        }

        // Start full load in parallel
        if (!closed) {
            try {
                var pcm = decodeFile();
                if (closed) return;

                Audio fullAudioInstance = new Audio(new int[0], AL10.alGenSources(), false, Mode.FULL);
                fullAudioInstance.filePath = filePath;
                fullAudioInstance.formatId = formatId;
                fullAudioInstance.sampleRate = sampleRate;
                fullAudioInstance.channels = channels;
                fullAudioInstance.sampleSizeInBits = sampleSizeInBits;

                int fullBuf = AL10.alGenBuffers();
                var b = BufferUtils.createByteBuffer(pcm.length).put(pcm);
                b.flip();
                AL10.alBufferData(fullBuf, formatId, b, sampleRate);

                fullAudioInstance.fullBuffer = fullBuf;
                fullAudioInstance.loaded = true;

                // Queue transition
                this.fullAudio = fullAudioInstance;
                pendingALOperations.add(this::transitionToFullMode);

            } catch (IOException e) {
                Beatcraft.LOGGER.error("Audio full load error", e);
            }
        }
    }

    private void transitionToFullMode() {
        if (closed || fullAudio == null) return;

        float currentTime = AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET);
        boolean wasPlaying = isPlaying();

        // Stop current playback
        AL10.alSourceStop(source);
        AL10.alSourceUnqueueBuffers(source, buffer);

        // Transition to full audio properties
        fullBuffer = fullAudio.fullBuffer;
        AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);

        // Clean up streaming resources
        if (buffer.length > 0) {
            AL10.alDeleteBuffers(buffer);
        }
        synchronized (audioCache) {
            audioCache.clear();
        }

        // Restore playback state
        AL10.alSourcef(source, AL11.AL_SEC_OFFSET, currentTime);
        if (wasPlaying) {
            AL10.alSourcePlay(source);
        }

        // Clean up temporary full audio instance
        if (fullAudio.source != 0) {
            AL10.alDeleteSources(fullAudio.source);
        }
        fullAudio = null;

        transitionToFull = true;
        backgroundTask = null;
    }

    private void loadStream() {

    }

    public void play() {
        if (closed || !loaded) return;
        playing = true;
        paused = false;
        AL10.alSourcePlay(source);
    }

    public void pause() {
        if (closed || !playing || !loaded) return;
        AL10.alSourcePause(source);
        paused = true;
    }

    public void stop() {
        if (closed) return;
        AL10.alSourceStop(source);
        playing = false;
        paused = false;
    }

    public void close() {
        if (closed) return;
        closed = true;
        AudioController.remove(this);

        if (backgroundTask != null) {
            backgroundTask.cancel(true);
        }

        if (fullBuffer != 0) {
            AL10.alDeleteBuffers(fullBuffer);
        }
        if (buffer.length > 0) {
            AL10.alDeleteBuffers(buffer);
        }

        EXTEfx.alDeleteFilters(wallFilter);
        AL10.alSourceStop(source);
        AL10.alDeleteSources(source);
    }

    public void seek(float seconds) {
        if (closed) return;

        if (mode == Mode.INSTANT && !transitionToFull) {
            // Handle seeking during streaming
            long targetPosition = (long) (seconds * sampleRate * channels * (sampleSizeInBits / 8));
            long cacheDelta = targetPosition - cacheStartPosition;

            // Check if target is within cache
            if (cacheDelta >= 0 && cacheDelta < audioCache.size() * STREAM_BUFFER_SIZE) {
                // Can seek within cache - just adjust playback position
                // This is a simplified approach; full implementation would reconstruct buffers
                AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
            } else {
                // Out of cache range - reset stream
                // Queue a stream reset operation
                pendingALOperations.add(() -> resetStream(seconds));
            }
        } else if (fullBuffer != 0) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
        }
    }

    private void resetStream(float seconds) {
        // Stop and clear current buffers
        AL10.alSourceStop(source);
        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            AL10.alSourceUnqueueBuffers(source, buffer);
        }

        // Reset cache and position
        synchronized (audioCache) {
            audioCache.clear();
        }

        // Restart streaming from new position
        // This would require reopening the stream and skipping to position
        // Simplified for now - actual implementation would decode from target position
    }

    public void setSpeed(float speed) {
        AL10.alSourcef(source, AL10.AL_PITCH, speed);
    }

    public void setVolume(float volume) {
        AL10.alSourcef(source, AL10.AL_GAIN, volume);
    }

    private boolean wasInWall = false;
    private double lastSeconds = 0;

    public void update(float beat, double dt, BeatmapController controller) {
        if (!isLoaded()) {
            return;
        }

        // Execute pending OpenAL operations from background thread
        if (mode == Mode.INSTANT && !transitionToFull) {
            Runnable op;
            while ((op = pendingALOperations.poll()) != null) {
                op.run();
            }

            // Handle buffer refilling for streaming
            if (!transitionToFull) {
                int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                if (processed > 0) {
                    AL10.alSourceUnqueueBuffers(source, buffer);

                    synchronized (audioCache) {
                        if (!audioCache.isEmpty()) {
                            byte[] nextChunk = audioCache.peekFirst();
                            if (nextChunk != null) {
                                ByteBuffer bb = BufferUtils.createByteBuffer(nextChunk.length).put(nextChunk);
                                bb.flip();
                                AL10.alBufferData(buffer[0], formatId, bb, sampleRate);
                                AL10.alSourceQueueBuffers(source, buffer[0]);
                            }
                        }
                    }
                }
            }
        }

        if (Minecraft.getInstance().isPaused() || !controller.isPlaying()) {
            pause();
        } else {
            if (Math.abs(controller.currentSeconds - lastSeconds) > 0.2) {
                seek(controller.currentSeconds);
            }
            lastSeconds = controller.currentSeconds;

            if (!isPlaying()) {
                play();
            }

            if (controller.isInWall && !wasInWall) {
                applyFx();
            } else if (wasInWall && !controller.isInWall) {
                clearFx();
            }
            wasInWall = controller.isInWall;
        }
    }

    private void applyFx() {
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, wallFilter);
    }

    private void clearFx() {
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, 0);
    }

    public boolean isOk() {
        return mode != Mode.ERROR;
    }

    public boolean isLoaded() { return loaded && !closed; }
    public boolean isPlaying() { return playing && !paused && !closed; }
    public boolean isPaused() { return paused && !closed; }
    public boolean isClosed() { return closed; }

}