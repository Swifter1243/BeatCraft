package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.beatcraft.common.event.IEvent;

import java.util.function.Function;

public class FloatFxEvent extends BeatmapObject implements IEvent {

    public float fxState;
    public int lightID;
    public float startState;
    public float duration;
    public Function<Float, Float> easing;

    public FloatFxEvent(float beat, float startState, float endState, float duration, int lightID, Function<Float, Float> easing) {
        this.beat = beat;
        this.startState = startState;
        this.fxState = endState;
        this.duration = duration;
        this.lightID = lightID;
        this.easing = easing;
    }


    @Override
    public float getEventBeat() {
        return beat;
    }

    @Override
    public float getEventDuration() {
        return duration;
    }

    public FloatFxEvent extendTo(float beat) {
        return new FloatFxEvent(
            this.beat + duration, fxState, fxState,
            beat - (this.beat + duration), lightID,
            Easing::easeLinear
        );
    }

}
