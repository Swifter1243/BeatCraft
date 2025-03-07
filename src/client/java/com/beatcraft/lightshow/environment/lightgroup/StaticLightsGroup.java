package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;

import java.util.HashMap;
import java.util.List;

public class StaticLightsGroup extends LightGroupV2 {

    public StaticLightsGroup(HashMap<Integer, LightObject> lights) {
        this.lights.putAll(lights);
    }

    public boolean isLightEventGroup(EventGroup group) {
        return group == EventGroup.BACK_LASERS || group == EventGroup.CENTER_LASERS;
    }

    @Override
    public void handleEvent(EventGroup group, Object obj) {
        if (isLightEventGroup(group) && obj instanceof LightState state) {
            lights.values().forEach(l -> {
                l.setLightState(state);
            });
        }
    }

    @Override
    public void update(float beat, double deltaTime) {

    }
}
