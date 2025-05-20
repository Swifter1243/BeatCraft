package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EnvironmentColor;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.data.types.Color;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.environment.BoostableColor;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LightEventV2 extends LightEvent implements IEvent {
    private static final float FADE_DURATION = 4;
    private LightState lightState;
    private int lightEventType = 0;
    private float eventValue = 0;
    private float duration = 0;
    private int[] lightIDs;
    private Color chromaColor = null;

    private LightState fadeFrom = null;

    public void setFadeFrom(LightState from) {
        if (fadeFrom == null) fadeFrom = from;
    }

    public LightState getFaded(float t) {
        if (fadeFrom != null) {
            return fadeFrom.lerpFromTo(lightState, t);
        }
        return lightState;
    }

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
        return String.format("LightEvent[b:%s, v:%s, f:%s, d:%s]", getBeat(), lightEventType, eventValue, duration);
    }

    public boolean isFlashType() {
        return lightEventType == 2 || lightEventType == 6 || lightEventType == 10;
    }

    public boolean isFadeType() {
        return lightEventType == 3 || lightEventType == 7 || lightEventType == 11;
    }

    public boolean isTransitionType() {
        return lightEventType == 4 || lightEventType == 8 || lightEventType == 12;
    }

    @Override
    public LightEventV2 loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        lightEventType = JsonUtil.getOrDefault(json, "_value", JsonElement::getAsInt, 0);
        eventValue = JsonUtil.getOrDefault(json, "_floatValue", JsonElement::getAsFloat, 1f);
        lightState = new LightState(new Color(0), 0);

        var customData = JsonUtil.getOrDefault(json,"_customData", JsonElement::getAsJsonObject, null);
        if (customData != null) {

            var lightIDs = JsonUtil.getOrDefault(customData, "_lightID", e -> e, null);
            if (lightIDs != null) {
                if (lightIDs.isJsonArray()) {
                    var lightIDsArray = lightIDs.getAsJsonArray();
                    this.lightIDs = new int[lightIDsArray.size()];
                    for (int i = 0; i < lightIDsArray.size(); i++) {
                        int lightID = lightIDsArray.get(i).getAsInt();
                        this.lightIDs[i] = lightID;
                    }
                } else {
                    this.lightIDs = new int[1];
                    this.lightIDs[0] = lightIDs.getAsInt();
                }
            }

            var chromaColor = JsonUtil.getOrDefault(customData, "_color", JsonElement::getAsJsonArray, null);
            if (chromaColor != null) {
                this.chromaColor = Color.fromJsonArray(chromaColor);
            }
        }

        process(difficulty, lightState);

        return this;
    }

    public LightEventV2 loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        lightEventType = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        eventValue = JsonUtil.getOrDefault(json, "f", JsonElement::getAsFloat, 1.0f);
        lightState = new LightState(new Color(0), 0);

        process(difficulty, lightState);

        return this;

    }

    public LightEventV2 loadV4(JsonObject json, JsonObject data, Difficulty difficulty) {
        super.loadV3(json, difficulty);


        lightEventType = JsonUtil.getOrDefault(data, "i", JsonElement::getAsInt, 0);
        eventValue = JsonUtil.getOrDefault(data, "f", JsonElement::getAsFloat, 1.0f);
        lightState = new LightState(new Color(0), 0);

        process(difficulty, lightState);

        return this;
    }

    private Color getColor(EnvironmentColor environmentColor) {

        if (chromaColor != null) {
            return chromaColor;
        }

        return switch (environmentColor) {
            case LEFT -> new BoostableColor(0);
            case RIGHT -> new BoostableColor(1);
            case WHITE -> new BoostableColor(2);
        };
    }

    public boolean containsLightID(int lightID) {
        if (lightIDs == null) return true;

        for (int id : lightIDs) {
            if (id == lightID) return true;
        }
        return false;
    }

    public LightState getLightState() {
        return lightState;
    }

    public void process(Difficulty difficulty, LightState lightState) {
        switch (lightEventType) {
            case 0 -> {
                lightState.setBrightness(0);
                lightState.setColor(new Color(0));
            }
            case 1 -> {
                lightState.setColor(getColor(EnvironmentColor.RIGHT));
                lightState.setBrightness(eventValue);
            }
            case 2, 3, 4 -> {
                lightState.setColor(getColor(EnvironmentColor.RIGHT));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }
            case 5 -> {
                lightState.setColor(getColor(EnvironmentColor.LEFT));
                lightState.setBrightness(eventValue);
            }
            case 6, 7, 8 -> {
                lightState.setColor(getColor(EnvironmentColor.LEFT));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }
            case 9 -> {
                lightState.setColor(getColor(EnvironmentColor.WHITE));
                lightState.setBrightness(eventValue);
            }
            case 10, 11, 12 -> {
                lightState.setColor(getColor(EnvironmentColor.WHITE));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }

            default -> {}
        }
    }

}
