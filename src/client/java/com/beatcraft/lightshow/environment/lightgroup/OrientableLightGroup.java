package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.lightshow.lights.LightObject;

import java.util.HashMap;

public class OrientableLightGroup extends LightGroupV3 {
    public OrientableLightGroup(HashMap<Integer, LightObject> lights) {
        super(lights);
    }



    @Override
    public void update(float beat, double deltaTime) {

    }
}
