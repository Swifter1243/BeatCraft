package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.lights.LightState;

import java.util.List;

public class LightEventHandlerV3 extends EventHandler<LightState, LightEventV3> {


    public LightEventHandlerV3(List<LightEventV3> events, LightState initialState) {
        super(events, initialState);
    }

    @Override
    public void onEventInterrupted(LightEventV3 event, float normalTime) {

    }

    @Override
    public void onInsideEvent(LightEventV3 event, float normalTime) {

    }

    @Override
    public void onEventPassed(LightEventV3 event) {

    }
}
