package com.beatcraft.event;

import com.beatcraft.utils.MathUtil;

import java.util.ArrayList;

public abstract class EventHandler<D, E extends IEvent<D>> {
    protected final ArrayList<E> events;
    protected final ArrayList<E> upcoming = new ArrayList<>();
    protected final D initialState;
    protected D state;

    public EventHandler(ArrayList<E> elements, D initialState) {
        this.events = elements;
        this.initialState = initialState;
        reset();
    }

    public void reset() {
        state = initialState;
        upcoming.clear();
        upcoming.addAll(events);
    }

    public D seek(float beat) {
        reset();
        return update(beat);
    }

    public abstract void onInsideEvent(E event, float normalTime);
    public abstract void onEventPassed(E event);

    public D update(float beat) {
        // Check new events to process
        while (!upcoming.isEmpty() && upcoming.getFirst().getEventBeat() < beat) {
            E currentEvent = upcoming.getFirst();
            float startBeat = currentEvent.getEventBeat();
            float endBeat = startBeat + currentEvent.getEventDuration();

            if (endBeat > beat) { // Inside event
                float normalTime = MathUtil.inverseLerp(startBeat, endBeat, beat);
                onInsideEvent(currentEvent, normalTime);
                break;
            }
            else { // Passed event
                onEventPassed(currentEvent);
                upcoming.remove(currentEvent);
            }
        }

        return state;
    }
}
