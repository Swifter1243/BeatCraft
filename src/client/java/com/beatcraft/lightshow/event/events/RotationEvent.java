package com.beatcraft.lightshow.event.events;

import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.event.IEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.function.Function;

public class RotationEvent extends BeatmapObject implements IEvent {

    public TransformState transformState;
    public int lightID;
    public TransformState startState;
    public float duration;
    public Function<Float, Float> easing;
    public int loops;
    public int direction;

    public RotationEvent(float beat, TransformState startState, TransformState endState, float duration, int lightID, Function<Float, Float> easing, int loops, int direction) {
        this.beat = beat;
        this.startState = startState;
        this.transformState = endState;
        this.duration = duration;
        this.lightID = lightID;
        this.easing = easing;
        this.loops = loops;
        this.direction = direction;
    }

    public boolean containsLightID(int id) {
        return lightID == id;
    }

    public RotationEvent extendTo(float beat) {
        return new RotationEvent(
            beat, transformState, transformState,
            0, lightID, easing,
            0, 0
        );
    }

    @Override
    public float getEventBeat() {
        return beat;
    }

    @Override
    public float getEventDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "TransformEvent{" +
            "beat=" + beat +
            ", easing=" + easing +
            ", duration=" + duration +
            ", startState=" + startState +
            ", lightID=" + lightID +
            ", transformState=" + transformState +
            '}';
    }
}
