package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.event.IEvent;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ValueEvent extends BeatmapObject implements IEvent {

    private int value = 0;
    private float duration = 0;

    @Override
    public float getEventBeat() {
        return getBeat();
    }

    @Override
    public float getEventDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("ValueEvent[b:%s, v:%s, d:%s]", getBeat(), value, duration);
    }

    @Override
    public ValueEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);
        value = JsonUtil.getOrDefault(json, "_value", JsonElement::getAsInt, 0);
        return this;
    }


    @Override
    public ValueEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);
        value = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        return this;

    }

    public ValueEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        value = JsonUtil.getOrDefault(data, "i", JsonElement::getAsInt, 0);

        return this;
    }

    public int getValue() {
        return value;
    }

}
