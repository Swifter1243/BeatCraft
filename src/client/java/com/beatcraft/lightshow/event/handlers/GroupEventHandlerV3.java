package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.BeatCraft;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupEventHandlerV3 {
    private final LightGroupV3 lightGroupV3;
    private final HashMap<Integer, LightEventHandlerV3> lightHandlers = new HashMap<>();
    private final HashMap<Integer, HashMap<TransformState.Axis, TransformEventHandlerV3>> transformHandlers = new HashMap<>();


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

    public void linkTransformEvents(int lightID, HashMap<TransformState.Axis, List<TransformEvent>> transformEvents) {
        if (!lightGroupV3.lights.containsKey(lightID)) return;
        if (!transformHandlers.containsKey(lightID)) {
            transformHandlers.put(lightID, new HashMap<>());
        }
        transformEvents.forEach((axis, events) -> {
            var relevantEvents = events.stream().filter(o -> o.containsLightID(lightID)).toList();

            var axes = transformHandlers.get(lightID);
            if (axes.containsKey(axis)) {
                axes.get(axis).addEvents(relevantEvents);
            } else {
                axes.put(axis, new TransformEventHandlerV3(relevantEvents, axis));
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
