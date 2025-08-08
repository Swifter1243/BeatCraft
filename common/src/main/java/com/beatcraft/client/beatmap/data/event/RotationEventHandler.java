package com.beatcraft.client.beatmap.data.event;

import com.beatcraft.common.event.EventHandler;

import java.util.ArrayList;

public class RotationEventHandler extends EventHandler<Float, RotationEvent> {
    public RotationEventHandler(ArrayList<RotationEvent> elements) {
        super(elements, 0F);
    }

    @Override
    public void onEventInterrupted(RotationEvent event, float normalTime) {
        // Rotation events are instant
    }

    @Override
    public void onInsideEvent(RotationEvent event, float normalTime) {
        // Rotation events are instant
    }

    @Override
    public void onEventPassed(RotationEvent event) {
        state += event.getRotation();
    }
}