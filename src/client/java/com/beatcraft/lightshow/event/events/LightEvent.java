package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EnvironmentColor;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.data.types.Color;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LightEvent extends BeatmapObject implements IEvent {
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
    public LightEvent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        lightEventType = JsonUtil.getOrDefault(json, "_value", JsonElement::getAsInt, 0);

        eventValue = JsonUtil.getOrDefault(json, "_floatValue", JsonElement::getAsFloat, 1f);

        lightState = new LightState(new Color(0), 0);

        var customData = JsonUtil.getOrDefault(json,"_customData", JsonElement::getAsJsonObject, null);
        if (customData != null) {
            var lightIDs = JsonUtil.getOrDefault(customData, "_lightID", JsonElement::getAsJsonArray, null);
            if (lightIDs != null) {
                this.lightIDs = new int[lightIDs.size()];

                for (int i = 0; i < lightIDs.size(); i++) {
                    int lightID = lightIDs.get(i).getAsInt();
                    this.lightIDs[i] = lightID;
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

    private Color getColor(Difficulty difficulty, EnvironmentColor environmentColor) {
        var colorScheme = difficulty.getSetDifficulty().getColorScheme();

        if (chromaColor != null) {
            return chromaColor;
        }

        return switch (environmentColor) {
            case LEFT -> colorScheme.getEnvironmentLeftColor().withAlpha(1);
            case RIGHT -> colorScheme.getEnvironmentRightColor().withAlpha(1);
            case WHITE -> colorScheme.getEnvironmentWhiteColor().withAlpha(1);
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
                lightState.setColor(getColor(difficulty, EnvironmentColor.RIGHT));
                lightState.setBrightness(eventValue);
            }
            case 2, 3, 4 -> {
                lightState.setColor(getColor(difficulty, EnvironmentColor.RIGHT));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }
            case 5 -> {
                lightState.setColor(getColor(difficulty, EnvironmentColor.LEFT));
                lightState.setBrightness(eventValue);
            }
            case 6, 7, 8 -> {
                lightState.setColor(getColor(difficulty, EnvironmentColor.LEFT));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }
            case 9 -> {
                lightState.setColor(getColor(difficulty, EnvironmentColor.WHITE));
                lightState.setBrightness(eventValue);
            }
            case 10, 11, 12 -> {
                lightState.setColor(getColor(difficulty, EnvironmentColor.WHITE));
                lightState.setBrightness(eventValue);
                duration = FADE_DURATION;
            }

            default -> {}
        }
    }

}
