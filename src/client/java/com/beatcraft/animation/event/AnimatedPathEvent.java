package com.beatcraft.animation.event;

import com.beatcraft.animation.pointdefinition.PointDefinition;
import com.beatcraft.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.event.IEvent;

public class AnimatedPathEvent<T> implements IEvent {
    private final PointDefinition<T> property;
    private final AssignPathAnimation origin;

    public AnimatedPathEvent(PointDefinition<T> property, AssignPathAnimation origin) {
        this.property = property;
        this.origin = origin;
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
        return origin.getDuration();
    }

    public float applyEasing(float normalTime) {
        if (hasEasing()) {
            return origin.getEasing().apply(normalTime);
        } else {
            return normalTime;
        }
    }

    public PointDefinition<T> getProperty() {
        return property;
    }
}
