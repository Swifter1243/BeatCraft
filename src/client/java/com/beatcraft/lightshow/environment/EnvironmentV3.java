package com.beatcraft.lightshow.environment;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.event.EventBuilderV3;
import com.beatcraft.lightshow.event.Filter;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEvent;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EnvironmentV3 extends Environment {


    private Random random = new Random();

    public void loadLightshow(Difficulty difficulty, JsonObject json) {

        var version = json.get("version").getAsString().split("\\.")[0];

        if (version.equals("3")) {
            loadV3(difficulty, json);
        } else if (version.equals("4")) {
            loadV4(difficulty, json);
        }
    }

    protected abstract int getLightCount(int group);
    protected abstract void linkEvents(int group, int lightID, List<LightEventV3> lightEvents, HashMap<TransformState.Axis, List<RotationEvent>> transformEvents);

    private void preProcessLightEventsV3(EventBuilderV3 builder, JsonArray rawLightEvents) {
        rawLightEvents.forEach(rawBoxGroup -> {
            var boxGroupData = rawBoxGroup.getAsJsonObject();

            var baseBeat = JsonUtil.getOrDefault(boxGroupData, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupData, "g", JsonElement::getAsInt, 0);

            var coveredIDs = new ArrayList<Integer>();
            var lightCount = getLightCount(group);
            var rawEventLanes = boxGroupData.getAsJsonArray("e");

            rawEventLanes.forEach(rawEventLane -> {
                var eventLaneData = rawEventLane.getAsJsonObject();

                var rawFilter = eventLaneData.getAsJsonObject("f");
                var beatDistributionValue = JsonUtil.getOrDefault(eventLaneData, "w", JsonElement::getAsFloat, 1f);
                var beatDistributionType = JsonUtil.getOrDefault(eventLaneData, "d", JsonElement::getAsInt, 0);

                var brightnessDistributionValue = JsonUtil.getOrDefault(eventLaneData, "r", JsonElement::getAsFloat, 1f);
                var brightnessDistributionType = JsonUtil.getOrDefault(eventLaneData, "t", JsonElement::getAsInt, 0);
                boolean affectsFirst = JsonUtil.getOrDefault(eventLaneData, "b", JsonElement::getAsInt, 0) > 0;
                var rawBrightnessEasing = JsonUtil.getOrDefault(eventLaneData, "i", JsonElement::getAsInt, 0);

                var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);
                var brightnessEasing = Easing.getEasing(String.valueOf(rawBrightnessEasing));

                var baseData = new EventBuilderV3.BaseLightData(
                    baseBeat, group, filter, beatDistributionType, beatDistributionValue,
                    brightnessDistributionType, brightnessDistributionValue, brightnessEasing,
                    affectsFirst
                );

                var rawLightSubEvents = eventLaneData.getAsJsonArray("e");

                AtomicBoolean isFirst = new AtomicBoolean(true);
                rawLightSubEvents.forEach(rawSubEvent -> {
                    var eventData = rawSubEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                    var transitionType = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);
                    var color = JsonUtil.getOrDefault(eventData, "c", JsonElement::getAsInt, 0);
                    var brightness = JsonUtil.getOrDefault(eventData, "s", JsonElement::getAsFloat, 0f);
                    var strobeFrequency = JsonUtil.getOrDefault(eventData, "f", JsonElement::getAsFloat, 0f);
                    var strobeBrightness = JsonUtil.getOrDefault(eventData, "sb", JsonElement::getAsFloat, 0f);
                    boolean strobeFade = JsonUtil.getOrDefault(eventData, "sf", JsonElement::getAsInt, 0) > 0;

                    var events = baseData.buildEvents(
                        isFirst.get(),
                        beatOffset, transitionType, color,
                        brightness, strobeFrequency,
                        strobeBrightness, strobeFade
                    );

                    builder.addRawLightEvents(events);

                    isFirst.set(false);
                });

            });
        });
    }

    private void preProcessRotationEventsV3(EventBuilderV3 builder, JsonArray rawRotationEvents) {
        rawRotationEvents.forEach(rawBoxGroup -> {
            var boxGroupData = rawBoxGroup.getAsJsonObject();

            var baseBeat = JsonUtil.getOrDefault(boxGroupData, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupData, "g", JsonElement::getAsInt, 0);

            var coveredIDs = new ArrayList<Integer>();
            var lightCount = getLightCount(group);
            var rawEventLanes = boxGroupData.getAsJsonArray("e");

            rawEventLanes.forEach(rawEventLane -> {
                var eventLaneData = rawEventLane.getAsJsonObject();

                var rawFilter = eventLaneData.getAsJsonObject("f");
                var beatDistributionValue = JsonUtil.getOrDefault(eventLaneData, "w", JsonElement::getAsFloat, 1f);
                var beatDistributionType = JsonUtil.getOrDefault(eventLaneData, "d", JsonElement::getAsInt, 0);

                var rotationDistributionValue = JsonUtil.getOrDefault(eventLaneData, "s", JsonElement::getAsFloat, 0f);
                var rotationDistributionType = JsonUtil.getOrDefault(eventLaneData, "t", JsonElement::getAsInt, 0);
                boolean affectsFirst = JsonUtil.getOrDefault(eventLaneData, "b", JsonElement::getAsInt, 0) > 0;

                var rawAxis = JsonUtil.getOrDefault(eventLaneData, "a", JsonElement::getAsInt, 0);
                boolean invertAxis = JsonUtil.getOrDefault(eventLaneData, "r", JsonElement::getAsInt, 0) > 0;

                var axis = TransformState.Axis.values()[rawAxis % 3];

                var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);

                var baseData = new EventBuilderV3.BaseRotationData(
                    baseBeat, group, filter,
                    beatDistributionType, beatDistributionValue,
                    rotationDistributionType, rotationDistributionValue,
                    axis, invertAxis, affectsFirst
                );

                var rawRotationSubEvents = eventLaneData.getAsJsonArray("l");
                AtomicBoolean isFirst = new AtomicBoolean(true);
                rawRotationSubEvents.forEach(rawSubEvent -> {
                    var eventData = rawSubEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                    var transitionType = JsonUtil.getOrDefault(eventData, "p", JsonElement::getAsInt, 0);
                    var rawEasing = JsonUtil.getOrDefault(eventData, "e", JsonElement::getAsInt, 0);
                    var magnitude = JsonUtil.getOrDefault(eventData, "r", JsonElement::getAsFloat, 0f);
                    var direction = JsonUtil.getOrDefault(eventData, "o", JsonElement::getAsInt, 0);
                    var loopCount = JsonUtil.getOrDefault(eventData, "l", JsonElement::getAsInt, 0);

                    var easing = Easing.getEasing(String.valueOf(rawEasing));

                    var events = baseData.buildEvents(
                        isFirst.get(),
                        beatOffset, transitionType, magnitude,
                        direction, loopCount, easing
                    );

                    builder.addRawRotationEvents(events);

                    isFirst.set(false);
                });

            });

        });
    }

    private void buildLightEventsV3(EventBuilderV3 builder) {

    }

    private void buildRotationEventsV3(EventBuilderV3 builder) {

    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var eventBuilder = new EventBuilderV3();

        preProcessLightEventsV3(eventBuilder, rawColorEventBoxes);
        preProcessRotationEventsV3(eventBuilder, rawRotationEvents);
        eventBuilder.sortEvents();
        buildLightEventsV3(eventBuilder);
        buildRotationEventsV3(eventBuilder);
    }
    /*
    private void loadV3old(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var colorEvents = new HashMap<GroupKey, ArrayList<LightEventV3>>();
        var transformEvents = new HashMap<GroupKey, HashMap<TransformState.Axis, List<RotationEvent>>>();

        // {[group, lightId]: event, ...}
        var latestColorEvents = new HashMap<GroupKey, LightEventV3>();
        var latestTransformEvents = new HashMap<GroupKey, HashMap<TransformState.Axis, RotationEvent>>();

        rawColorEventBoxes.forEach(rawBoxGroup -> {
            var boxGroupObj = rawBoxGroup.getAsJsonObject();
            var baseBeat = JsonUtil.getOrDefault(boxGroupObj, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupObj, "g", JsonElement::getAsInt, 0);
            var rawEventLanes = boxGroupObj.getAsJsonArray("e");
            ArrayList<Integer> coveredIds = new ArrayList<>();
            var lightCount = getLightCount(group);

            rawEventLanes.forEach(rawEventLane -> {
                var subEvent = rawEventLane.getAsJsonObject();

                var rawFilter = subEvent.getAsJsonObject("f");

                var filter = processFilter(lightCount, coveredIds, rawFilter);

                // 0 = step, 1 = wave
                var beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 1f);
                var beatDistributionType = JsonUtil.getOrDefault(subEvent, "d", JsonElement::getAsInt, 0);

                float maxBeat = beatDistributionValue;
                if (beatDistributionType % 2 == 0) {
                    maxBeat *= filter.chunkCount();
                }

                // 0 = step, 1 = wave
                var brightnessDistributionValue = JsonUtil.getOrDefault(subEvent, "r", JsonElement::getAsFloat, 1f);
                var brightnessDistributionType = JsonUtil.getOrDefault(subEvent, "t", JsonElement::getAsInt, 0);

                float maxBrightness0 = brightnessDistributionValue;
                if (brightnessDistributionType % 2 == 0) {
                    maxBrightness0 *= filter.chunkCount();
                }

                // whether distribution affects the first event in the sequence
                boolean affectsFirst = JsonUtil.getOrDefault(subEvent, "b", JsonElement::getAsInt, 0) > 0;

                var brightnessDistributionEasing = JsonUtil.getOrDefault(subEvent, "i", JsonElement::getAsInt, 0);
                var easingFunction = Easing.getEasing(String.valueOf(brightnessDistributionEasing));

                AtomicReference<Boolean> doDistribution = new AtomicReference<>(affectsFirst);

                var rawEvents = subEvent.getAsJsonArray("e");

                float maxDuration = maxBeat;
                float maxBrightness = maxBrightness0;
                AtomicInteger index = new AtomicInteger();
                rawEvents.forEach(rawEvent -> {
                    var eventData = rawEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);

                    // 0 = instant, 1 = transition, 2 = extend
                    var transitionType = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);

                    var color = JsonUtil.getOrDefault(eventData, "c", JsonElement::getAsInt, 0);

                    var brightness = JsonUtil.getOrDefault(eventData, "s", JsonElement::getAsFloat, 1f);

                    var strobeFrequency = JsonUtil.getOrDefault(eventData, "f", JsonElement::getAsFloat, 0f);

                    var strobeBrightness = JsonUtil.getOrDefault(eventData, "sb", JsonElement::getAsFloat, 0f);

                    var strobeFade = JsonUtil.getOrDefault(eventData, "sf", JsonElement::getAsInt, 0) > 0;

                    for (var targetSet : filter) {
                        var targets = targetSet.getA();
                        var durationMod = doDistribution.get() ? targetSet.getB() * maxDuration : 0f;
                        var distributionMod = doDistribution.get() ? targetSet.getC() * maxBrightness : 0f;

                        if (transitionType == 1) { // transition
                            for (var target : targets) {
                                var key = new GroupKey(group, target);
                                var last = latestColorEvents.computeIfAbsent(key, k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k.getLightId()));

                                var curr_start = last.getEventBeat() + last.getEventDuration();// + (index.get() == 0 ? durationMod : 0);
                                var curr_end = baseBeat + beatOffset + durationMod;

                                var curr_brightness = brightness + distributionMod;

                                curr_start = Math.min(curr_end, curr_start);

                                var curr_duration = curr_end - curr_start;

                                var curr_startState = last.lightState.copy();
                                LightState curr_endState;
                                if (color == 0) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentLeftColor().withAlpha(1), curr_brightness);
                                } else if (color == 1) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentRightColor().withAlpha(1), curr_brightness);
                                } else {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentWhiteColor().withAlpha(1), curr_brightness);
                                }

                                curr_endState.setStrobeState(easingFunction, strobeBrightness, strobeFrequency, strobeFade);

                                var currentEvent = new LightEventV3(
                                    curr_start, curr_startState, curr_endState,
                                    curr_duration, target
                                );

                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else if (transitionType == 0) { // instant
                            for (var target : targets) {

                                var curr_end = baseBeat + beatOffset + durationMod;

                                var curr_brightness = brightness;

                                var curr_duration = 0;

                                LightState curr_endState;
                                if (color == 0) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentLeftColor().withAlpha(1), curr_brightness);
                                } else if (color == 1) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentRightColor().withAlpha(1), curr_brightness);
                                } else {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentWhiteColor().withAlpha(1), curr_brightness);
                                }
                                curr_endState.setStrobeState(easingFunction, strobeBrightness, strobeFrequency, strobeFade);

                                var curr_startState = curr_endState;

                                var currentEvent = new LightEventV3(
                                    curr_end, curr_startState, curr_endState,
                                    curr_duration, target
                                );

                                var key = new GroupKey(group, target);
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else { // extend
                            for (var target : targets) {
                                var key = new GroupKey(group, target);
                                var last = latestColorEvents.computeIfAbsent(key, k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k.getLightId()));

                                var currentEvent = last.extendTo(baseBeat + beatOffset + durationMod);

                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        }
                    }
                    index.addAndGet(1);
                    doDistribution.set(true);
                });

            });

        });

        rawRotationEvents.forEach(rawBoxGroup -> {
            var boxGroupObj = rawBoxGroup.getAsJsonObject();
            float baseBeat = JsonUtil.getOrDefault(boxGroupObj, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupObj, "g", JsonElement::getAsInt, 0);
            var rawEventLanes = boxGroupObj.getAsJsonArray("e");
            HashMap<TransformState.Axis, ArrayList<Integer>> coveredIds = new HashMap<>();
            int lightCount = getLightCount(group);

            rawEventLanes.forEach(rawEventLane -> {
                var subEvent = rawEventLane.getAsJsonObject();

                var rawFilter = subEvent.getAsJsonObject("f");


                int rawAxis = JsonUtil.getOrDefault(subEvent, "a", JsonElement::getAsInt, 5);
                boolean flipAxis = JsonUtil.getOrDefault(subEvent, "r", JsonElement::getAsInt, 0) > 0;

                var axis = TransformState.Axis.values()[rawAxis % 3];

                if (!coveredIds.containsKey(axis)) {
                    coveredIds.put(axis, new ArrayList<>());
                }

                var filter = processFilter(lightCount, coveredIds.get(axis), rawFilter);

                // 0 = step, 1 = wave
                float beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 1f);
                int beatDistributionType = JsonUtil.getOrDefault(subEvent, "d", JsonElement::getAsInt, 0);

                float maxBeat = beatDistributionValue;
                if (beatDistributionType % 2 == 0) {
                    maxBeat *= filter.chunkCount();
                }

                // 0 = step, 1 = wave
                float rotationDistributionValue = JsonUtil.getOrDefault(subEvent, "s", JsonElement::getAsFloat, 1f);
                int rotationDistributionType = JsonUtil.getOrDefault(subEvent, "t", JsonElement::getAsInt, 0);

                float maxRotation0 = rotationDistributionValue;
                if (rotationDistributionType % 2 == 0) {
                    maxRotation0 *= filter.chunkCount();
                }

                // whether distribution affects the first event in the sequence
                boolean affectsFirst = JsonUtil.getOrDefault(subEvent, "b", JsonElement::getAsInt, 0) > 0;

                AtomicReference<Boolean> doDistribution = new AtomicReference<>(affectsFirst);

                var rawEvents = subEvent.getAsJsonArray("l");

                float maxDuration = maxBeat;
                float maxRotation = maxRotation0;
                AtomicInteger index = new AtomicInteger();
                rawEvents.forEach(rawEvent -> {
                    var eventData = rawEvent.getAsJsonObject();

                    float beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);

                    // 0 = transition, 1 = extend
                    int transitionType = JsonUtil.getOrDefault(eventData, "p", JsonElement::getAsInt, 0);

                    int easing = JsonUtil.getOrDefault(eventData, "e", JsonElement::getAsInt, 0);
                    var easingFunction = Easing.getEasing(String.valueOf(easing));

                    float magnitude = JsonUtil.getOrDefault(eventData, "r", JsonElement::getAsFloat, 0f);

                    if (flipAxis) {
                        magnitude = ((-magnitude % 360) + 360) % 360;
                    }

                    // 0 = auto, 1 = cw, 2 = ccw
                    int direction = JsonUtil.getOrDefault(eventData, "o", JsonElement::getAsInt, 0);

                    int loopCount = JsonUtil.getOrDefault(eventData, "l", JsonElement::getAsInt, 0);

                    for (var targetSet : filter) {
                        var targets = targetSet.getA();
                        float durationMod = doDistribution.get() ? targetSet.getB() * maxDuration : 0f;
                        float distributionMod = doDistribution.get() ? targetSet.getC() * maxRotation : 0f;

                        if (transitionType == 0) { // transition

                            for (var target : targets) {
                                var key = new GroupKey(group, target);
                                var last = latestTransformEvents.computeIfAbsent(
                                    key,
                                    k -> new HashMap<>()
                                ).computeIfAbsent(
                                    axis,
                                    a -> new RotationEvent(0, new TransformState(axis, 0), new TransformState(axis, 0), 0, target, easingFunction, 0, 0)
                                );

                                var curr_start = last.getEventBeat() + last.getEventDuration();// + (index.get() == 0 ? durationMod : 0);
                                var curr_end = baseBeat + beatOffset + durationMod;
                                var curr_duration = curr_end - curr_start;

                                var curr_angle = magnitude + distributionMod;

                                var curr_startState = last.transformState.copy();
                                var curr_endState = new TransformState(axis, curr_angle);
                                var currentEvent = new RotationEvent(
                                    curr_start, curr_startState, curr_endState,
                                    curr_duration, target, easingFunction, loopCount, direction
                                );


                                if (!latestTransformEvents.containsKey(key)) {
                                    latestTransformEvents.put(key, new HashMap<>());
                                }
                                var axes = latestTransformEvents.get(key);
                                axes.put(axis, currentEvent);

                                if (!transformEvents.containsKey(key)) {
                                    transformEvents.put(key, new HashMap<>());
                                }
                                var eventAxes = transformEvents.get(key);
                                if (!eventAxes.containsKey(axis)) {
                                    eventAxes.put(axis, new ArrayList<>());
                                }
                                eventAxes.get(axis).add(currentEvent);

                            }

                        } else { // extend
                            for (var target : targets) {
                                var key = new GroupKey(group, target);
                                var last = latestTransformEvents.computeIfAbsent(
                                    key,
                                    k -> new HashMap<>()
                                ).computeIfAbsent(
                                    axis,
                                    a -> new RotationEvent(0, new TransformState(axis, 0), new TransformState(axis, 0), 0, target, easingFunction, 0, 0)
                                );

                                var currentEvent = last.extendTo(baseBeat + beatOffset + durationMod);

                                if (!latestTransformEvents.containsKey(key)) {
                                    latestTransformEvents.put(key, new HashMap<>());
                                }
                                var axes = latestTransformEvents.get(key);
                                axes.put(axis, currentEvent);

                                if (!transformEvents.containsKey(key)) {
                                    transformEvents.put(key, new HashMap<>());
                                }
                                var eventAxes = transformEvents.get(key);
                                if (!eventAxes.containsKey(axis)) {
                                    eventAxes.put(axis, new ArrayList<>());
                                }
                                eventAxes.get(axis).add(currentEvent);

                            }
                        }
                    }
                    index.addAndGet(1);
                    doDistribution.set(true);

                });

            });

        });

        colorEvents.forEach((k, v) -> {
            linkEvents(k.getGroup(), k.getLightId(), v, transformEvents.remove(k));
        });

        transformEvents.forEach((k, v) -> {
            linkEvents(k.getGroup(), k.getLightId(), new ArrayList<>(), v);
        });

    }
    */

    private void loadV4(Difficulty difficulty, JsonObject json) {

    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
    }
}
