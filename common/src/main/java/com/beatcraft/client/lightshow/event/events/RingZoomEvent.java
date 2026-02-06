package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;

public class RingZoomEvent extends ValueEvent {

    public Float step = null;
    // public float speed = 1.0f; // value

    public RingZoomEvent() {

    }

    @Override
    public RingZoomEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        return this;
    }

    @Override
    public RingZoomEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        return this;
    }

    @Override
    public RingZoomEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        return this;
    }
}
