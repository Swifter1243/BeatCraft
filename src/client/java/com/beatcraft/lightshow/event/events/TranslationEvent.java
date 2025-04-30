package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.function.Function;

public class TranslationEvent extends BeatmapObject implements IEvent {

    public TransformState transformState;
    public int lightID;
    public TransformState startState;
    public float duration;
    public Function<Float, Float> easing;

    public TranslationEvent(float beat, TransformState startState, TransformState endState, float duration, int lightID, Function<Float, Float> easing) {
        this.beat = beat;
        this.startState = startState;
        this.transformState = endState;
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

    public TranslationEvent extendTo(float beat, float duration) {
        return new TranslationEvent(
            beat, transformState, transformState,
            duration, lightID, easing
        );
    }

}
