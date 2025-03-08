package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.event.VoidEventHandler;
import com.beatcraft.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.lightshow.event.events.ValueEvent;

import java.util.List;

public class ActionEventHandlerV2 extends VoidEventHandler<ValueEvent> {
    private final ActionLightGroupV2 lightGroup;
    private final EventGroup eventGroup;

    public ActionEventHandlerV2(ActionLightGroupV2 lightGroup, List<ValueEvent> events, EventGroup eventGroup) {
        super(events);
        this.lightGroup = lightGroup;
        this.eventGroup = eventGroup;
    }

    @Override
    public void onEventInterrupted(ValueEvent event, float normalTime) {
        // will never happen
    }

    @Override
    public void onInsideEvent(ValueEvent event, float normalTime) {
        // will never happen
    }

    @Override
    public void onEventPassed(ValueEvent event) {
        lightGroup.handleEvent(event, eventGroup);
    }
}
