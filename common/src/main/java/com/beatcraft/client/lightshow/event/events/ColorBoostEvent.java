package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.beatcraft.common.event.IEvent;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ColorBoostEvent extends BeatmapObject implements IEvent {
    public boolean boosted = false;

    public ColorBoostEvent() {}

    public ColorBoostEvent(float beat, boolean boosted) {
        this.beat = beat;
        this.boosted = boosted;
    }

    @Override
    public float getEventBeat() {
        return beat;
    }

    @Override
    public float getEventDuration() {
        return 0;
    }

    @Override
    public ColorBoostEvent loadV2(JsonObject json, Difficulty difficulty) {
        beat = JsonUtil.getOrDefault(json, "_time", JsonElement::getAsFloat, 0f);
        boosted = JsonUtil.getOrDefault(json, "_value", JsonElement::getAsInt, 0) > 0;
        return this;
    }

    @Override
    public ColorBoostEvent loadV3(JsonObject json, Difficulty difficulty) {
        beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);
        boosted = JsonUtil.getOrDefault(json, "o", JsonElement::getAsBoolean, false);
        return this;
    }

    public ColorBoostEvent loadV4(JsonObject json, JsonArray rawMetaData, Difficulty difficulty) {
        beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);

        var index = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);

        var metaData = rawMetaData.get(index).getAsJsonObject();
        boosted = JsonUtil.getOrDefault(metaData, "b", JsonElement::getAsInt, 0) > 0;

        return this;
    }

}
