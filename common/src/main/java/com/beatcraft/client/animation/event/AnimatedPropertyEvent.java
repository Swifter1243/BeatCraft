package com.beatcraft.client.animation.event;

import com.beatcraft.client.animation.pointdefinition.PointDefinition;
import com.beatcraft.client.beatmap.data.event.AnimateTrack;
import com.beatcraft.common.event.IEvent;

public class AnimatedPropertyEvent<T> implements IEvent {
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

    public T getEventData(float normalTime) {
        if (isRepeating()) {
            normalTime = (normalTime * origin.getRepeat()) % 1;
        }

        if (hasEasing()) {
            normalTime = origin.getEasing().apply(normalTime);
        }

        return property.interpolate(normalTime);
    }
}
