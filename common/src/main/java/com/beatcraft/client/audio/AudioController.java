package com.beatcraft.client.audio;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioController {


    private static final ArrayList<Audio> tracks = new ArrayList<>();

    private static final HashMap<Integer, Audio> channels = new HashMap<>();

    public Audio playAudio(String path, Audio.Mode mode) {
        var audio = Audio.loadFromFile(path, mode);
        audio.play();
        return audio;
    }

    public void playAudioForChannel(int channel, String path, Audio.Mode mode) {
        if (channels.containsKey(channel)) {
            var old = channels.remove(channel);
            old.close();
        }

        var audio = Audio.loadFromFile(path, mode);

        if (audio.isOk()) {
            channels.put(channel, audio);
            audio.play();
        }

    }

    public void playMapPreview(String path, float startTime) {
        playAudioForChannel(0, path, Audio.Mode.STREAM);
        if (channels.containsKey(0)) {
            var audio = channels.get(0);
            audio.seek(startTime);
            audio.play();
        }
    }

    public void playMapSong(String path) {
        playAudioForChannel(0, path, Audio.Mode.INSTANT);
    }

}
