package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.event.events.ValueEvent;

public abstract class ActionLightGroupV2 extends LightGroupV2 {
    public abstract void handleEvent(ValueEvent lightEvent, EventGroup eventGroup);
}
