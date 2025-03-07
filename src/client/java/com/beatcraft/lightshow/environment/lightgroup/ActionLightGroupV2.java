package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;

import java.util.HashMap;

public abstract class ActionLightGroupV2 extends LightGroupV2 {
    public ActionLightGroupV2(HashMap<Integer, LightObject> lights) {
        super(lights);
    }

    public abstract void handleEvent(ValueEvent lightEvent, EventGroup eventGroup);
}
