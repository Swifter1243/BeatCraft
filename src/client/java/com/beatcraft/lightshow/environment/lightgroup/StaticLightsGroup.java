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

    @Override
    public void update(float beat, double deltaTime) {

    }
}
