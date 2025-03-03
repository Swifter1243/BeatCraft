package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.ValueEvent;

import java.util.ArrayList;

public class ValueEventHandler extends EventHandler<Integer, ValueEvent> {
    public ValueEventHandler(ArrayList<ValueEvent> events) {
        super(events, 0);
    }

    @Override
    public void onEventInterrupted(ValueEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(ValueEvent event, float normalTime) {

    }

    @Override
    public void onEventPassed(ValueEvent event) {
        state = event.getValue();
    }

    @Override
    public Integer update(float beat) {
        state = -1000000000;
        return super.update(beat);
    }
}
