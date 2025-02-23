package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.IBeatmapData;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class BeatmapObject implements IBeatmapData<BeatmapObject> {
    protected float beat = 0;

    @Override
    public BeatmapObject loadV2(JsonObject json, Difficulty difficulty) {
        this.beat = json.get("_time").getAsFloat();
        return this;
    }

    @Override
    public BeatmapObject loadV3(JsonObject json, Difficulty difficulty) {
        this.beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);
        return this;
    }

    public float getBeat() {
        return beat;
    }
}
