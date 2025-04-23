package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.event.events.LightEventV3;

import java.util.HashMap;
import java.util.List;

public class GroupEventHandlerV3 {
    private final LightGroupV3 lightGroupV3;
    private final HashMap<Integer, LightEventHandlerV3> lightHandlers = new HashMap<>();
    private final HashMap<Integer, HashMap<Integer, TransformEventHandlerV3>> transformHandlers = new HashMap<>();


    public GroupEventHandlerV3(LightGroupV3 group) {
        lightGroupV3 = group;
    }

    public void linkLightEvents(List<LightEventV3> lightEvents) {
        lightGroupV3.lights.forEach((lightID, light) -> {
            var relevantEvents = lightEvents.stream().filter(o -> o.containsLightID(lightID)).toList();
            if (lightHandlers.containsKey(lightID)) {
                lightHandlers.get(lightID).addEvents(relevantEvents);
            } else {
                lightHandlers.put(lightID, new LightEventHandlerV3(relevantEvents));
            }
        });

    }

    public void update(float beat) {
        lightHandlers.forEach((id, handler) -> {
            var state = handler.update(beat);
            lightGroupV3.setLightState(id, state);
        });

        transformHandlers.forEach((id, axisHandlers) -> {
            axisHandlers.forEach((axis, handler) -> {
                var state = handler.update(beat);
                lightGroupV3.setTransform(id, state);
            });
        });

    }

    public void reset() {
        lightHandlers.forEach((k, v) -> {
            v.reset();
        });
        transformHandlers.forEach((k, v) -> {
            v.forEach((k2, v2) -> {
                v2.reset();
            });
        });
    }

    public void clear() {
        lightHandlers.forEach((k, v) -> v.clear());
        transformHandlers.forEach((k, v) -> v.clear());
    }
}
