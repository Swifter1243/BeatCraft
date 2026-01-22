package com.beatcraft.client.audio;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.VorbisFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.JOrbisAudioStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path file = Path.of(audioFileName);

        try {
            // Use VorbisFile to get duration directly from headers
            VorbisFile vf = new VorbisFile(file.toString());

            float duration = vf.time_total(-1); // Returns duration in seconds
            Info info = vf.getInfo(0); // Get audio format info
            int frequency = info.rate;

            // Calculate sample count from duration and frequency
            int sampleCount = Math.round(duration * frequency);

            var audioInfo = new AudioInfo(sampleCount, frequency);
            audioInfo.regions.add(new BpmRegion(audioInfo, bpm));
            return audioInfo;

        } catch (Exception e) {
            Beatcraft.LOGGER.error("Failed to load audio info {}", audioFileName, e);
            return new AudioInfo(0, 44100);
        }
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
