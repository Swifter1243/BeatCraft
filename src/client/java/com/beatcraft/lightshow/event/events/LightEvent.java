package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.data.types.Color;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LightEvent extends BeatmapObject implements IEvent {

    private LightState lightState;
    private int lightEventType = 0;
    private float eventValue = 0;
    private float duration = 0;
    private int[] lightIDs;

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
        process(difficulty, lightState);

        var customData = JsonUtil.getOrDefault(json,"_customData", JsonElement::getAsJsonObject, null);
        if (customData != null) {
            var lightIDs = JsonUtil.getOrDefault(customData, "_lightID", JsonElement::getAsJsonArray, null);
            if (lightIDs != null) {
                this.lightIDs = new int[lightIDs.size()];

                lightIDs.forEach(lightIDElem -> {
                    int lightID = lightIDElem.getAsInt();
                    this.lightIDs[lightID] = lightID;
                });
            }
        }

        return this;
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
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentRightColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
            }
            case 2, 3, 4 -> {
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentRightColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
                duration = 1;
            }
            case 5 -> {
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentLeftColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
            }
            case 6, 7, 8 -> {
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentLeftColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
                duration = 1;
            }
            case 9 -> {
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentWhiteColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
            }
            case 10, 11, 12 -> {
                lightState.setColor(difficulty.getSetDifficulty().getColorScheme().getEnvironmentWhiteColor().withAlpha(1f));
                lightState.setBrightness(eventValue);
                duration = 1;
            }

            default -> {}
        }
    }

}
