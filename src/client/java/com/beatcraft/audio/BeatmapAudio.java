package com.beatcraft.audio;

import net.minecraft.client.sound.OggAudioStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BeatmapAudio {
    private int buffer;
    private final int source;
    private boolean isPlaying = false;
    private boolean isLoaded = false;

    public BeatmapAudio() {
        source = AL10.alGenSources();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void pause() {
        if (isPlaying && isLoaded) {
            AL10.alSourcePause(source);
            isPlaying = false;
        }
    }

    public void stop() {
        if (isPlaying && isLoaded) {
            AL10.alSourceStop(source);
            isPlaying = false;
        }
    }

    public void play() {
        if (!isPlaying && isLoaded) {
            AL10.alSourcePlay(source);
            isPlaying = true;
        }
    }
    public void play(float time) {
        seek(time);
        play();
    }

    public void seek(float time) {
        if (isLoaded) {
            AL11.alSourcef(source, AL11.AL_SEC_OFFSET, time);
        }
    }

    public void setPlaybackSpeed(float speed) {
        AL10.alSourcef(source, AL10.AL_PITCH, speed);
    }

    private int getFormatID(AudioFormat format) {
        AudioFormat.Encoding encoding = format.getEncoding();
        int channels = format.getChannels();
        int sampleSize = format.getSampleSizeInBits();

        if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED) || encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (channels == 1) {
                if (sampleSize == 8) {
                    return AL10.AL_FORMAT_MONO8;
                }
                if (sampleSize == 16) {
                    return AL10.AL_FORMAT_MONO16;
                }
            } else if (channels == 2) {
                if (sampleSize == 8) {
                    return AL10.AL_FORMAT_STEREO8;
                }
                if (sampleSize == 16) {
                    return AL10.AL_FORMAT_STEREO16;
                }
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + format);
    }

    public void loadAudioFromFile(String path) throws IOException {
        closeBuffer();

        InputStream inputStream = Files.newInputStream(Path.of(path));
        OggAudioStream oggAudioStream = new OggAudioStream(inputStream);

        AudioFormat format = oggAudioStream.getFormat();
        buffer = AL10.alGenBuffers();

        int formatID = getFormatID(format);
        int sampleRate = (int) format.getSampleRate();

        ByteBuffer audioData = oggAudioStream.getBuffer();
        AL10.alBufferData(buffer, formatID, audioData, sampleRate);
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);

        isLoaded = true;
        inputStream.close();
        oggAudioStream.close();
    }

    public void closeBuffer() {
        if (isLoaded) {
            stop();
            AL10.alDeleteBuffers(buffer);
            isLoaded = false;
        }
    }
}
