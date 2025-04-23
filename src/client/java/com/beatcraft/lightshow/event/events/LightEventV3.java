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

    public float strobeStartFrequency = 0;
    public float strobeStartBrightness = 0;

    public float strobeFrequency = 0;
    public float strobeBrightness = 0;
    public boolean strobeFade = false;

    public LightEventV3(float beat, LightState startState, LightState endState, float duration, int lightID) {
        this.beat = beat;
        this.startState = startState;
        this.lightID = lightID;
        this.lightState = endState;
        this.duration = duration;
    }

    public LightEventV3(
        float beat, LightState startState, LightState endState,
        float duration, int lightID,
        float strobeStartFrequency, float strobeStartBrightness,
        float strobeFrequency, float strobeBrightness, boolean strobeFade
    ) {
        this.beat = beat;
        this.startState = startState;
        this.lightID = lightID;
        this.lightState = endState;
        this.duration = duration;
        this.strobeStartFrequency = strobeStartFrequency;
        this.strobeStartBrightness = strobeStartBrightness;
        this.strobeFrequency = strobeFrequency;
        this.strobeBrightness = strobeBrightness;
        this.strobeFade = strobeFade;
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
            beat, this.lightState, this.lightState,
            0, this.lightID,
            this.strobeFrequency, this.strobeBrightness,
            this.strobeFrequency, this.strobeBrightness, this.strobeFade
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
            ", strobeStartFrequency=" + strobeStartFrequency +
            ", strobeStartBrightness=" + strobeStartBrightness +
            ", strobeFrequency=" + strobeFrequency +
            ", strobeBrightness=" + strobeBrightness +
            ", strobeFade=" + strobeFade +
            '}';
    }
}
