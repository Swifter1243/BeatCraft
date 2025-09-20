package com.beatcraft.client.audio;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapPlayer;
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

public class Audio {

    public enum Mode {
        STREAM,
        INSTANT,
        FULL,
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

        // TODO: figure out streaming modes without deadlocking my PC lol

        audio.loadFull();

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

        if (fullBuffer != 0) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, seconds);
            return;
        }

    }

    public void setSpeed(float speed) {
        AL10.alSourcef(source, AL10.AL_PITCH, speed);
    }

    private boolean wasInWall = false;
    private double lastSeconds = 0;

    public void update(float beat, double dt, BeatmapPlayer controller) {
        if (!isLoaded()) {
            return;
        }
        if (Minecraft.getInstance().isPaused() || !controller.isPlaying()) {
            pause();
        } else {
            // Only seek if controller time jumped (big drift or manual scrub)
            if (Math.abs(controller.currentSeconds - lastSeconds) > 0.2) {
                // Beatcraft.LOGGER.info("re-seeking");
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
