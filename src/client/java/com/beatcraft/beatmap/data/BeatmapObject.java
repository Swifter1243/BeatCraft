package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;

public abstract class BeatmapObject implements IBeatmapData<BeatmapObject> {
    private float beat = 0;

    @Override
    public BeatmapObject loadV2(JsonObject json, Difficulty difficulty) {
        setBeat(json.get("_time").getAsFloat());
        return this;
    }

    @Override
    public BeatmapObject loadV3(JsonObject json, Difficulty difficulty) {
        setBeat(json.get("b").getAsFloat());
        return this;
    }

    @Override
    public BeatmapObject loadV4(JsonObject objectJson, JsonObject lutJson, Difficulty difficulty) {
        setBeat(objectJson.get("b").getAsFloat());
        return this;
    }

    public float getBeat() {
        return beat;
    }

    public void setBeat(float beat) {
        this.beat = beat;
    }
}
