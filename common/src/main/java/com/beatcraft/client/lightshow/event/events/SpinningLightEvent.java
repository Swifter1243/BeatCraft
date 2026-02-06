package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;

public class SpinningLightEvent extends ValueEvent {

    public boolean lockRotation = false;
    public float speed = 0;
    public RingRotationEvent.Direction direction = RingRotationEvent.Direction.Random;

    public SpinningLightEvent() {

    }

    @Override
    public SpinningLightEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        speed = (float) value;

        if (json.has("_customData")) {
            var customData = json.getAsJsonObject("_customData");

            if (customData.has("_lockPosition")) {
                lockRotation = customData.get("_lockPosition").getAsBoolean();
            }

            if (customData.has("_preciseSpeed")) {
                speed = customData.get("_preciseSpeed").getAsFloat();
            }

            if (customData.has("_direction")) {
                direction = customData.get("_direction").getAsInt() == 1
                    ? RingRotationEvent.Direction.CW
                    : RingRotationEvent.Direction.CCW;
            }

        }

        return this;
    }

    @Override
    public SpinningLightEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        speed = (float) value;

        if (json.has("customData")) {
            var customData = json.getAsJsonObject("customData");

            if (customData.has("lockRotation")) {
                lockRotation = customData.get("lockRotation").getAsBoolean();
            }

            if (customData.has("speed")) {
                speed = customData.get("speed").getAsFloat();
            }

            if (customData.has("direction")) {
                direction = customData.get("direction").getAsInt() == 1
                    ? RingRotationEvent.Direction.CW
                    : RingRotationEvent.Direction.CCW;
            }

        }

        return this;
    }

    @Override
    public SpinningLightEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        speed = (float) value;

        return this;
    }
}
