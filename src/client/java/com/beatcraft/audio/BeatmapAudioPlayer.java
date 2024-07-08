package com.beatcraft.audio;

import net.minecraft.client.sound.*;
import org.lwjgl.openal.AL10;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BeatmapAudioPlayer {
    public static void loadAudioFromFile(String path) throws IOException {
        InputStream inputStream = Files.newInputStream(Path.of(path));
        OggAudioStream oggAudioStream = new OggAudioStream(inputStream);

        ByteBuffer audioData = oggAudioStream.getBuffer();

        int format;
        if (oggAudioStream.getFormat().getChannels() == 1) {
            format = AL10.AL_FORMAT_MONO16;
        } else if (oggAudioStream.getFormat().getChannels() == 2) {
            format = AL10.AL_FORMAT_STEREO16;
        } else {
            throw new IllegalArgumentException("Unsupported number of channels: " + oggAudioStream.getFormat().getChannels());
        }

        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, format, audioData, (int)oggAudioStream.getFormat().getSampleRate());

        oggAudioStream.close();
        inputStream.close();

        int source = AL10.alGenSources();
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);

        AL10.alSourcePlay(source);
    }
}
