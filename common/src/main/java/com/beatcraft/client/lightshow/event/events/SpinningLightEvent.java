package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;

public class SpinningLightEvent extends ValueEvent {

    public boolean lockRotation = false;
    // public float speed = 0; // value
    public int direction = 0;

    public SpinningLightEvent() {

    }

    @Override
    public SpinningLightEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);
        // _customData":{"_preciseSpeed":5.5,"_direction":0,"_lockPosition":true}

        return this;
    }

    @Override
    public SpinningLightEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        return this;
    }

    @Override
    public SpinningLightEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        return this;
    }
}
