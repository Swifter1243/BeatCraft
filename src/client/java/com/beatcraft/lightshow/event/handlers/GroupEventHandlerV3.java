package com.beatcraft.lightshow.event.handlers;

import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEventV3;
import com.beatcraft.lightshow.event.events.TranslationEvent;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupEventHandlerV3 {
    private final LightGroupV3 lightGroupV3;
    private final HashMap<Integer, LightEventHandlerV3> lightHandlers = new HashMap<>();
    private final HashMap<Integer, HashMap<TransformState.Axis, RotationEventHandlerV3>> rotationHandlers = new HashMap<>();
    private final HashMap<Integer, HashMap<TransformState.Axis, TranslationEventHandler>> translationHandlers = new HashMap<>();


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

    public void linkRotationEvents(int lightID, HashMap<TransformState.Axis, ArrayList<RotationEventV3>> rotationEvents) {
        if (!lightGroupV3.lights.containsKey(lightID)) return;
        if (!rotationHandlers.containsKey(lightID)) {
            rotationHandlers.put(lightID, new HashMap<>());
        }
        if (rotationEvents != null) {
            rotationEvents.forEach((axis, events) -> {
                var axes = rotationHandlers.get(lightID);
                if (axes.containsKey(axis)) {
                    axes.get(axis).addEvents(events);
                } else {
                    axes.put(axis, new RotationEventHandlerV3(events, axis));
                }
            });
        }
    }

    public void linkTranslationEvents(int lightID, HashMap<TransformState.Axis, ArrayList<TranslationEvent>> translationEvents) {
        if (!lightGroupV3.lights.containsKey(lightID)) return;
        if (!translationHandlers.containsKey(lightID)) {
            translationHandlers.put(lightID, new HashMap<>());
        }
        if (translationEvents != null) {
            translationEvents.forEach((axis, events) -> {
                var axes = translationHandlers.get(lightID);
                if (axes.containsKey(axis)) {
                    axes.get(axis).addEvents(events);
                } else {
                    axes.put(axis, new TranslationEventHandler(events, axis));
                }
            });
        }
    }

    public void update(float beat) {
        lightHandlers.forEach((id, handler) -> {
            var state = handler.update(beat);
            lightGroupV3.setLightState(id, state);
        });

        rotationHandlers.forEach((id, axisHandlers) -> {
            axisHandlers.forEach((axis, handler) -> {
                var state = handler.update(beat);
                lightGroupV3.setTransform(id, state);
            });
        });

        translationHandlers.forEach((id, axisHandlers) -> {
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
        rotationHandlers.forEach((k, v) -> {
            v.forEach((k2, v2) -> {
                v2.reset();
            });
        });
        translationHandlers.forEach((k, v) -> {
            v.forEach((k2, v2) -> {
                v2.reset();
            });
        });
    }

    public void clear() {
        lightHandlers.clear();
        rotationHandlers.clear();
        translationHandlers.clear();
    }
}
