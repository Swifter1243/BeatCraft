package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.animation.Easing;
import com.beatcraft.common.event.IEvent;
import com.beatcraft.client.lightshow.lights.LightState;

import java.util.function.Function;

public class LightEventV3 extends LightEvent implements IEvent {
    public LightState lightState;
    public int lightID;
    public LightState startState;
    public float duration;
    public Function<Float, Float> easing;

    public LightEventV3(float beat, LightState startState, LightState endState, float duration, int lightID, Function<Float, Float> easing) {
        this.beat = beat;
        this.startState = startState;
        this.lightID = lightID;
        this.lightState = endState;
        this.duration = duration;
        this.easing = easing;
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

    public LightEventV3 extendTo(float beat) {
        return new LightEventV3(
            this.beat + duration, this.lightState, this.lightState,
            beat - (this.beat + duration), this.lightID,
            Easing::easeLinear
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
