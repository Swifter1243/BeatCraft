package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class RingRotationEvent extends ValueEvent {

    @Nullable
    public String nameFilter = null;
    public Float rotation = null;
    public Float step = null;
    public float prop = 1.0f;
    public float speed = 1.0f;
    public int direction = -1; // -1: random, 0: ccw, 1: cw

    @Override
    public RingRotationEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);
        // "_customData":{"_direction":1,"_prop":100,"_rotation":180,"_speed":10,"_step":50}

        if (json.has("_customData")) {
            var customData = json.getAsJsonObject("_customData");
        }

        return this;
    }

    @Override
    public RingRotationEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        return this;
    }

    @Override
    public RingRotationEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        return this;
    }

}
