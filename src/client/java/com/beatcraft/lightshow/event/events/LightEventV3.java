package com.beatcraft.lightshow.event.events;

import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.LightState;

public class LightEventV3 extends LightEvent implements IEvent {
    private LightState lightState;
    private int[] lightIDs;
    private LightState startState = null;
    private float duration;



    @Override
    public boolean containsLightID(int lightID) {
        if (lightIDs == null) return true;

        for (int id : lightIDs) {
            if (id == lightID) return true;
        }
        return false;
    }

    @Override
    public float getEventBeat() {
        return getBeat();
    }

    @Override
    public float getEventDuration() {
        return duration;
    }
}
