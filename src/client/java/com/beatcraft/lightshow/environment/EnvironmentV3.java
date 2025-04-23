package com.beatcraft.lightshow.environment;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.event.Filter;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EnvironmentV3 extends Environment {

    public static class GroupKey {
        private int group;
        private int id;

        public int getLightId() {
            return id;
        }

        public int getGroup() {
            return group;
        }

        public GroupKey(int group, int id) {
            this.group = group;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GroupKey other)) return false;
            return group == other.group && id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, id);
        }
    }

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
    protected abstract void linkEvents(int group, int lightID, List<LightEventV3> lightEvents, HashMap<TransformState.Axis, List<TransformEvent>> transformEvents);

    private static ArrayList<Triplet<Integer[], Float, Float>> reChunk(ArrayList<Triplet<Integer[], Float, Float>> targets, int chunks) {
        int total = targets.size();
        ArrayList<Triplet<Integer[], Float, Float>> result = new ArrayList<>(chunks);

        double chunkSize = (double) total / chunks;
        double currentIndex = 0;

        for (int i = 0; i < chunks; i++) {
            int start = (int) currentIndex;
            currentIndex += chunkSize;
            int end = (int) Math.ceil(currentIndex);

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

    // p0 = # of sections, p1 = section
    public static List<Triplet<Integer[], Float, Float>> getSection(ArrayList<Triplet<Integer[], Float, Float>> targets, float p0, int p1) {
        float total = targets.size();
        float span = total / p0;

        int start = (int) (span * p1);
        int end = (int) Math.ceil(span * (p1 + 1));

        end = Math.min(end, targets.size());

        return targets.subList(start, end);
    }


    private static <T> ArrayList<T> getStepOffset(ArrayList<T> targets, int p0, int p1) {
        var out = new ArrayList<T>();
        for (int i = p0-1; i < targets.size(); i += p1) {
            out.add(targets.get(i));
        }
        return out;
    }

    private Filter processFilter(int lightCount, ArrayList<Integer> coveredIds, JsonObject filter) {
        var targets = new ArrayList<Triplet<Integer[], Float, Float>>();
        var ordering = new ArrayList<Integer>();
        for (int i = 0; i < lightCount; i++) {
            var f = ((float) i) / (float) (lightCount-1);
            targets.add(new Triplet<>(new Integer[]{i}, f, f));
        }


        // divide available objects by `chunks`. treat each section as a single object
        var chunks = JsonUtil.getOrDefault(filter, "c", JsonElement::getAsInt, 0);

        if (chunks == 0) { chunks = targets.size(); }
        chunks = Math.clamp(chunks, 1, targets.size());

        targets = reChunk(targets, chunks);

        // 0 = division, 1 = step & offset
        var type = JsonUtil.getOrDefault(filter, "f", JsonElement::getAsInt, 0);

        type = type % 2;
        // division:
        // p0: number of sections to divide the objects into
        // p1: which section to process
        // step & offset:
        // p0: starting light
        // p1: step value
        var p0 = JsonUtil.getOrDefault(filter, "p", JsonElement::getAsInt, 0);
        var p1 = JsonUtil.getOrDefault(filter, "t", JsonElement::getAsInt, 0);

        if (type == 1) {
            targets = new ArrayList<>(getSection(targets, (float) p0, p1));
        } else {
            targets = getStepOffset(targets, Math.clamp(p0, 1, lightCount+1), Math.max(1, p1));
        }

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

        var cutoff = false;
        for (int i = 0; i < targets.size(); i++) {
            if (cutoff || (i / (float) targets.size()) > limitPercent) {
                cutoff = true;
                targets.remove(i);
                i--;
                continue;
            }
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

        for (var i = 0; i < targets.size(); i++) {
            ordering.add(i);
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

        ArrayList<float[]> weights = new ArrayList<>();
        if (reverse) {
            for (var t : targets) {
                weights.add(new float[]{t.getB(), t.getC()});
            }
            for (int i = 0; i < targets.size(); i++) {
                var t = targets.get(i);
                targets.set(i, new Triplet<>(t.getA(), weights.get(targets.size()-1-i)[0], weights.get(targets.size()-1-i)[1]));
            }
            ordering = new ArrayList<>(ordering.reversed());
        }

        return new Filter(targets, ordering.toArray(new Integer[0]));
    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

        var colorEvents = new HashMap<GroupKey, ArrayList<LightEventV3>>();
        var transformEvents = new HashMap<GroupKey, HashMap<TransformState.Axis, List<TransformEvent>>>();

        // {[group, lightId]: event, ...}
        var latestColorEvents = new HashMap<GroupKey, LightEventV3>();
        var latestRotationEvents = new HashMap<GroupKey, HashMap<Integer, TransformEvent>>();

        rawColorEventBoxes.forEach(rawBoxGroup -> {
            var boxGroupObj = rawBoxGroup.getAsJsonObject();
            var baseBeat = JsonUtil.getOrDefault(boxGroupObj, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupObj, "g", JsonElement::getAsInt, 0);
            var rawSubEvents = boxGroupObj.getAsJsonArray("e");
            ArrayList<Integer> coveredIds = new ArrayList<>();
            var lightCount = getLightCount(group);

            rawSubEvents.forEach(rawSubEvent -> {
                var subEvent = rawSubEvent.getAsJsonObject();

                var rawFilter = subEvent.getAsJsonObject("f");

                var filter = processFilter(lightCount, coveredIds, rawFilter);

                // 0 = step, 1 = wave
                var beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 0f);
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
                        var distributionMod = doDistribution.get() ? targetSet.getC() * maxBrightness : 1f;

                        if (transitionType == 1) {
                            for (var target : targets) {
                                var last = latestColorEvents.computeIfAbsent(new GroupKey(group, target), k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k.getLightId()));

                                var curr_start = last.getEventBeat() + last.getEventDuration();
                                var curr_end = baseBeat + beatOffset + durationMod;

                                var curr_brightness = brightness * distributionMod;

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

                                var key = new GroupKey(group, target);
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else if (transitionType == 0) {
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
                                var curr_startState = curr_endState;

                                var currentEvent = new LightEventV3(
                                    curr_end, curr_startState, curr_endState,
                                    curr_duration, target,
                                    0, 0,
                                    strobeFrequency, strobeBrightness, strobeFade
                                );

                                var key = new GroupKey(group, target);
                                latestColorEvents.put(key, currentEvent);

                                if (!colorEvents.containsKey(key)) {
                                    colorEvents.put(key, new ArrayList<>());
                                }
                                colorEvents.get(key).add(currentEvent);
                            }
                        } else {
                            for (var target : targets) {
                                var last = latestColorEvents.computeIfAbsent(new GroupKey(group, target), k -> new LightEventV3(0, new LightState(new Color(0), 0), new LightState(new Color(0), 0), 0, k.getLightId()));

                                var currentEvent = last.extendTo(baseBeat + beatOffset + durationMod);

                                var key = new GroupKey(group, target);
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

        rawRotationEvents.forEach(rawBoxGroup -> {
            var boxGroupObj = rawBoxGroup.getAsJsonObject();
            var baseBeat = JsonUtil.getOrDefault(boxGroupObj, "b", JsonElement::getAsFloat, 0f);
            var group = JsonUtil.getOrDefault(boxGroupObj, "g", JsonElement::getAsInt, 0);
            var rawSubEvents = boxGroupObj.getAsJsonArray("e");
            ArrayList<Integer> coveredIds = new ArrayList<>();
            var lightCount = getLightCount(group);

            rawSubEvents.forEach(rawSubEvent -> {
                var subEvent = rawSubEvent.getAsJsonObject();

                var rawFilter = subEvent.getAsJsonObject("f");

                var filter = processFilter(lightCount, coveredIds, rawFilter);

                // 0 = step, 1 = wave
                var beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 0f);
                var beatDistributionType = JsonUtil.getOrDefault(subEvent, "d", JsonElement::getAsInt, 0);

                float maxBeat = beatDistributionValue;
                if (beatDistributionType % 2 == 0) {
                    maxBeat *= filter.chunkCount();
                }

                // 0 = step, 1 = wave
                var rotationDistributionValue = JsonUtil.getOrDefault(subEvent, "s", JsonElement::getAsFloat, 1f);
                var rotationDistributionType = JsonUtil.getOrDefault(subEvent, "t", JsonElement::getAsInt, 0);

                float maxRotation0 = rotationDistributionValue;
                if (rotationDistributionType % 2 == 0) {
                    maxRotation0 *= filter.chunkCount();
                }

                // whether distribution affects the first event in the sequence
                boolean affectsFirst = JsonUtil.getOrDefault(subEvent, "b", JsonElement::getAsInt, 0) > 0;

                var rawAxis = JsonUtil.getOrDefault(subEvent, "a", JsonElement::getAsInt, 0);
                boolean flipAxis = JsonUtil.getOrDefault(subEvent, "r", JsonElement::getAsInt, 0) > 0;

                var axis = TransformState.Axis.values()[rawAxis % 3];

                AtomicReference<Boolean> doDistribution = new AtomicReference<>(affectsFirst);

                var rawEvents = subEvent.getAsJsonArray("l");

                var maxDuration = maxBeat;
                var maxRotation = maxRotation0;
                rawEvents.forEach(rawEvent -> {
                    var eventData = rawEvent.getAsJsonObject();

                    var beatOffset = JsonUtil.getOrDefault(eventData, "b", JsonElement::getAsFloat, 0f);

                    // 0 = transition, 1 = extend
                    var transitionType = JsonUtil.getOrDefault(eventData, "p", JsonElement::getAsInt, 0);

                    var easing = JsonUtil.getOrDefault(eventData, "e", JsonElement::getAsInt, 0);
                    var easingFunction = Easing.getEasing(String.valueOf(easing));

                    var magnitude = JsonUtil.getOrDefault(eventData, "r", JsonElement::getAsInt, 0);

                    // 0 = auto, 1 = cw, 2 = ccw
                    var direction = JsonUtil.getOrDefault(eventData, "o", JsonElement::getAsInt, 0);

                    var loopCount = JsonUtil.getOrDefault(eventData, "l", JsonElement::getAsInt, 0);


                    for (var targetSet : filter) {
                        var targets = targetSet.getA();
                        var durationMod = doDistribution.get() ? targetSet.getB() * maxDuration : 0f;
                        var distributionMod = doDistribution.get() ? targetSet.getC() * maxRotation : 0f;

                    }

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
