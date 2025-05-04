package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.FloatFxEvent;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class FloatFxEventHandler extends EventHandler<Float, FloatFxEvent> {
    public FloatFxEventHandler(List<FloatFxEvent> events) {
        super(events, 0f);
    }

    public void addEvents(List<FloatFxEvent> events) {
        this.events.addAll(events);
        this.events.sort(Difficulty::compareObjects);
        reset();
    }

    public void clear() {
        this.events.clear();
    }

    @Override
    public void onEventInterrupted(FloatFxEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(FloatFxEvent event, float normalTime) {
        float easedTime = event.easing.apply(normalTime);
        state = MathHelper.lerp(easedTime, event.startState, event.fxState);
    }

    @Override
    public void onEventPassed(FloatFxEvent event) {
        state = event.fxState;
    }
}
