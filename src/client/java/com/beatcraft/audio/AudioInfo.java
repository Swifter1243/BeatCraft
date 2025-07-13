package com.beatcraft.audio;

import com.beatcraft.BeatCraft;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class AudioInfo {

    private final float songDuration;
    private final int frequency;
    private final ArrayList<BpmRegion> regions = new ArrayList<>();

    private AudioInfo(int sampleCount, int frequency) {
        this.songDuration = (float) sampleCount / (float) frequency;
        this.frequency = frequency;
    }

    public float getSongDuration() {
        return songDuration;
    }

    public float getBpm(float beat) {

        for (var region : regions) {
            if (region.containsBeat(beat)) {
                return region.bpm;
            }
        }
        return 120; // this is theoretically impossible to reach but better be safe I guess?
    }

    public static AudioInfo loadV2(JsonObject json) {
        int sampleCount = json.get("_songSampleCount").getAsInt();
        int frequency = json.get("_songFrequency").getAsInt();

        AudioInfo info = new AudioInfo(sampleCount, frequency);

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

        AudioInfo info = new AudioInfo(sampleCount, frequency);

        JsonArray regions = json.getAsJsonArray("bpmData");

        regions.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            BpmRegion bpmRegion = BpmRegion.loadV4(obj, info);
            info.regions.add(bpmRegion);
        });

        return info;
    }

    public float getBeat(float time, float speedModifier) {
        float beat = 0;
        for (BpmRegion region : regions) {
            float b = region.getBeat(time, speedModifier);
            if (b == 0) return beat;
            beat = b;
        }
        return beat;
    }

    public float getTime(float beat, float speedModifier) {
        float t = 0;
        for (BpmRegion region : regions) {
            t += region.getTime(beat, speedModifier);
        }

        return t;
    }


    public static class BpmRegion {
        private final int startIndex;
        private final int endIndex;
        private final float startBeat;
        private final float endBeat;
        public final float bpm;
        private final AudioInfo info;

        private BpmRegion(AudioInfo parent, int startIndex, int endIndex, float startBeat, float endBeat) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startBeat = startBeat;
            this.endBeat = endBeat;
            info = parent;

            var beats = endBeat - startBeat;
            var samples = endIndex - startIndex;
            var dt = samples / info.frequency;
            bpm = (beats / dt) * 60f;

        }

        public float getBeat(float time, float speedModifier) {
            float startTime = (((float) startIndex) / info.frequency) * speedModifier;
            if (time < startTime) return 0;

            float duration = ((((float) endIndex) / info.frequency) * speedModifier) - startTime;

            float progress = Math.clamp((time - startTime) / duration, 0, 1);

            return startBeat + (progress * (endBeat-startBeat));
        }

        public float getTime(float beat, float speedModifier) {
            if (beat < startBeat) return 0;

            float progress = Math.clamp((beat - startBeat) / (endBeat - startBeat), 0, 1);
            float samples = (endIndex - startIndex) * progress;

            float t = samples/info.frequency;
            return t * speedModifier;
        }

        public boolean containsBeat(float currentBeat) {
            return startBeat <= currentBeat && currentBeat < endBeat;
        }

        public static BpmRegion loadV2(JsonObject json, AudioInfo parent) {
            var region = new BpmRegion(
                parent,
                json.get("_startSampleIndex").getAsInt(),
                json.get("_endSampleIndex").getAsInt(),
                json.get("_startBeat").getAsFloat(),
                json.get("_endBeat").getAsFloat()
            );
            BeatCraft.LOGGER.info("BPM Region made V2: {}, {}, {}, {}", region.startIndex, region.endIndex, region.startBeat, region.endBeat);
            return region;
        }

        public static BpmRegion loadV4(JsonObject json, AudioInfo parent) {
            var region = new BpmRegion(
                parent,
                json.get("si").getAsInt(),
                json.get("ei").getAsInt(),
                json.get("sb").getAsFloat(),
                json.get("eb").getAsFloat()
            );
            BeatCraft.LOGGER.info("BPM Region made V4: {}, {}, {}, {}", region.startIndex, region.endIndex, region.startBeat, region.endBeat);
            return region;
        }

    }

}
