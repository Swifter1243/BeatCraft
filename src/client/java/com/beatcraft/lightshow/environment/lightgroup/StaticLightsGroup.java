package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;

import java.util.List;

public class StaticLightsGroup extends LightGroupV2 {

    public StaticLightsGroup(List<LightObject> lights) {
        this.lights.addAll(lights);
    }

    public boolean isLightEventGroup(EventGroup group) {
        return group == EventGroup.BACK_LASERS || group == EventGroup.CENTER_LASERS;
    }

    @Override
    public void handleEvent(EventGroup group, Object obj) {
        if (isLightEventGroup(group) && obj instanceof LightState state) {
            lights.forEach(l -> {
                l.setLightState(state);
            });
        }
    }

    @Override
    public void update(float beat, double deltaTime) {

    }
}
