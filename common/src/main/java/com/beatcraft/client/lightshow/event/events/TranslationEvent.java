package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.beatcraft.common.event.IEvent;
import com.beatcraft.client.lightshow.lights.TransformState;

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

    public TranslationEvent extendTo(float beat) {
        return new TranslationEvent(
            this.beat + duration, transformState, transformState,
            beat - (this.beat + duration), lightID, easing
        );
    }

}
