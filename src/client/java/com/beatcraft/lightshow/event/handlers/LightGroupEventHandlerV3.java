package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.event.events.LightEventV2;

import java.util.HashMap;
import java.util.List;

public class LightGroupEventHandlerV3 {
    private final LightGroupV3 lightGroupV3;
    private final HashMap<Integer, LightEventHandlerV2> handlers = new HashMap<>();

    public LightGroupEventHandlerV3(LightGroupV3 group, List<LightEventV2> events) {
        lightGroupV3 = group;

        group.lights.forEach((lightID, light) -> {
            var relevantEvents = events.stream().filter(o -> o.containsLightID(lightID)).toList();
            handlers.put(lightID, new LightEventHandlerV2(relevantEvents));
        });

    }

    public void update(float beat) {
        handlers.forEach((id, handler) -> {
            var state = handler.update(beat);
            lightGroupV3.setLightState(id, state);
        });
    }

    public void reset() {
        handlers.forEach((k, v) -> {
            v.reset();
        });
    }
}
