package com.beatcraft.client.audio;

import com.beatcraft.Beatcraft;
import net.minecraft.client.sounds.JOrbisAudioStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Audio {

    public enum Mode {
        STREAM,     // Stream-only
        FULL,       // Full load before playing
        INSTANT,    // Stream immediately, build full buffer in the background (single-pass cache)
        ERR
    }

    public final int[] buffer;
    public final int source;
    private int fullBuffer = -1;

    public int activeStreamBuffer = 0;
    private boolean loaded = false;   // audio is successfully loaded and can be played
    private boolean playing = false;  // whether the audio is actively playing, or paused while not at time=0
    private boolean paused = false;   // explicit pause state
    private boolean closed = false;   // if true, all functions should no-op
    private final Mode mode;
    private int phase = 0;

    private volatile boolean streaming = false;
    private CompletableFuture<Void> streamFuture = null;

    private int formatId;
    private int sampleRate;
    private int channels;
    private int sampleSizeInBits;
    private Path filePath;

    private static final int STREAM_BUFFER_SIZE = 64 * 1024;

    /// Calling the constructor directly assumes the audio has already been loaded.
    public Audio(int[] buffer, int source, Mode mode) {
        this(buffer, source, true, mode);
    }

    public Audio(int[] buffer, int source, boolean loaded, Mode mode) {
        this.buffer = buffer;
        this.source = source;
        this.loaded = loaded;
        this.mode = mode;
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

    private static Audio erroredAudio(String path) {
        var a = new Audio(new int[0], 0, false, Mode.ERR);
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
            case STREAM -> audio.startStreamMode();
            case FULL -> audio.startFullMode();
            case INSTANT -> audio.startInstantMode();
            default -> {
                return erroredAudio(path);
            }
        }

        return audio;
    }

    private void startStreamMode() {
        phase = 0;
        loaded = true;
        streaming = true;

        ensureStreamBuffersAllocated();

        streamFuture = CompletableFuture.runAsync(this::streamLoopSinglePass);
        streamFuture.thenRun(() -> { if (!closed) phase = 1; });
    }

    private void startFullMode() {
        phase = 0;
        loaded = false;
        streaming = false;

        CompletableFuture.runAsync(() -> {
            try {
                byte[] pcm = decodeWholeFileToByteArray();
                if (closed) return;

                fullBuffer = AL10.alGenBuffers();
                ByteBuffer b = BufferUtils.createByteBuffer(pcm.length).put(pcm);
                b.flip();
                AL10.alBufferData(fullBuffer, formatId, b, sampleRate);

                AL10.alSourcei(source, AL10.AL_BUFFER, fullBuffer);

                loaded = true;
                phase = 1;
            } catch (IOException ex) {
                Beatcraft.LOGGER.error("Failed to fully load audio: {}", ex.getMessage());
            }
        });
    }

    private void startInstantMode() {
        phase = 0;
        loaded = true;
        streaming = true;

        ensureStreamBuffersAllocated();

        ByteArrayOutputStream cache = new ByteArrayOutputStream();

        streamFuture = CompletableFuture.runAsync(() -> streamLoopSinglePass(true, cache));

        streamFuture.thenRunAsync(() -> {
            if (closed) return;
            try {
                byte[] pcm = cache.toByteArray();
                if (pcm.length == 0) {
                    Beatcraft.LOGGER.error("Instant-mode: cached PCM is empty for {}", filePath);
                    return;
                }

                int fb = AL10.alGenBuffers();
                ByteBuffer b = BufferUtils.createByteBuffer(pcm.length).put(pcm);
                b.flip();
                AL10.alBufferData(fb, formatId, b, sampleRate);

                float currentSec = AL11.alGetSourcef(source, AL11.AL_SEC_OFFSET);

                AL10.alSourceStop(source);

                int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
                while (queued-- > 0) AL10.alSourceUnqueueBuffers(source);

                AL10.alSourcei(source, AL10.AL_BUFFER, fb);
                AL11.alSourcef(source, AL11.AL_SEC_OFFSET, currentSec);

                deleteStreamBuffers();

                fullBuffer = fb;
                phase = 3;
                loaded = true;

                if (playing && !paused) AL10.alSourcePlay(source);

            } catch (Exception ex) {
                Beatcraft.LOGGER.error("Instant-mode swap failed: {}", ex.getMessage());
            }
        });
    }

    private void streamLoopSinglePass() {
        streamLoopSinglePass(false, null);
    }

    private void streamLoopSinglePass(boolean cachePCM, ByteArrayOutputStream cache) {
        if (filePath == null) return;

        try (InputStream is = Files.newInputStream(filePath);
             JOrbisAudioStream s = new JOrbisAudioStream(is)) {

            int filled = 0;
            byte[] tmp = new byte[STREAM_BUFFER_SIZE];

            for (int i = 0; i < buffer.length; i++) {
                ByteBuffer pcm = s.read(STREAM_BUFFER_SIZE);
                if (!pcm.hasRemaining()) break;

                int len = pcm.remaining();
                pcm.get(tmp, 0, len);
                if (cachePCM && cache != null) cache.write(tmp, 0, len);

                if (buffer[i] == 0) buffer[i] = AL10.alGenBuffers();
                ByteBuffer albuf = BufferUtils.createByteBuffer(len).put(tmp, 0, len);
                albuf.flip();
                AL10.alBufferData(buffer[i], formatId, albuf, sampleRate);
                filled++;
            }

            if (filled == 0) {
                streaming = false;
                return;
            }

            AL10.alSourceQueueBuffers(source, buffer);
            if (playing && !paused) {
                AL10.alSourcePlay(source);
            }

            byte[] tmpRefill = new byte[STREAM_BUFFER_SIZE];
            while (streaming && !closed) {
                int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                while (processed-- > 0) {
                    int bufId = AL10.alSourceUnqueueBuffers(source);
                    ByteBuffer pcm = s.read(STREAM_BUFFER_SIZE);
                    if (!pcm.hasRemaining()) {
                        streaming = false;
                        break;
                    }
                    int len = pcm.remaining();
                    pcm.get(tmpRefill, 0, len);
                    if (cachePCM && cache != null) cache.write(tmpRefill, 0, len);

                    ByteBuffer albuf = BufferUtils.createByteBuffer(len).put(tmpRefill, 0, len);
                    albuf.flip();
                    AL10.alBufferData(bufId, formatId, albuf, sampleRate);
                    AL10.alSourceQueueBuffers(source, bufId);
                }

            }

            if (mode == Mode.STREAM) phase = 1;

        } catch (Exception ex) {
            Beatcraft.LOGGER.error("Streaming error for {}: {}", filePath, ex.getMessage());
            streaming = false;
        }
    }

    private byte[] decodeWholeFileToByteArray() throws IOException {
        try (InputStream is = Files.newInputStream(filePath);
             JOrbisAudioStream s = new JOrbisAudioStream(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ByteBuffer read;
            byte[] tmp = new byte[STREAM_BUFFER_SIZE];
            while (true) {
                read = s.read(STREAM_BUFFER_SIZE);
                if (!read.hasRemaining()) break;
                int len = read.remaining();
                read.get(tmp, 0, len);
                out.write(tmp, 0, len);
            }
            return out.toByteArray();
        }
    }

    private void ensureStreamBuffersAllocated() {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0) buffer[i] = AL10.alGenBuffers();
        }
    }

    private void deleteStreamBuffers() {
        if (buffer == null) return;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != 0) {
                try { AL10.alDeleteBuffers(buffer[i]); } catch (Exception ignored) {}
                buffer[i] = 0;
            }
        }
    }

    public synchronized void play() {
        if (closed || !loaded) return;
        playing = true;
        paused = false;

        AL10.alSourcePlay(source);
    }

    public synchronized void pause() {
        if (closed || !playing || !loaded) return;
        AL10.alSourcePause(source);
        paused = true;
    }

    public synchronized void stop() {
        if (closed) return;
        AL10.alSourceStop(source);
        playing = false;
        paused = false;
    }

    public boolean isOk() {
        return mode != Mode.ERR;
    }

    public void seek(double seconds) {
        if (closed) return;

        if (fullBuffer != -1) {
            AL11.alSourcef(source, AL11.AL_SEC_OFFSET, (float) seconds);
            return;
        }

        CompletableFuture.runAsync(() -> {
            streaming = false;
            try {
                AL10.alSourceStop(source);
            } catch (Exception ignored) {}

            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) {
                try { AL10.alSourceUnqueueBuffers(source); } catch (Exception ignored) {}
            }

            try (InputStream is = Files.newInputStream(filePath);
                 JOrbisAudioStream s = new JOrbisAudioStream(is)) {

                long bytesPerSecond = (long) sampleRate * channels * (sampleSizeInBits / 8);
                long bytesToSkip = (long) (seconds * bytesPerSecond);

                long skipped = 0;
                int safety = 0;
                ByteBuffer tmp;
                while (skipped < bytesToSkip) {
                    tmp = s.read(STREAM_BUFFER_SIZE);
                    if (!tmp.hasRemaining()) break;
                    skipped += tmp.remaining();
                    if (++safety > 1_000_000) break;
                }

                byte[] tmpArr = new byte[STREAM_BUFFER_SIZE];
                int filled = 0;
                for (int i = 0; i < buffer.length; i++) {
                    tmp = s.read(STREAM_BUFFER_SIZE);
                    if (!tmp.hasRemaining()) break;
                    int len = tmp.remaining();
                    tmp.get(tmpArr, 0, len);
                    ByteBuffer alBuf = BufferUtils.createByteBuffer(len).put(tmpArr, 0, len);
                    alBuf.flip();
                    if (buffer[i] == 0) buffer[i] = AL10.alGenBuffers();
                    AL10.alBufferData(buffer[i], formatId, alBuf, sampleRate);
                    filled++;
                }

                if (filled > 0) {
                    AL10.alSourceQueueBuffers(source, buffer);
                    if (playing && !paused) AL10.alSourcePlay(source);
                } else {
                    AL10.alSourceStop(source);
                }

                streaming = true;
                streamFuture = CompletableFuture.runAsync(this::streamLoopSinglePass);

            } catch (IOException ex) {
                Beatcraft.LOGGER.error("Seek error on {}: {}", filePath, ex.getMessage());
            }
        });
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        streaming = false;

        try { AL10.alSourceStop(source); } catch (Exception ignored) {}

        try {
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) AL10.alSourceUnqueueBuffers(source);
        } catch (Exception ignored) {}

        deleteStreamBuffers();

        if (fullBuffer != -1) {
            try { AL10.alDeleteBuffers(fullBuffer); } catch (Exception ignored) {}
            fullBuffer = -1;
        }

        try { AL10.alDeleteSources(source); } catch (Exception ignored) {}
    }

    public boolean isLoaded() { return loaded && !closed; }
    public boolean isPlaying() { return playing && !paused && !closed; }
    public boolean isPaused() { return paused && !closed; }
    public boolean isClosed() { return closed; }
}
