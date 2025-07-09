package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.event.events.LightEventV2;

import java.util.HashMap;
import java.util.List;

public class LightGroupEventHandlerV2 {
    private final LightGroupV2 lightGroupV2;
    private final HashMap<Integer, LightEventHandlerV2> handlers = new HashMap<>();

    public LightGroupEventHandlerV2(LightGroupV2 group, List<LightEventV2> events) {
        lightGroupV2 = group;
        group.lights.forEach((lightID, light) -> {
            var relevantEvents = events.stream().filter(o -> o.containsLightID(lightID)).toList();
            handlers.put(lightID, new LightEventHandlerV2(relevantEvents));
        });
    }

    public void seek(float beat) {
        handlers.forEach((id, handler) -> {
            handler.seek(beat);
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
