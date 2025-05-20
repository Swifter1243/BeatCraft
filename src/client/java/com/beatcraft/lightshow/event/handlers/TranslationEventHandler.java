package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.TranslationEvent;
import com.beatcraft.lightshow.lights.TransformState;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class TranslationEventHandler extends EventHandler<TransformState, TranslationEvent> {
    public TranslationEventHandler(List<TranslationEvent> events, TransformState.Axis initialAxis) {
        super(events, new TransformState(initialAxis, 0));
    }

    public void addEvents(List<TranslationEvent> events) {
        this.events.addAll(events);
        this.events.sort(Difficulty::compareObjects);
    }

    @Override
    public void onEventInterrupted(TranslationEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(TranslationEvent event, float normalTime) {
        var start = event.startState;
        var end = event.transformState;
        var easing = event.easing;

        float easedTime = easing.apply(normalTime);

        float interpolatedPosition = MathHelper.lerp(easedTime, start.value, end.value);

        state.set(start.axis, interpolatedPosition);

    }

    @Override
    public void onEventPassed(TranslationEvent event) {
        state.set(event.transformState);
    }
}
