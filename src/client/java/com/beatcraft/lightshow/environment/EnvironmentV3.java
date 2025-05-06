package com.beatcraft.lightshow.environment;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.event.EventBuilder;
import com.beatcraft.lightshow.event.Filter;
import com.beatcraft.lightshow.event.events.ColorBoostEvent;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEventV3;
import com.beatcraft.lightshow.event.events.TranslationEvent;
import com.beatcraft.lightshow.event.handlers.ColorBoostEventHandler;
import com.beatcraft.lightshow.event.handlers.GroupEventHandlerV3;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import oshi.util.tuples.Triplet;

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

    @Override
    public float getVersion() {
        return 3;
    }

    public abstract int getGroupCount();
    public abstract int getLightCount(int group);
    protected abstract void linkEvents(
        int group, int lightID,
        List<LightEventV3> lightEvents,
        HashMap<TransformState.Axis,ArrayList<RotationEventV3>> rotationEvents,
        HashMap<TransformState.Axis,ArrayList<TranslationEvent>> translationEvents,
        List<Integer> floatFxEvents
    );

    private static final TransformState.Axis[] rotationAxes = new TransformState.Axis[]{
        TransformState.Axis.RX,
        TransformState.Axis.RY,
        TransformState.Axis.RZ
    };

    private static final TransformState.Axis[] translationAxes = new TransformState.Axis[]{
        TransformState.Axis.TX,
        TransformState.Axis.TY,
        TransformState.Axis.TZ
    };


    protected abstract HashMap<Integer, Pair<LightGroupV3, GroupEventHandlerV3>> getEventGroups();

    public List<LightEventV3> getLightEvents(int group, int lightID, float start, float end) {
        var eventGroups = getEventGroups();
        var groupEventHandler = eventGroups.get(group).getRight();
        return groupEventHandler.lightHandlers.get(lightID).getEventsInRange(start, end);
    }

    public List<RotationEventV3> getRotationEvents(int group, int lightID, TransformState.Axis axis, float start, float end) {
        var eventGroups = getEventGroups();
        var groupEventHandler = eventGroups.get(group).getRight();
        return groupEventHandler.rotationHandlers.get(lightID).get(axis).getEventsInRange(start, end);
    }

    private void preProcessLightEventsV3(EventBuilder builder, JsonArray rawLightEvents) {
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

                var baseData = new EventBuilder.BaseLightData(
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

                    var events = baseData.buildEventsV3(
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

    private void preProcessRotationEventsV3(EventBuilder builder, JsonArray rawRotationEvents) {
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

                if (!coveredIDs.containsKey(axis)) {
                    coveredIDs.put(axis, new ArrayList<>());
                }
                var covered = coveredIDs.get(axis);
                var filter = Filter.processFilter(random, lightCount, covered, rawFilter);

                //builder.applyRotationEventBeatCutoff(group, baseBeat, filter);

                var baseData = new EventBuilder.BaseRotationData(
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

    private void preProcessTranslationEventsV3(EventBuilder builder, JsonArray rawTranslationEvents) {
        rawTranslationEvents.forEach(rawBoxGroup -> {
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

                var gapDistributionValue = JsonUtil.getOrDefault(eventLaneData, "s", JsonElement::getAsFloat, 0f);
                var gapDistributionType = JsonUtil.getOrDefault(eventLaneData, "t", JsonElement::getAsInt, 0);
                boolean affectsFirst = JsonUtil.getOrDefault(eventLaneData, "b", JsonElement::getAsInt, 0) > 0;

                var distributionEasing = JsonUtil.getOrDefault(eventLaneData, "i", JsonElement::getAsInt, 0);
                var gapEasing = Easing.getEasing(String.valueOf(distributionEasing));

                var rawAxis = JsonUtil.getOrDefault(eventLaneData, "a", JsonElement::getAsInt, 0);
                boolean invertAxis = JsonUtil.getOrDefault(eventLaneData, "r", JsonElement::getAsInt, 0) > 0;

                var axis = TransformState.Axis.values()[(rawAxis % 3) + 3];

                if (!coveredIDs.containsKey(axis)) {
                    coveredIDs.put(axis, new ArrayList<>());
                }
                var covered = coveredIDs.get(axis);
                var filter = Filter.processFilter(random, lightCount, covered, rawFilter);

                var baseData = new EventBuilder.BaseTranslationData(
                    baseBeat, group, lightCount, filter,
                    (beatDistributionType % 2), beatDistributionValue,
                    (gapDistributionType % 2), gapDistributionValue, gapEasing,
                    axis, invertAxis, affectsFirst
                );

                var rawTranslationSubEvents = eventLaneData.getAsJsonArray("l");
                AtomicBoolean isFirst = new AtomicBoolean(true);
                rawTranslationSubEvents.forEach(rawSubEvent -> {
                    var eventData = rawSubEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                    var transitionType = JsonUtil.getOrDefault(eventData, "p", JsonElement::getAsInt, 0);
                    var rawEasing = JsonUtil.getOrDefault(eventData, "e", JsonElement::getAsInt, 0);
                    var magnitude = JsonUtil.getOrDefault(eventData, "t", JsonElement::getAsFloat, 0f);

                    var easing = Easing.getEasing(String.valueOf(rawEasing));

                    var events = baseData.buildEvents(
                        isFirst.get(),
                        beatOffset, transitionType, magnitude,
                        easing
                    );

                    builder.addRawTranslationEvents(events);

                    isFirst.set(false);
                });

            });

        });
    }

    private void preProcessLightEventsV4(
        EventBuilder builder, float baseBeat, int group,
        JsonArray eventBoxDataArray, JsonArray indexFilters,
        JsonArray colorEventBoxes, JsonArray colorEventMetaData
    ) {
        var coveredIDs = new ArrayList<Integer>();
        var lightCount = getLightCount(group);

        eventBoxDataArray.forEach(rawEventBoxData -> {
            var eventBoxData = rawEventBoxData.getAsJsonObject();

            var filterIndex = JsonUtil.getOrDefault(eventBoxData, "f", JsonElement::getAsInt, 0);
            var boxMetaDataIndex = JsonUtil.getOrDefault(eventBoxData, "e", JsonElement::getAsInt, 0);
            var eventList = eventBoxData.getAsJsonArray("l");

            var rawFilter = indexFilters.get(filterIndex).getAsJsonObject();
            var boxMetaData = colorEventBoxes.get(boxMetaDataIndex).getAsJsonObject();

            var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);

            var beatDistributionValue = JsonUtil.getOrDefault(boxMetaData, "w", JsonElement::getAsFloat, 1f);
            var beatDistributionType = JsonUtil.getOrDefault(boxMetaData, "d", JsonElement::getAsInt, 0);

            var brightnessDistributionValue = JsonUtil.getOrDefault(boxMetaData, "s", JsonElement::getAsFloat, 1f);
            var brightnessDistributionType = JsonUtil.getOrDefault(boxMetaData, "t", JsonElement::getAsInt, 0);

            boolean affectsFirst = JsonUtil.getOrDefault(boxMetaData, "b", JsonElement::getAsInt, 0) > 0;

            var rawDistributionEasing = JsonUtil.getOrDefault(boxMetaData, "e", JsonElement::getAsInt, 0);

            var distributionEasing = Easing.getEasing(String.valueOf(rawDistributionEasing));

            var baseData = new EventBuilder.BaseLightData(
                baseBeat, group, lightCount, filter,
                beatDistributionType, beatDistributionValue,
                brightnessDistributionType, brightnessDistributionValue,
                distributionEasing, affectsFirst
            );

            AtomicBoolean isFirst = new AtomicBoolean(true);
            eventList.forEach(rawEventData -> {
                var eventData = rawEventData.getAsJsonObject();

                var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                var metaDataIndex = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);

                var metaData = colorEventMetaData.get(metaDataIndex).getAsJsonObject();

                var eventType = JsonUtil.getOrDefault(metaData, "p", JsonElement::getAsInt, 0);
                var rawEasing = JsonUtil.getOrDefault(metaData, "e", JsonElement::getAsInt, 0);
                var color = JsonUtil.getOrDefault(metaData, "c", JsonElement::getAsInt, 0);
                var brightness = JsonUtil.getOrDefault(metaData, "b", JsonElement::getAsFloat, 0f);
                var strobeFrequency = JsonUtil.getOrDefault(metaData, "f", JsonElement::getAsFloat, 0f);
                var strobeBrightness = JsonUtil.getOrDefault(metaData, "sb", JsonElement::getAsFloat, 0f);
                boolean strobeFade = JsonUtil.getOrDefault(metaData, "sf", JsonElement::getAsInt, 0) > 0;

                var easing = Easing.getEasing(String.valueOf(rawEasing));

                var events = baseData.buildEventsV4(
                    isFirst.get(),
                    beatOffset, eventType,
                    color, brightness,
                    strobeFrequency, strobeBrightness, strobeFade,
                    easing
                );

                builder.addRawLightEvents(events);

                isFirst.set(false);
            });

        });
    }

    private void preProcessRotationEventsV4(
        EventBuilder builder, float baseBeat, int group,
        JsonArray eventBoxDataArray, JsonArray indexFilters,
        JsonArray rotationEventBoxes, JsonArray rotationEventMetaData
    ) {
        var coveredIDs = new ArrayList<Integer>();
        var lightCount = getLightCount(group);

        eventBoxDataArray.forEach(rawEventBoxData -> {
            var eventBoxData = rawEventBoxData.getAsJsonObject();

            var filterIndex = JsonUtil.getOrDefault(eventBoxData, "f", JsonElement::getAsInt, 0);
            var boxMetaDataIndex = JsonUtil.getOrDefault(eventBoxData, "e", JsonElement::getAsInt, 0);
            var eventList = eventBoxData.getAsJsonArray("l");

            var rawFilter = indexFilters.get(filterIndex).getAsJsonObject();
            var boxMetaData = rotationEventBoxes.get(boxMetaDataIndex).getAsJsonObject();

            var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);

            var beatDistributionValue = JsonUtil.getOrDefault(boxMetaData, "w", JsonElement::getAsFloat, 1f);
            var beatDistributionType = JsonUtil.getOrDefault(boxMetaData, "d", JsonElement::getAsInt, 0);

            var rotationDistributionValue = JsonUtil.getOrDefault(boxMetaData, "s", JsonElement::getAsFloat, 0f);
            var rotationDistributionType = JsonUtil.getOrDefault(boxMetaData, "t", JsonElement::getAsInt, 0);


            boolean affectsFirst = JsonUtil.getOrDefault(boxMetaData, "b", JsonElement::getAsInt, 0) > 0;

            var rawDistributionEasing = JsonUtil.getOrDefault(boxMetaData, "e", JsonElement::getAsInt, 0);

            var distributionEasing = Easing.getEasing(String.valueOf(rawDistributionEasing));

            var rawAxis = JsonUtil.getOrDefault(boxMetaData, "a", JsonElement::getAsInt, 0);
            boolean invertAxis = JsonUtil.getOrDefault(boxMetaData, "f", JsonElement::getAsInt, 0) > 0;

            var axis = TransformState.Axis.values()[rawAxis % 3];

            var baseData = new EventBuilder.BaseRotationData(
                baseBeat, group, lightCount, filter,
                beatDistributionType, beatDistributionValue,
                rotationDistributionType, rotationDistributionValue, distributionEasing,
                axis, invertAxis, affectsFirst
            );

            AtomicBoolean isFirst = new AtomicBoolean(true);
            eventList.forEach(rawEventData -> {
                var eventData = rawEventData.getAsJsonObject();

                var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                var metaDataIndex = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);

                var metaData = rotationEventMetaData.get(metaDataIndex).getAsJsonObject();

                var eventType = JsonUtil.getOrDefault(metaData, "p", JsonElement::getAsInt, 0);
                var rawEasing = JsonUtil.getOrDefault(metaData, "e", JsonElement::getAsInt, 0);

                var magnitude = JsonUtil.getOrDefault(metaData, "r", JsonElement::getAsFloat, 0f);
                var direction = JsonUtil.getOrDefault(metaData, "d", JsonElement::getAsInt, 0);
                var loopCount = JsonUtil.getOrDefault(metaData, "l", JsonElement::getAsInt, 0);

                var easing = Easing.getEasing(String.valueOf(rawEasing));

                var events = baseData.buildEvents(
                    isFirst.get(),
                    beatOffset, eventType,
                    magnitude, direction, loopCount,
                    easing
                );

                builder.addRawRotationEvents(events);

                isFirst.set(false);
            });

        });

    }

    private void preProcessTranslationEventsV4(
        EventBuilder builder, float baseBeat, int group,
        JsonArray eventBoxDataArray, JsonArray indexFilters,
        JsonArray translationEventBoxes, JsonArray translationEventMetaData
    ) {
        var coveredIDs = new ArrayList<Integer>();
        var lightCount = getLightCount(group);

        eventBoxDataArray.forEach(rawEventBoxData -> {
            var eventBoxData = rawEventBoxData.getAsJsonObject();

            var filterIndex = JsonUtil.getOrDefault(eventBoxData, "f", JsonElement::getAsInt, 0);
            var boxMetaDataIndex = JsonUtil.getOrDefault(eventBoxData, "e", JsonElement::getAsInt, 0);
            var eventList = eventBoxData.getAsJsonArray("l");

            var rawFilter = indexFilters.get(filterIndex).getAsJsonObject();
            var boxMetaData = translationEventBoxes.get(boxMetaDataIndex).getAsJsonObject();

            var filter = Filter.processFilter(random, lightCount, coveredIDs, rawFilter);

            var beatDistributionValue = JsonUtil.getOrDefault(boxMetaData, "w", JsonElement::getAsFloat, 1f);
            var beatDistributionType = JsonUtil.getOrDefault(boxMetaData, "d", JsonElement::getAsInt, 0);

            var rotationDistributionValue = JsonUtil.getOrDefault(boxMetaData, "s", JsonElement::getAsFloat, 0f);
            var rotationDistributionType = JsonUtil.getOrDefault(boxMetaData, "t", JsonElement::getAsInt, 0);

            boolean affectsFirst = JsonUtil.getOrDefault(boxMetaData, "b", JsonElement::getAsInt, 0) > 0;

            var rawDistributionEasing = JsonUtil.getOrDefault(boxMetaData, "e", JsonElement::getAsInt, 0);

            var distributionEasing = Easing.getEasing(String.valueOf(rawDistributionEasing));

            var rawAxis = JsonUtil.getOrDefault(boxMetaData, "a", JsonElement::getAsInt, 0);
            boolean invertAxis = JsonUtil.getOrDefault(boxMetaData, "f", JsonElement::getAsInt, 0) > 0;

            var axis = TransformState.Axis.values()[(rawAxis % 3) + 3];

            var baseData = new EventBuilder.BaseTranslationData(
                baseBeat, group, lightCount, filter,
                beatDistributionType, beatDistributionValue,
                rotationDistributionType, rotationDistributionValue, distributionEasing,
                axis, invertAxis, affectsFirst
            );

            AtomicBoolean isFirst = new AtomicBoolean(true);
            eventList.forEach(rawEventData -> {
                var eventData = rawEventData.getAsJsonObject();

                var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
                var metaDataIndex = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);

                var metaData = translationEventMetaData.get(metaDataIndex).getAsJsonObject();

                var eventType = JsonUtil.getOrDefault(metaData, "p", JsonElement::getAsInt, 0);
                var rawEasing = JsonUtil.getOrDefault(metaData, "e", JsonElement::getAsInt, 0);

                var magnitude = JsonUtil.getOrDefault(metaData, "r", JsonElement::getAsFloat, 0f);

                var easing = Easing.getEasing(String.valueOf(rawEasing));

                var events = baseData.buildEvents(
                    isFirst.get(),
                    beatOffset, eventType,
                    magnitude,
                    easing
                );

                builder.addRawTranslationEvents(events);

                isFirst.set(false);
            });

        });

    }

    private void buildLightEvents(EventBuilder builder, Difficulty difficulty) {

        var finalBeat = difficulty.getInfo().getBeat(difficulty.getInfo().getSongDuration(), 1);

        int groupCount = getGroupCount();

        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {

                for (var rawEvent : builder.getRawLightEvents(group, lightID)) {
                    var lastEvent = builder.getLatestLightEvent(group, lightID);

                    var endBeat = rawEvent.eventBeat() + rawEvent.beatOffset() + rawEvent.endOffset();
                    var startBeat = Math.min(lastEvent.getEventBeat() + lastEvent.getEventDuration(), endBeat);

                    var duration = Math.max(0, endBeat - startBeat);

                    if (rawEvent.eventType() == 0) { // transition

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
                            duration, lightID,
                            rawEvent.easing()
                        );

                        builder.putEvent(group, lightID, transitionEvent);

                    }
                    else { // extend
                        var extensionEvent = lastEvent.extendTo(endBeat);
                        builder.putEvent(group, lightID, extensionEvent);
                    }

                }
                var lastEvent = builder.getLatestLightEvent(group, lightID);
                var endEvent = lastEvent.extendTo(finalBeat);
                builder.putEvent(group, lightID, endEvent);

            }
        }

    }

    private void buildRotationEvents(EventBuilder builder, Difficulty difficulty) {
        var finalBeat = difficulty.getInfo().getBeat(difficulty.getInfo().getSongDuration(), 1);

        int groupCount = getGroupCount();

        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                for (var axis : rotationAxes) {

                    for (var rawEvent : builder.getRawRotationEvents(group, lightID, axis)) {
                        var lastEvent = builder.getLatestRotationEvent(group, lightID, axis);


                        var endBeat = rawEvent.eventBeat() + rawEvent.beatOffset() + rawEvent.endOffset();
                        var startBeat = Math.min(lastEvent.getEventBeat() + lastEvent.getEventDuration(), endBeat);

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
                            var extensionEvent = lastEvent.extendTo(endBeat);
                            builder.putEvent(group, lightID, axis, extensionEvent);
                        }

                    }
                    var lastEvent = builder.getLatestRotationEvent(group, lightID, axis);
                    var endEvent = lastEvent.extendTo(finalBeat);
                    builder.putEvent(group, lightID, axis, endEvent);
                }

            }
        }
    }

    private void buildTranslationEvents(EventBuilder builder, Difficulty difficulty) {
        var finalBeat = difficulty.getInfo().getBeat(difficulty.getInfo().getSongDuration(), 1);

        int groupCount = getGroupCount();

        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                for (var axis : translationAxes) {

                    for (var rawEvent : builder.getRawTranslationEvents(group, lightID, axis)) {
                        var lastEvent = builder.getLatestTranslationEvent(group, lightID, axis);


                        var endBeat = rawEvent.eventBeat() + rawEvent.beatOffset() + rawEvent.endOffset();
                        var startBeat = Math.min(lastEvent.getEventBeat() + lastEvent.getEventDuration(), endBeat);

                        var duration = Math.max(0, endBeat - startBeat);

                        if (rawEvent.eventType() == 0) { // transition
                            var startState = lastEvent.transformState.copy();
                            var endState = new TransformState(
                                axis, rawEvent.delta()
                            );

                            var transitionEvent = new TranslationEvent(
                                startBeat,
                                startState, endState,
                                duration,
                                lightID, rawEvent.easing()
                            );

                            builder.putEvent(group, lightID, axis, transitionEvent);

                        }
                        else { // extend
                            var extensionEvent = lastEvent.extendTo(endBeat);
                            builder.putEvent(group, lightID, axis, extensionEvent);
                        }

                    }
                    var lastEvent = builder.getLatestTranslationEvent(group, lightID, axis);
                    var endEvent = lastEvent.extendTo(finalBeat);
                    builder.putEvent(group, lightID, axis, endEvent);
                }
            }
        }


    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var rawTranslationEvents = json.getAsJsonArray("lightTranslationEventBoxGroups");
        if (rawTranslationEvents == null) {
            rawTranslationEvents = new JsonArray();
        }

        var rawBoostEvents = json.getAsJsonArray("colorBoostBeatmapEvents");


        var boostEvents = new ArrayList<ColorBoostEvent>();
        boostEvents.add(new ColorBoostEvent(0, false));

        rawBoostEvents.forEach(rawEvent -> {
            var eventData = rawEvent.getAsJsonObject();
            boostEvents.add(new ColorBoostEvent().loadV3(eventData, difficulty));
        });

        boostEventHandler = new ColorBoostEventHandler(boostEvents);

        var eventBuilder = new EventBuilder();

        preProcessLightEventsV3(eventBuilder, rawColorEventBoxes);
        preProcessRotationEventsV3(eventBuilder, rawRotationEvents);
        preProcessTranslationEventsV3(eventBuilder, rawTranslationEvents);
        eventBuilder.sortEvents();
        buildLightEvents(eventBuilder, difficulty);
        buildRotationEvents(eventBuilder, difficulty);
        buildTranslationEvents(eventBuilder, difficulty);

        int groupCount = getGroupCount();
        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                linkEvents(
                    group, lightID,
                    eventBuilder.getLightEvents(group, lightID),
                    eventBuilder.getRotationEvents(group, lightID),
                    eventBuilder.getTranslationEvents(group, lightID),
                    new ArrayList<>()
                );
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

        var eventBuilder = new EventBuilder();

        rawEventBoxGroups.forEach(rawEventBox -> {
            var eventData = rawEventBox.getAsJsonObject();

            var baseBeat = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(eventData, "g", JsonElement::getAsInt, 0);
            var eventType = JsonUtil.getOrDefault(eventData, "t", JsonElement::getAsInt, 0);
            var data = eventData.getAsJsonArray("e");

            if (eventType == 1) {
                preProcessLightEventsV4(eventBuilder, baseBeat, group, data, indexFilters, lightColorEventBoxes, lightColorEventMetaData);
            } else if (eventType == 2) {
                preProcessRotationEventsV4(eventBuilder, baseBeat, group, data, indexFilters, lightRotationEventBoxes, lightRotationEventMetaData);
            } else if (eventType == 3) {
                preProcessTranslationEventsV4(eventBuilder, baseBeat, group, data, indexFilters, lightTranslationEventBoxes, lightTranslationEventMetaData);
            } // event type 4 is only in V4 light shows

        });

        eventBuilder.sortEvents();
        buildLightEvents(eventBuilder, difficulty);
        buildRotationEvents(eventBuilder, difficulty);
        buildTranslationEvents(eventBuilder, difficulty);

        int groupCount = getGroupCount();
        for (int group = 0; group < groupCount; group++) {
            int lightCount = getLightCount(group);
            for (int lightID = 0; lightID < lightCount; lightID++) {
                linkEvents(
                    group, lightID,
                    eventBuilder.getLightEvents(group, lightID),
                    eventBuilder.getRotationEvents(group, lightID),
                    eventBuilder.getTranslationEvents(group, lightID),
                    new ArrayList<>()
                );
            }
        }

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
