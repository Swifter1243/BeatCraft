package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.event.events.LightEvent;
import it.unimi.dsi.fastutil.Hash;

import java.util.HashMap;
import java.util.List;

public class LightGroupEventHandlerV2 {
    private final LightGroupV2 lightGroupV2;
    private final HashMap<Integer, LightEventHandler> handlers = new HashMap<>();

    public LightGroupEventHandlerV2(LightGroupV2 group, List<LightEvent> events)
    {
        lightGroupV2 = group;
        group.lights.forEach((lightID, light) -> {
            var relevantEvents = events.stream().filter(o -> o.containsLightID(lightID)).toList();
            handlers.put(lightID, new LightEventHandler(relevantEvents));
        });
    }

    public void update(float beat) {
        handlers.forEach((id, handler) -> {
            var state = handler.update(beat);
            lightGroupV2.setLightState(id, state);
        });
    }

    public void reset() {
        handlers.forEach((k, v) -> {
            v.reset();
        });
    }

}
