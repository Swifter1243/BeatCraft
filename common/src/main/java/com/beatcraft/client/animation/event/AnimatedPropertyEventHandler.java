package com.beatcraft.client.animation.event;

import com.beatcraft.common.event.EventHandler;

import java.util.ArrayList;

public class AnimatedPropertyEventHandler<T> extends EventHandler<T, AnimatedPropertyEvent<T>> {
    public AnimatedPropertyEventHandler(ArrayList<AnimatedPropertyEvent<T>> elements, T initialState) {
        super(elements, initialState);
    }

    @Override
    public void onEventInterrupted(AnimatedPropertyEvent<T> event, float normalTime) {
        state = event.getEventData(normalTime);
    }

    @Override
    public void onInsideEvent(AnimatedPropertyEvent<T> event, float normalTime) {
        state = event.getEventData(normalTime);
    }

    @Override
    public void onEventPassed(AnimatedPropertyEvent<T> event) {
        state = event.getEventData(1);
    }
}