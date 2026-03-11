package com.beatcraft.client.lightshow.environment.thefirst;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.client.lightshow.event.events.ValueEvent;
import com.beatcraft.client.lightshow.lights.LightObject;
import org.joml.Vector3f;

import java.util.HashMap;

public class TheFirstRingLights extends ActionLightGroupV2 {

    protected static HashMap<Integer, LightObject> buildRingLights(BeatmapController beatmap) {
        var map = new HashMap<Integer, LightObject>();

        var pos = new Vector3f(0, 0, 8);

        return map;
    }

    public TheFirstRingLights(BeatmapController map) {
        super(map, buildRingLights(map));
    }

    @Override
    public void handleEvent(ValueEvent lightEvent, EventGroup eventGroup) {

    }

    @Override
    public void update(float beat, double deltaTime) {

    }
}
