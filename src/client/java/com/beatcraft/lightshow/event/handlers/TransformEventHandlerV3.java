package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.event.VoidEventHandler;
import com.beatcraft.lightshow.event.events.TransformEvent;

import java.util.List;

public class TransformEventHandlerV3 extends VoidEventHandler<TransformEvent> {
    public TransformEventHandlerV3(List<TransformEvent> events) {
        super(events);
    }

    @Override
    public void onEventInterrupted(TransformEvent event, float normalTime) {

    }

    @Override
    public void onInsideEvent(TransformEvent event, float normalTime) {

    }

    @Override
    public void onEventPassed(TransformEvent event) {

    }
}
