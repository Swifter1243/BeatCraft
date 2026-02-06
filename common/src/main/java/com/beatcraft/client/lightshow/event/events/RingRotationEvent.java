package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.ring_lights.RingLightHandler;
import com.google.gson.JsonObject;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class RingRotationEvent extends ValueEvent {

    public enum RingTarget {
        Both,
        Inner,
        Outer,
    }

    public enum Direction {
        Random,
        CCW,
        CW;

        public float apply(float speed, RandomSource random) {
            switch (this) {
                case Random -> {
                    if (random.nextBoolean()) {
                        return speed;
                    } else {
                        return -speed;
                    }
                }
                case CCW -> {
                    return speed;
                }
                case CW -> {
                    return -speed;
                }
            }
            return speed;
        }
    }

    public RingTarget target = RingTarget.Both;
    public Float rotation = null;
    public Float step = null;
    public float prop = 0.025f;
    public float speed = 1.0f;
    public Direction direction = Direction.Random;

    @Override
    public RingRotationEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        if (json.has("_customData")) {
            var customData = json.getAsJsonObject("_customData");

            if (customData.has("_direction")) {
                direction = customData.get("_direction").getAsInt() == 1
                    ? Direction.CW
                    : Direction.CCW;
            } else {
                direction = Direction.Random;
            }

            if (customData.has("_prop")) {
                prop = 1f - (customData.get("_prop").getAsFloat() / 100f);
            }

            if (customData.has("_rotation")) {
                rotation = customData.get("_rotation").getAsFloat() * Mth.DEG_TO_RAD;
            }

            if (customData.has("_speed")) {
                speed = customData.get("_speed").getAsFloat();
            }

            if (customData.has("_step")) {
                step = customData.get("_step").getAsFloat() * Mth.DEG_TO_RAD;
            }

            if (customData.has("_nameFilter")) {
                var name = customData.get("_nameFilter").getAsString();
                switch (name) {
                    case "SmallTrackLaneRing",
                         "TriangleTrackLaneRing" -> {
                        target = RingTarget.Inner;
                    }
                    case "BigTrackLaneRing", "LightsTrackLaneRing" -> {
                        target = RingTarget.Outer;
                    }
                    default -> {}
                }
            }

        }

        return this;
    }

    @Override
    public RingRotationEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        if (json.has("customData")) {
            var customData = json.getAsJsonObject("customData");

            if (customData.has("direction")) {
                direction = customData.get("direction").getAsInt() == 1
                    ? Direction.CW
                    : Direction.CCW;
            } else {
                direction = Direction.Random;
            }

            if (customData.has("prop")) {
                prop = 1f - (customData.get("prop").getAsFloat() / 100f);
            }

            if (customData.has("rotation")) {
                rotation = customData.get("rotation").getAsFloat() * Mth.DEG_TO_RAD;
            }

            if (customData.has("speed")) {
                speed = customData.get("speed").getAsFloat();
            }

            if (customData.has("step")) {
                step = customData.get("step").getAsFloat() * Mth.DEG_TO_RAD;
            }

            if (customData.has("nameFilter")) {
                var name = customData.get("nameFilter").getAsString();
                switch (name) {
                    case "SmallTrackLaneRing",
                         "TriangleTrackLaneRing" -> {
                        target = RingTarget.Inner;
                    }
                    case "BigTrackLaneRing", "LightsTrackLaneRing" -> {
                        target = RingTarget.Outer;
                    }
                    default -> {}
                }
            }

        }

        return this;
    }

    @Override
    public RingRotationEvent loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV4(json, data, difficulty);

        return this;
    }


    public void apply(RingLightHandler ringLights, RandomSource random) {

        float target = ringLights.getCurrentRotation();
        float targetStep;

        if (rotation == null) {
            target += ringLights.presets.getJumpOffset(random);
        } else {
            target += direction.apply(rotation, random);
        }

        if (step == null) {
            targetStep = ringLights.presets.getRotationOffset(random);
        } else {
            targetStep = step;
        }

        ringLights.spinTo(target, targetStep, prop, 1f);

    }

}
