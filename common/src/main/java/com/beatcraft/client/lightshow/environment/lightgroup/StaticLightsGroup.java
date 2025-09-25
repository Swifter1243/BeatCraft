package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;

import java.util.HashMap;

public class StaticLightsGroup extends LightGroupV2 {

    public StaticLightsGroup(BeatmapController map, HashMap<Integer, LightObject> lights) {
        super(map, lights);
    }

    @Override
    public void update(float beat, double deltaTime) {

    }
}
