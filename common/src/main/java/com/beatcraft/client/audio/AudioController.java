package com.beatcraft.client.audio;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioController {


    private static final ArrayList<Audio> tracks = new ArrayList<>();

    private static final HashMap<Integer, Audio> channels = new HashMap<>();

    public static Audio playAudio(String path, Audio.Mode mode) {
        var audio = Audio.loadFromFile(path, mode);
        audio.play();
        return audio;
    }

    public static Audio playAudioForChannel(int channel, String path, Audio.Mode mode) {
        if (channels.containsKey(channel)) {
            var old = channels.remove(channel);
            old.close();
        }

        var audio = Audio.loadFromFile(path, mode);

        if (audio.isOk()) {
            channels.put(channel, audio);
            audio.play();
        }

        return audio;
    }

    public static Audio playMapPreview(String path, float startTime) {
        var audio = playAudioForChannel(0, path, Audio.Mode.STREAM);
        audio.seek(startTime);
        audio.play();
        return audio;
    }

    public static Audio playMapSong(String path) {
        return playAudio(path, Audio.Mode.INSTANT);
    }

}
