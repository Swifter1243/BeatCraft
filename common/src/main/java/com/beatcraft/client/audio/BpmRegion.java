package com.beatcraft.client.audio;

import com.beatcraft.Beatcraft;
import com.google.gson.JsonObject;

public class BpmRegion {
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

    public float getBeat(float time) {
        float startTime = (((float) startIndex) / info.frequency);
        if (time < startTime) return 0;

        float duration = ((((float) endIndex) / info.frequency)) - startTime;

        float progress = Math.clamp((time - startTime) / duration, 0, 1);

        return startBeat + (progress * (endBeat-startBeat));
    }

    public float getTime(float beat) {
        if (beat < startBeat) return 0;

        float progress = Math.clamp((beat - startBeat) / (endBeat - startBeat), 0, 1);
        float samples = (endIndex - startIndex) * progress;

        float t = samples/info.frequency;
        return t;
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
        Beatcraft.LOGGER.info("BPM Region made V2: {}, {}, {}, {}", region.startIndex, region.endIndex, region.startBeat, region.endBeat);
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
        Beatcraft.LOGGER.info("BPM Region made V4: {}, {}, {}, {}", region.startIndex, region.endIndex, region.startBeat, region.endBeat);
        return region;
    }

}