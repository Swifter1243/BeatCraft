package com.beatcraft.event;

import com.beatcraft.animation.pointdefinition.PointDefinition;
import com.beatcraft.beatmap.data.AnimateTrack;

public class AnimatedPropertyEvent<T> implements IEvent<T> {
    private final PointDefinition<T> property;
    private final AnimateTrack origin;

    public AnimatedPropertyEvent(PointDefinition<T> property, AnimateTrack origin) {
        this.property = property;
        this.origin = origin;
    }

    private boolean isRepeating() {
        return origin.getRepeat() != null;
    }

    private boolean hasEasing() {
        return origin.getEasing() != null;
    }

    @Override
    public float getEventBeat() {
        return origin.getBeat();
    }

    @Override
    public float getEventDuration() {
        return origin.getRepeatedDuration();
    }

    @Override
    public T getEventData(float normalTime) {
        if (hasEasing()) { // tbh idk if the easing is applied before or after the repeat. guess we'll find out!
            normalTime = origin.getEasing().apply(normalTime);
        }

        if (isRepeating()) {
            normalTime = (normalTime * origin.getRepeat()) % 1;
        }

        return property.interpolate(normalTime);
    }
}
