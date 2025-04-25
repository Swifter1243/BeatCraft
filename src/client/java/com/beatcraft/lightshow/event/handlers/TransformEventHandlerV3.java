package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.List;

public class TransformEventHandlerV3 extends EventHandler<TransformState, TransformEvent> {
    public TransformEventHandlerV3(List<TransformEvent> events, TransformState.Axis initialAxis) {
        super(events, new TransformState(initialAxis, 0));

    }

    public void addEvents(List<TransformEvent> events) {
        this.events.addAll(events);
        this.events.sort(Difficulty::compareObjects);
    }

    @Override
    public void onEventInterrupted(TransformEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(TransformEvent event, float normalTime) {
        var start = event.startState;
        var end = event.transformState;
        var loops = event.loops;
        var direction = event.direction;
        var easing = event.easing;

        float easedTime = easing.apply(normalTime);

        float startRotation = start.value;
        float endRotation = end.value;

        startRotation = ((startRotation % 360) + 360) % 360;
        endRotation = ((endRotation % 360) + 360) % 360;

        float rotationDiff;

        if (direction == 0) {
            float clockwiseDiff = (endRotation - startRotation + 360) % 360;
            float counterClockwiseDiff = (startRotation - endRotation + 360) % 360;
            rotationDiff = (clockwiseDiff <= counterClockwiseDiff) ? clockwiseDiff : -counterClockwiseDiff;
        } else if (direction == 1) {
            rotationDiff = (endRotation - startRotation + 360) % 360;
        } else {
            rotationDiff = -((startRotation - endRotation + 360) % 360);
        }

        rotationDiff += loops * 360 * (rotationDiff >= 0 ? 1 : -1);

        float interpolatedRotation = startRotation + rotationDiff * easedTime;

        state.set(start.axis, interpolatedRotation);
    }

    @Override
    public void onEventPassed(TransformEvent event) {
        state.set(event.transformState);
    }
}
