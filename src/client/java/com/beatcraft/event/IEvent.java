package com.beatcraft.event;

public interface IEvent<D> {
    float getEventBeat();
    float getEventDuration();
    D getEventData(float normalTime);
}
