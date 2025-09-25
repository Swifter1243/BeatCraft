package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.event.events.ValueEvent;
import com.beatcraft.client.lightshow.lights.LightObject;

import java.util.HashMap;

public abstract class ActionLightGroupV2 extends LightGroupV2 {
    public ActionLightGroupV2(BeatmapController map, HashMap<Integer, LightObject> lights) {
        super(map, lights);
    }

    public abstract void handleEvent(ValueEvent lightEvent, EventGroup eventGroup);
}
