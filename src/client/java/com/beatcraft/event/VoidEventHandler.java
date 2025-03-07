package com.beatcraft.event;

import com.beatcraft.utils.MathUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class VoidEventHandler<E extends IEvent> {
    private final List<E> events;
    private final List<E> upcoming = new ArrayList<>();

    public VoidEventHandler(List<E> events) {
        this.events = events;
        reset();
    }

    public void reset() {
        upcoming.clear();
        upcoming.addAll(events);
    }

    public void seek(float beat) {
        reset();
        update(beat);
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

    public void update(float beat) {
        // Check new events to process
        while (!upcoming.isEmpty() && upcoming.get(0).getEventBeat() < beat) {
            E currentEvent = upcoming.get(0);
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
    }

    public List<E> getEvents() {
        return events;
    }
}
