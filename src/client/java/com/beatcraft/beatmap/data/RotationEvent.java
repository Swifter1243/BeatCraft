package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.event.IEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RotationEvent extends BeatmapObject implements IEvent<Float> {
    private float rotation;
    private final boolean early;

    public RotationEvent(boolean early) {
        this.early = early;
    }

    public float getRotation() {
        return rotation;
    }

    public boolean isEarly() {
        return early;
    }

    @Override
    public float getEventBeat() {
        return getBeat();
    }

    @Override
    public float getEventDuration() {
        return 0;
    }

    @Override
    public Float getEventData(float normalTime) {
        return rotation;
    }

    private enum V2ROTATION {
        CCW_60,
        CCW_45,
        CCW_30,
        CCW_15,
        CW_15,
        CW_30,
        CW_45,
        CW_60
    }

    private static float getRotationAsFloat(JsonElement element) {
        V2ROTATION rotation = V2ROTATION.values()[element.getAsInt()];
        return switch(rotation) {
            case CCW_60 -> -60F;
            case CCW_45 -> -45F;
            case CCW_30 -> -30F;
            case CCW_15 -> -15F;
            case CW_15 -> 15F;
            case CW_30 -> 30F;
            case CW_45 -> 45F;
            case CW_60 -> 60F;
        };
    }

    @Override
    public RotationEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);
        this.rotation = getRotationAsFloat(json.get("_value"));

        if (json.has("_customData")) {
            JsonObject customData = json.getAsJsonObject("_customData");

            if (customData.has("_rotation")) {
                this.rotation = customData.get("_rotation").getAsFloat();
            }
        }

        return this;
    }

    @Override
    public RotationEvent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);
        this.rotation = json.get("r").getAsFloat();
        return this;
    }

    public RotationEvent fromBasicEventV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);
        this.rotation = getRotationAsFloat(json.get("i"));

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("rotation")) {
                this.rotation = customData.get("rotation").getAsFloat();
            }
        }

        return this;
    }
}
