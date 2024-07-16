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

    public abstract void onEventInterrupted(E event, float normalTime);
    public abstract void onInsideEvent(E event, float normalTime);
    public abstract void onEventPassed(E event);

    private void handleEventInterrupted(float startBeat, float endBeat, E currentEvent) {
        E interruptingEvent = upcoming.get(1);
        float interruptBeat = interruptingEvent.getEventBeat();
        float normalTime = MathUtil.inverseLerp(startBeat, endBeat, interruptBeat);
        onEventInterrupted(currentEvent, normalTime);
        upcoming.remove(currentEvent);
    }

    private void handleEventPassed(E currentEvent) {
        onEventPassed(currentEvent);
        upcoming.remove(currentEvent);
    }

    private boolean handleInsideEvent(float beat, float startBeat, float endBeat, E currentEvent) {
        if (upcoming.size() >= 2 && upcoming.get(1).getEventBeat() < beat) { // Event interrupted
            handleEventInterrupted(startBeat, endBeat, currentEvent);
            return false;
        }

        float normalTime = MathUtil.inverseLerp(startBeat, endBeat, beat);
        onInsideEvent(currentEvent, normalTime);
        return true;
    }

    public D update(float beat) {
        // Check new events to process
        while (!upcoming.isEmpty() && upcoming.getFirst().getEventBeat() < beat) {
            E currentEvent = upcoming.getFirst();
            float startBeat = currentEvent.getEventBeat();
            float endBeat = startBeat + currentEvent.getEventDuration();

            if (endBeat > beat) { // Inside event
                if (handleInsideEvent(beat, startBeat, endBeat, currentEvent)) {
                    break;
                }
            }
            else { // Passed event
                handleEventPassed(currentEvent);
            }
        }

        return state;
    }
}
