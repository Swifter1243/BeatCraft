package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;

public abstract class BeatmapObject implements IBeatmapData<BeatmapObject> {
    public float beat = 0;

    @Override
    public BeatmapObject loadV2(JsonObject json, Difficulty difficulty) {
        beat = json.get("_time").getAsFloat();
        return this;
    }

    @Override
    public BeatmapObject loadV3(JsonObject json, Difficulty difficulty) {
        beat = json.get("b").getAsFloat();
        return this;
    }

    @Override
    public BeatmapObject loadV4(JsonObject objectJson, JsonObject lutJson, Difficulty difficulty) {
        beat = objectJson.get("b").getAsFloat();
        return this;
    }
}
