package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;

public class RingZoomEvent extends ValueEvent {

    public Float step = null;
    public float speed = 1.0f;


    @Override
    public RingZoomEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        if (json.has("_customData")) {
            var customData = json.getAsJsonObject("_customData");

            if (customData.has("_step")) {
                step = customData.get("_step").getAsFloat();
            }

            if (customData.has("_speed")) {
                speed = 1f / customData.get("_speed").getAsFloat();
            }

        }

        return this;
    }

    @Override
    public RingZoomEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        if (json.has("customData")) {
            var customData = json.getAsJsonObject("customData");

            if (customData.has("step")) {
                step = customData.get("step").getAsFloat();
            }

            if (customData.has("speed")) {
                speed = 1f / customData.get("speed").getAsFloat();
            }

        }

        return this;
    }

    @Override
    public RingZoomEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        return this;
    }
}
