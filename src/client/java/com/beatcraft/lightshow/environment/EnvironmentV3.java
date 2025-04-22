package com.beatcraft.lightshow.environment;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.event.Filter;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
    protected abstract void linkEvents(int group, int lightID, List<LightEventV3> events, List<TransformEvent> transformEvents);

    private static ArrayList<Triplet<Integer[], Float, Float>> reChunk(ArrayList<Triplet<Integer[], Float, Float>> targets, int chunks) {
        int total = targets.size();
        ArrayList<Triplet<Integer[], Float, Float>> result = new ArrayList<>(chunks);

        double chunkSize = (double) total / chunks;
        double currentIndex = 0;

        for (int i = 0; i < chunks; i++) {
            int start = (int) Math.round(currentIndex);
            currentIndex += chunkSize;
            int end = (int) Math.round(currentIndex);

            var subList = targets.subList(start, end);

            int len = 0;

            for (var s : subList) {
                len += s.getA().length;
            }
            var chunkTargets = new Integer[len];
            int pos = 0;
            for (var s : subList) {
                System.arraycopy(s.getA(), 0, chunkTargets, pos, s.getA().length);
                pos += s.getA().length;
            }

            var f = ((float) i) / (float) chunks;
            result.add(new Triplet<>(chunkTargets, f, f));

        }

        return result;
    }

    private static int[] flatten(List<Pair<Integer, Float>[]> list) {
        int totalLength = list.stream().mapToInt(arr -> arr.length).sum();
        int[] result = new int[totalLength];

        int pos = 0;
        for (var arr : list) {
            for (var v : arr) {
                result[pos++] = v.getLeft();
            }
        }

        return result;
    }

    private static void removeValues(ArrayList<Triplet<Integer[], Float, Float>> arrays, List<Integer> toRemove) {
        var removeSet = new java.util.HashSet<>(toRemove);

        for (int i = 0; i < arrays.size(); i++) {
            Triplet<Integer[], Float, Float> original = arrays.get(i);

            int count = 0;
            for (var val : original.getA()) {
                if (!removeSet.contains(val)) {
                    count++;
                }
            }

            var filtered = new Integer[count];
            int index = 0;
            for (var val : original.getA()) {
                if (!removeSet.contains(val)) {
                    filtered[index++] = val;
                }
            }

            arrays.set(i, new Triplet<>(filtered, original.getB(), original.getC()));
        }
    }


    private Filter processFilter(int lightCount, ArrayList<Integer> coveredIds, JsonObject filter) {
        var targets = new ArrayList<Triplet<Integer[], Float, Float>>();
        var ordering = new ArrayList<Integer>();
        for (int i = 0; i < lightCount; i++) {
            var f = ((float) i) / (float) lightCount;
            targets.add(new Triplet<>(new Integer[]{i}, f, f));
        }


        // divide available objects by `chunks`. treat each section as a single object
        var chunks = JsonUtil.getOrDefault(filter, "c", JsonElement::getAsInt, 0);

        if (chunks == 0) { chunks = targets.size(); }
        chunks = Math.clamp(chunks, 1, targets.size());

        targets = reChunk(targets, chunks);

        for (var i = 0; i < targets.size(); i++) {
            ordering.add(i);
        }

        // 0 = division, 1 = step & offset
        var type = JsonUtil.getOrDefault(filter, "f", JsonElement::getAsInt, 0);
        // division:
        // p0: number of sections to divide the objects into
        // p1: which section to process
        // step & offset:
        // p0: starting light
        // p1: step value
        var p0 = JsonUtil.getOrDefault(filter, "p", JsonElement::getAsFloat, 0f);
        var p1 = JsonUtil.getOrDefault(filter, "t", JsonElement::getAsFloat, 0f);

        // reverses distribution
        var reverse = JsonUtil.getOrDefault(filter, "r", JsonElement::getAsInt, 0) > 0;

        // 0 = not random, 1 = keep order?, 2 = random elements
        var randomBehavior = JsonUtil.getOrDefault(filter, "n", JsonElement::getAsInt, 0);

        var randomSeed = JsonUtil.getOrDefault(filter, "s", JsonElement::getAsInt, 0);


        // what percentage of objects to affect
        var limitPercent = JsonUtil.getOrDefault(filter, "l", JsonElement::getAsFloat, 0f);
        // 0 = none, 1 = duration, 2 = distribution
        var limitBehavior = JsonUtil.getOrDefault(filter, "d", JsonElement::getAsInt, 0);

        // limitPercent = 0.75 means 75% of lights are affected
        // duration: the event duration is 75% as long
        // distribution: if in wave mode, the distribution ends at 75% instead of getting cut off early

        if (limitPercent == 0) {
            limitPercent = 1.0f;
        }

        for (int i = 0; i < targets.size(); i++) {
            var trip = targets.get(i);
            var chunkTargets = trip.getA();
            var durationMod = trip.getB();
            var beatMod = trip.getC();
            if ((limitBehavior & 1) > 0) {
                durationMod *= limitPercent;
            }
            if ((limitBehavior & 2) > 0) {
                beatMod *= limitPercent;
            }
            targets.set(i, new Triplet<>(chunkTargets, durationMod, beatMod));
        }

        removeValues(targets, coveredIds);

        for (var arr : targets) {
            for (var t : arr.getA()) {
                if (!coveredIds.contains(t)) {
                    coveredIds.add(t);
                }
            }
        }

        random.setSeed(randomSeed);
        if ((randomBehavior & 2) > 0) {
            ArrayList<Integer> reordered = new ArrayList<>();

            var s = ordering.size();
            for (int i = 0; i < s; i++) {
                var index = random.nextInt(0, ordering.size());
                reordered.add(ordering.remove(index));
            }
            ordering = reordered;
        }

        return new Filter(targets, ordering.toArray(new Integer[0]));
    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var colorEvents = new HashMap<Integer[], ArrayList<LightEventV3>>();
        var transformEvents = new HashMap<Integer[], ArrayList<TransformEvent>>();

        // {[group, lightId]: event, ...}
        var latestColorEvents = new HashMap<Integer[], LightEventV3>();

        rawColorEventBoxes.forEach((rawBoxGroup) -> {
            var boxGroupObj = rawBoxGroup.getAsJsonObject();
            var baseBeat = JsonUtil.getOrDefault(boxGroupObj, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupObj, "g", JsonElement::getAsInt, 0);
            var rawSubEvents = boxGroupObj.getAsJsonArray("e");
            ArrayList<Integer> coveredIds = new ArrayList<>();
            var lightCount = getLightCount(group);

            rawSubEvents.forEach((rawSubEvent) -> {
                var subEvent = rawSubEvent.getAsJsonObject();

                var rawFilter = subEvent.getAsJsonObject("f");

                var filter = processFilter(lightCount, coveredIds, rawFilter);

                // 0 = step, 1 = wave
                var beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 0f);
                var beatDistributionType = JsonUtil.getOrDefault(subEvent, "d", JsonElement::getAsInt, 0);

                float maxBeat = beatDistributionValue;
                if (beatDistributionType == 0) {
                    maxBeat *= filter.chunkCount();
                }

                // 0 = step, 1 = wave
                var brightnessDistributionValue = JsonUtil.getOrDefault(subEvent, "r", JsonElement::getAsFloat, 0f);
                var brightnessDistributionType = JsonUtil.getOrDefault(subEvent, "t", JsonElement::getAsInt, 0);

                float maxBrightness0 = brightnessDistributionValue;
                if (brightnessDistributionType == 0) {
                    maxBrightness0 *= filter.chunkCount();
                }

                // whether distribution affects the first event in the sequence
                boolean affectsFirst = JsonUtil.getOrDefault(subEvent, "b", JsonElement::getAsInt, 0) > 0;


                var brightnessDistributionEasing = JsonUtil.getOrDefault(subEvent, "i", JsonElement::getAsInt, -1);
                var easingFunction = Easing.getEasing(String.valueOf(brightnessDistributionEasing));

                AtomicReference<Boolean> doDistribution = new AtomicReference<>(affectsFirst);

                var rawEvents = subEvent.getAsJsonArray("e");

                float maxDuration = maxBeat;
                float maxBrightness = maxBrightness0;
                rawEvents.forEach(rawEvent -> {
                    var eventData = rawEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);

                    // 0 = instant, 1 = transition, 2 = extend
                    var transitionType = JsonUtil.getOrDefault(eventData, "i", JsonElement::getAsInt, 0);

                    var color = JsonUtil.getOrDefault(eventData, "c", JsonElement::getAsInt, 0);

                    var brightness = JsonUtil.getOrDefault(eventData, "s", JsonElement::getAsFloat, 0f);

                    var strobeFrequency = JsonUtil.getOrDefault(eventData, "f", JsonElement::getAsFloat, 0f);

                    var strobeBrightness = JsonUtil.getOrDefault(eventData, "sb", JsonElement::getAsFloat, 0f);

                    var strobeFade = JsonUtil.getOrDefault(eventData, "sf", JsonElement::getAsInt, 0) > 0;

                    for (var targetSet : filter) {
                        var targets = targetSet.getA();
                        var durationMod = doDistribution.get() ? targetSet.getB() : 0;
                        var distributionMod = doDistribution.get() ? targetSet.getC() : 0;

                        if (transitionType == 1) {
                            for (var target : targets) {
                                var last = latestColorEvents.computeIfAbsent(new Integer[]{group, target}, k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k[1]));

                                var curr_start = last.getEventBeat() + last.getEventDuration();
                                var curr_end = baseBeat + beatOffset + (durationMod * maxDuration);

                                var curr_brightness = easingFunction.apply(distributionMod * maxBrightness * brightness);

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

                                var currentEvent = new LightEventV3(
                                    curr_start, curr_startState, curr_endState,
                                    curr_duration, target,
                                    last.strobeFrequency, last.strobeBrightness,
                                    strobeFrequency, strobeBrightness, strobeFade
                                );

                                var key = new Integer[]{group, target};
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else if (transitionType == 0) {
                            for (var target : targets) {

                                var curr_end = baseBeat + beatOffset + (durationMod * maxDuration);

                                var curr_brightness = easingFunction.apply(distributionMod * maxBrightness * brightness);

                                var curr_duration = 0;

                                LightState curr_endState;
                                if (color == 0) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentLeftColor().withAlpha(1), curr_brightness);
                                } else if (color == 1) {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentRightColor().withAlpha(1), curr_brightness);
                                } else {
                                    curr_endState = new LightState(difficulty.getSetDifficulty().getColorScheme().getEnvironmentWhiteColor().withAlpha(1), curr_brightness);
                                }
                                var curr_startState = curr_endState;

                                var currentEvent = new LightEventV3(
                                    curr_end, curr_startState, curr_endState,
                                    curr_duration, target,
                                    0, 0,
                                    strobeFrequency, strobeBrightness, strobeFade
                                );

                                var key = new Integer[]{group, target};
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else {
                            for (var target : targets) {
                                var last = latestColorEvents.computeIfAbsent(new Integer[]{group, target}, k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k[1]));

                                var currentEvent = last.extendTo(baseBeat + beatOffset + (durationMod * maxDuration));

                                var key = new Integer[]{group, target};
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        }

                    }

                    doDistribution.set(true);
                });


            });

        });

        colorEvents.forEach((k, v) -> {
            linkEvents(k[0], k[1], v, transformEvents.remove(k));
        });

        transformEvents.forEach((k, v) -> {
            linkEvents(k[0], k[1], new ArrayList<>(), v);
        });

    }

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
