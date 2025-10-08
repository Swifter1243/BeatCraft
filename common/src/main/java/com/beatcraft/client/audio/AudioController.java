package com.beatcraft.client.audio;

import com.beatcraft.client.BeatcraftClient;

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

    public static void playMapPreview(String path, float startTime) {
        var audio = playAudioForChannel(0, path, Audio.Mode.STREAM);
        audio.seek(startTime);
        audio.play();
    }

    /// Sets the volume of all beatmap audio
    public static void setVolume(float percent) {
        for (var track : tracks) {
            track.setVolume(percent);
        }
    }

    public static Audio playMapSong(String path) {
        if (channels.containsKey(0)) {
            var preview = channels.remove(0);
            preview.close();
        }
        var audio = playAudio(path, Audio.Mode.INSTANT);
        audio.setVolume(BeatcraftClient.playerConfig.audio.volume());
        tracks.add(audio);
        return audio;
    }


    public static void remove(Audio audio) {
        tracks.remove(audio);
    }

}
