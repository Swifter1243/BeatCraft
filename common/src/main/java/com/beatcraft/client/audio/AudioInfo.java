package com.beatcraft.client.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lwjgl.openal.AL10;

import java.util.ArrayList;

public class AudioInfo {

    // raw audio info
    public final float duration;
    public final int frequency;

    // beatmap audio properties
    private final ArrayList<BpmRegion> regions = new ArrayList<>();

    private AudioInfo(int sampleCount, int frequency) {
        this.duration = (float) sampleCount / (float) frequency;
        this.frequency = frequency;
    }

    public static AudioInfo loadDefault(float bpm, String audioFileName) {

        var audio = Audio.loadFromFile(audioFileName, Audio.Mode.FULL);

        int size = AL10.alGetBufferi(audio.buffer[0], AL10.AL_SIZE);
        int frequency = AL10.alGetBufferi(audio.buffer[0], AL10.AL_FREQUENCY);
        int channels = AL10.alGetBufferi(audio.buffer[0], AL10.AL_CHANNELS);
        int bits = AL10.alGetBufferi(audio.buffer[0], AL10.AL_BITS);

        int sampleCount = size / (channels * (bits / 8));

        var info = new AudioInfo(sampleCount, frequency);

        var inf = new BpmRegion(info, bpm);
        info.regions.add(inf);

        return info;
    }

    public static AudioInfo loadV2(JsonObject json) {
        int sampleCount = json.get("_songSampleCount").getAsInt();
        int frequency = json.get("_songFrequency").getAsInt();

        var info = new AudioInfo(sampleCount, frequency);

        JsonArray regions = json.getAsJsonArray("_regions");

        regions.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            BpmRegion bpmRegion = BpmRegion.loadV2(obj, info);
            info.regions.add(bpmRegion);
        });

        return info;
    }

    public static AudioInfo loadV4(JsonObject json) {
        int sampleCount = json.get("songSampleCount").getAsInt();
        int frequency = json.get("songFrequency").getAsInt();

        var info = new AudioInfo(sampleCount, frequency);

        JsonArray regions = json.getAsJsonArray("bpmData");

        regions.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            BpmRegion bpmRegion = BpmRegion.loadV4(obj, info);
            info.regions.add(bpmRegion);
        });

        return info;
    }

    public float getBpm(float beat) {
        for (var region : regions) {
            if (region.containsBeat(beat)) {
                return region.bpm;
            }
        }
        return 60;
    }

    public float getBeat(float time) {
        float beat = 0;
        for (BpmRegion region : regions) {
            float b = region.getBeat(time);
            if (b == 0) return beat;
            beat = b;
        }
        return beat;
    }

    public float getTime(float beat) {
        float t = 0;
        for (BpmRegion region : regions) {
            t += region.getTime(beat);
        }

        return t;
    }

}
