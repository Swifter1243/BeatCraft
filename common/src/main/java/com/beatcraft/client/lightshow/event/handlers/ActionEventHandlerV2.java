package com.beatcraft.client.lightshow.event.handlers;

import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.common.event.VoidEventHandler;
import com.beatcraft.client.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.client.lightshow.event.events.ValueEvent;

import java.util.List;

public class ActionEventHandlerV2<E extends ValueEvent> extends VoidEventHandler<E> {
    private final ActionLightGroupV2 lightGroup;
    private final EventGroup eventGroup;

    public ActionEventHandlerV2(ActionLightGroupV2 lightGroup, List<E> events, EventGroup eventGroup) {
        super(events);
        this.lightGroup = lightGroup;
        this.eventGroup = eventGroup;
    }

    @Override
    public void onEventInterrupted(E event, float normalTime) {
        // will never happen
    }

    @Override
    public void onInsideEvent(E event, float normalTime) {
        // will never happen
    }

    @Override
    public void onEventPassed(E event) {
        lightGroup.handleEvent(event, eventGroup);
    }
}
