package com.beatcraft.client.animation.event;

import com.beatcraft.client.animation.pointdefinition.PointDefinition;
import com.beatcraft.common.event.EventHandler;

import java.util.ArrayList;

public class AnimatedPathEventHandler<T> extends EventHandler<Path<T>, AnimatedPathEvent<T>> {
    private PointDefinition<T> previousProperty = null;

    public AnimatedPathEventHandler(ArrayList<AnimatedPathEvent<T>> events, Path<T> initialState) {
        super(events, initialState);
    }

    @Override
    public void reset() {
        super.reset();
        previousProperty = null;
    }

    @Override
    public void onEventInterrupted(AnimatedPathEvent<T> event, float normalTime) {
        previousProperty = event.getProperty();
    }

    @Override
    public void onInsideEvent(AnimatedPathEvent<T> event, float normalTime) {
        if (previousProperty != null) {
            normalTime = event.applyEasing(normalTime);
            state = new InterpolatedPath<>(previousProperty, event.getProperty(), normalTime);
        } else {
            state = new StaticPath<>(event.getProperty());
        }
    }

    @Override
    public void onEventPassed(AnimatedPathEvent<T> event) {
        state = new StaticPath<>(event.getProperty());
        previousProperty = event.getProperty();
    }
}