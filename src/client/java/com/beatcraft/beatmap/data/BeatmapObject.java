package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public abstract class BeatmapObject implements IBeatmapData<BeatmapObject> {
    public float beat = 0;

    @Override
    public BeatmapObject loadV2(JsonObject json, Info.SetDifficulty setDifficulty) {
        beat = json.get("_time").getAsFloat();
        return this;
    }

    @Override
    public BeatmapObject loadV3(JsonObject json, Info.SetDifficulty setDifficulty) {
        beat = json.get("b").getAsFloat();
        return this;
    }

    @Override
    public BeatmapObject loadV4(JsonObject objectJson, JsonObject lutJson, Info.SetDifficulty setDifficulty) {
        beat = objectJson.get("b").getAsFloat();
        return this;
    }
}
