package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.event.EventHandler;
import com.beatcraft.event.VoidEventHandler;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.List;

public class TransformEventHandlerV3 extends EventHandler<TransformState, TransformEvent> {
    public TransformEventHandlerV3(List<TransformEvent> events) {
        super(events, new TransformState(TransformState.Axis.UNKNOWN, 0));

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
