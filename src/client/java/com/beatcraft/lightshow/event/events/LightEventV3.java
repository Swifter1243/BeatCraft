package com.beatcraft.lightshow.event.events;

import com.beatcraft.animation.Easing;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.LightState;

import java.util.function.Function;

public class LightEventV3 extends LightEvent implements IEvent {
    public LightState lightState;
    public int lightID;
    public LightState startState;
    public float duration;

    public LightEventV3(float beat, LightState startState, LightState endState, float duration, int lightID) {
        this.beat = beat;
        this.startState = startState;
        this.lightID = lightID;
        this.lightState = endState;
        this.duration = duration;
    }

    @Override
    public boolean containsLightID(int lightID) {
        return this.lightID == lightID;
    }

    @Override
    public float getEventBeat() {
        return beat;
    }

    @Override
    public float getEventDuration() {
        return duration;
    }

    public LightEventV3 extendTo(float beat, float duration) {
        return new LightEventV3(
            beat, this.lightState, this.lightState,
            duration, this.lightID
        );
    }

    @Override
    public String toString() {
        return "LightEventV3{" +
            "beat=" + beat +
            ", lightState=" + lightState +
            ", lightID=" + lightID +
            ", startState=" + startState +
            ", duration=" + duration +
            '}';
    }
}
