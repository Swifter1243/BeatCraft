package com.beatcraft.client.lightshow.event.events;

import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.beatcraft.common.event.IEvent;
import com.beatcraft.client.lightshow.lights.TransformState;

import java.util.function.Function;

public class RotationEventV3 extends BeatmapObject implements IEvent {

    public TransformState transformState;
    public int lightID;
    public TransformState startState;
    public float duration;
    public Function<Float, Float> easing;
    public int loops;
    public int direction;

    public RotationEventV3(float beat, TransformState startState, TransformState endState, float duration, int lightID, Function<Float, Float> easing, int loops, int direction) {
        this.beat = beat;
        this.startState = startState;
        this.transformState = endState;
        this.duration = duration;
        this.lightID = lightID;
        this.easing = easing;
        this.loops = loops;
        this.direction = direction;
    }

    public RotationEventV3 extendTo(float beat) {
        return new RotationEventV3(
            this.beat + duration, transformState, transformState,
            beat - (this.beat + duration), lightID, easing,
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
