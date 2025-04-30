package com.beatcraft.lightshow.environment;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.event.EventBuilderV3;
import com.beatcraft.lightshow.event.Filter;
import com.beatcraft.lightshow.event.events.ColorBoostEvent;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEventV3;
import com.beatcraft.lightshow.event.events.TranslationEvent;
import com.beatcraft.lightshow.event.handlers.ColorBoostEventHandler;
import com.beatcraft.lightshow.lights.LightState;
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

    protected ColorBoostEventHandler boostEventHandler;

    private Random random = new Random();

    public void loadLightshow(Difficulty difficulty, JsonObject json) {

        var version = json.get("version").getAsString().split("\\.")[0];

        if (version.equals("3")) {
            loadV3(difficulty, json);
        } else if (version.equals("4")) {
            loadV4(difficulty, json);
        }
    }

    protected abstract int getGroupCount();
    protected abstract int getLightCount(int group);
    protected abstract void linkEvents(
        int group, int lightID,
        List<LightEventV3> lightEvents,
        HashMap<TransformState.Axis,ArrayList<RotationEventV3>> rotationEvents,
        HashMap<TransformState.Axis,ArrayList<TranslationEvent>> translationEvents
    );

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
                var rawBrightnessEasing = JsonUtil.getOrDefault(eventLaneData, "group", JsonElement::getAsInt, 0);

                var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);
                var brightnessEasing = Easing.getEasing(String.valueOf(rawBrightnessEasing));

                //builder.applyLightEventBeatCutoff(group, baseBeat, filter);

                var baseData = new EventBuilderV3.BaseLightData(
                    baseBeat, group, lightCount, filter,
                    (beatDistributionType % 2), beatDistributionValue,
                    (brightnessDistributionType % 2), brightnessDistributionValue, brightnessEasing,
                    affectsFirst
                );

                var rawLightSubEvents = eventLaneData.getAsJsonArray("e");

                AtomicBoolean isFirst = new AtomicBoolean(true);
                rawLightSubEvents.forEach(rawSubEvent -> {
                    var eventData = rawSubEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                    var transitionType = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);
                    var color = JsonUtil.getOrDefault(eventData, "c", JsonElement::getAsInt, 0);
                    var brightness = JsonUtil.getOrDefault(eventData, "s", JsonElement::getAsFloat, 1f);
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

            var coveredIDs = new HashMap<TransformState.Axis, ArrayList<Integer>>();
            var lightCount = getLightCount(group);
            var rawEventLanes = boxGroupData.getAsJsonArray("e");


            rawEventLanes.forEach(rawEventLane -> {
                var eventLaneData = rawEventLane.getAsJsonObject();

                var rawFilter = eventLaneData.getAsJsonObject("f");
                var beatDistributionValue = JsonUtil.getOrDefault(eventLaneData, "w", JsonElement::getAsFloat, 0f);
                var beatDistributionType = JsonUtil.getOrDefault(eventLaneData, "d", JsonElement::getAsInt, 0);

                var rotationDistributionValue = JsonUtil.getOrDefault(eventLaneData, "s", JsonElement::getAsFloat, 0f);
                var rotationDistributionType = JsonUtil.getOrDefault(eventLaneData, "t", JsonElement::getAsInt, 0);
                boolean affectsFirst = JsonUtil.getOrDefault(eventLaneData, "b", JsonElement::getAsInt, 0) > 0;

                var distributionEasing = JsonUtil.getOrDefault(eventLaneData, "i", JsonElement::getAsInt, 0);
                var rotationEasing = Easing.getEasing(String.valueOf(distributionEasing));

                var rawAxis = JsonUtil.getOrDefault(eventLaneData, "a", JsonElement::getAsInt, 0);
                boolean invertAxis = JsonUtil.getOrDefault(eventLaneData, "r", JsonElement::getAsInt, 0) > 0;

                var axis = TransformState.Axis.values()[rawAxis % 3];

                var covered = coveredIDs.computeIfAbsent(axis, k -> new ArrayList<>());
                var filter = Filter.processFilter(random, lightCount, covered, rawFilter);

                //builder.applyRotationEventBeatCutoff(group, baseBeat, filter);

                var baseData = new EventBuilderV3.BaseRotationData(
                    baseBeat, group, lightCount, filter,
                    (beatDistributionType % 2), beatDistributionValue,
                    (rotationDistributionType % 2), rotationDistributionValue, rotationEasing,
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

    private void preProcessTranslationEventsV3(EventBuilderV3 builder, JsonArray rawTranslationEvents) {

    }

    private void buildLightEventsV3(EventBuilderV3 builder, Difficulty difficulty) {

        var finalBeat = difficulty.getInfo().getBeat(difficulty.getInfo().getSongDuration(), 1);

        int groupCount = getGroupCount();

        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {

                for (var rawEvent : builder.getRawLightEvents(group, lightID)) {
                    var lastEvent = builder.getLatestLightEvent(group, lightID);

                    var startBeat = lastEvent.getEventBeat() + lastEvent.getEventDuration();

                    var endBeat = rawEvent.eventBeat() + rawEvent.beatOffset() + rawEvent.endOffset();

                    var duration = Math.max(0, endBeat - startBeat);


                    if (rawEvent.eventType() == 0) { // instant
                        var extensionEvent = lastEvent.extendTo(startBeat, duration);
                        builder.putEvent(group, lightID, extensionEvent);

                        var endState = new LightState(
                            new BoostableColor(rawEvent.color()),
                            rawEvent.brightness()
                        );

                        endState.setStrobeState(
                            rawEvent.strobeBrightness(),
                            rawEvent.strobeFrequency(),
                            rawEvent.strobeFade()
                        );

                        var instantEvent = new LightEventV3(
                            endBeat, extensionEvent.lightState.copy(),
                            endState, 0, lightID
                        );


                        builder.putEvent(group, lightID, instantEvent);

                    }
                    else if (rawEvent.eventType() == 1) { // transition

                        var startState = lastEvent.lightState.copy();
                        var endState = new LightState(
                            new BoostableColor(rawEvent.color()),
                            rawEvent.brightness()
                        );

                        endState.setStrobeState(
                            rawEvent.strobeBrightness(),
                            rawEvent.strobeFrequency(),
                            rawEvent.strobeFade()
                        );

                        var transitionEvent = new LightEventV3(
                            startBeat,
                            startState, endState,
                            duration, lightID
                        );

                        builder.putEvent(group, lightID, transitionEvent);

                    }
                    else { // extend
                        var extensionEvent = lastEvent.extendTo(startBeat, duration);
                        builder.putEvent(group, lightID, extensionEvent);
                    }

                }
                var lastEvent = builder.getLatestLightEvent(group, lightID);
                var endEvent = lastEvent.extendTo(finalBeat, 0);
                builder.putEvent(group, lightID, endEvent);

            }
        }

    }

    private static final TransformState.Axis[] rotationAxes = new TransformState.Axis[]{
        TransformState.Axis.RX,
        TransformState.Axis.RY,
        TransformState.Axis.RZ
    };

    private void buildRotationEventsV3(EventBuilderV3 builder, Difficulty difficulty) {
        var finalBeat = difficulty.getInfo().getBeat(difficulty.getInfo().getSongDuration(), 1);

        int groupCount = getGroupCount();

        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                for (var axis : rotationAxes) {

                    for (var rawEvent : builder.getRawRotationEvents(group, lightID, axis)) {
                        var lastEvent = builder.getLatestRotationEvent(group, lightID, axis);

                        var startBeat = lastEvent.getEventBeat() + lastEvent.getEventDuration();

                        var endBeat = rawEvent.eventBeat() + rawEvent.beatOffset() + rawEvent.endOffset();

                        var duration = Math.max(0, endBeat - startBeat);


                        if (rawEvent.eventType() == 0) { // transition
                            var startState = lastEvent.transformState.copy();
                            var endState = new TransformState(
                                axis, rawEvent.rotation()
                            );

                            var transitionEvent = new RotationEventV3(
                                startBeat,
                                startState, endState,
                                duration, lightID, rawEvent.easing(),
                                rawEvent.loopCount(), rawEvent.direction()
                            );

                            builder.putEvent(group, lightID, axis, transitionEvent);

                        }
                        else { // extend
                            var extensionEvent = lastEvent.extendTo(startBeat, duration);
                            builder.putEvent(group, lightID, axis, extensionEvent);
                        }

                    }
                    var lastEvent = builder.getLatestRotationEvent(group, lightID, axis);
                    var endEvent = lastEvent.extendTo(finalBeat, 0);
                    builder.putEvent(group, lightID, axis, endEvent);
                }

            }
        }
    }

    private static final TransformState.Axis[] translationAxes = new TransformState.Axis[]{
        TransformState.Axis.TX,
        TransformState.Axis.TY,
        TransformState.Axis.TZ
    };

    private void buildTranslationEventsV3(EventBuilderV3 builder, Difficulty difficulty) {

    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var rawTranslationEvents = json.getAsJsonArray("lightTranslationEventBoxGroups");

        var rawBoostEvents = json.getAsJsonArray("colorBoostBeatmapEvents");

        var boostEvents = new ArrayList<ColorBoostEvent>();
        boostEvents.add(new ColorBoostEvent(0, false));

        rawBoostEvents.forEach(rawEvent -> {
            var eventData = rawEvent.getAsJsonObject();
            boostEvents.add(new ColorBoostEvent().loadV3(eventData, difficulty));
        });

        boostEventHandler = new ColorBoostEventHandler(boostEvents);

        var eventBuilder = new EventBuilderV3();

        preProcessLightEventsV3(eventBuilder, rawColorEventBoxes);
        preProcessRotationEventsV3(eventBuilder, rawRotationEvents);
        preProcessTranslationEventsV3(eventBuilder, rawTranslationEvents);
        eventBuilder.sortEvents();
        buildLightEventsV3(eventBuilder, difficulty);
        buildRotationEventsV3(eventBuilder, difficulty);
        buildTranslationEventsV3(eventBuilder, difficulty);

        int groupCount = getGroupCount();
        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                linkEvents(group, lightID, eventBuilder.getLightEvents(group, lightID), eventBuilder.getRotationEvents(group, lightID), eventBuilder.getTranslationEvents(group, lightID));
            }
        }

    }

    private void loadV4(Difficulty difficulty, JsonObject json) {

        var rawEventBoxGroups = json.getAsJsonArray("eventBoxGroups");
        var indexFilters = json.getAsJsonArray("indexFilters");

        var lightColorEventBoxes = json.getAsJsonArray("lightColorEventBoxes");
        var lightColorEventMetaData = json.getAsJsonArray("lightColorEvents");

        var lightRotationEventBoxes = json.getAsJsonArray("lightRotationEventBoxes");
        var lightRotationEventMetaData = json.getAsJsonArray("lightRotationEvents");

        var lightTranslationEventBoxes = json.getAsJsonArray("lightTranslationEventBoxes");
        var lightTranslationEventMetaData = json.getAsJsonArray("lightTranslationEvents");



    }

    @Override
    public Environment reset() {
        boostEventHandler = null;

        return this;
    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);

        if (boostEventHandler != null) boostEventHandler.update(beat);

    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
    }
}
