package com.beatcraft.client.lightshow.event.handlers;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.common.event.EventHandler;
import com.beatcraft.client.lightshow.event.events.FloatFxEvent;
import net.minecraft.util.Mth;

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
        state = Mth.lerp(easedTime, event.startState, event.fxState);
    }

    @Override
    public void onEventPassed(FloatFxEvent event) {
        state = event.fxState;
    }
}
