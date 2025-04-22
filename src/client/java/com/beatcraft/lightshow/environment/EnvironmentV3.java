package com.beatcraft.lightshow.environment;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.*;

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
    protected abstract void linkLightEvents(int group, int lightID, List<LightEventV3> events);

    private static ArrayList<int[]> reChunk(ArrayList<int[]> targets, int chunks) {
        int total = targets.size();
        ArrayList<int[]> result = new ArrayList<>(chunks);

        double chunkSize = (double) total / chunks;
        double currentIndex = 0;

        for (int i = 0; i < chunks; i++) {
            int start = (int) Math.round(currentIndex);
            currentIndex += chunkSize;
            int end = (int) Math.round(currentIndex);

            List<int[]> subList = targets.subList(start, end);
            int totalLength = subList.stream().mapToInt(arr -> arr.length).sum();
            int[] merged = new int[totalLength];

            int pos = 0;
            for (int[] arr : subList) {
                System.arraycopy(arr, 0, merged, pos, arr.length);
                pos += arr.length;
            }

            result.add(merged);
        }

        return result;
    }

    private static int[] flatten(List<int[]> list) {
        int totalLength = list.stream().mapToInt(arr -> arr.length).sum();
        int[] result = new int[totalLength];

        int pos = 0;
        for (int[] arr : list) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }

        return result;
    }

    private static void removeValues(ArrayList<int[]> arrays, List<Integer> toRemove) {
        var removeSet = new java.util.HashSet<>(toRemove);

        for (int i = 0; i < arrays.size(); i++) {
            int[] original = arrays.get(i);

            int count = 0;
            for (int val : original) {
                if (!removeSet.contains(val)) {
                    count++;
                }
            }

            int[] filtered = new int[count];
            int index = 0;
            for (int val : original) {
                if (!removeSet.contains(val)) {
                    filtered[index++] = val;
                }
            }

            arrays.set(i, filtered);
        }
    }


    private List<int[]> processFilter(int lightCount, ArrayList<Integer> coveredIds, JsonObject filter) {
        var targets = new ArrayList<int[]>();
        for (int i = 0; i < lightCount; i++) { targets.add(new int[]{i}); }


        // divide available objects by `chunks`. treat each section as a single object
        var chunks = JsonUtil.getOrDefault(filter, "c", JsonElement::getAsInt, 0);

        if (chunks == 0) { chunks = targets.size(); }
        chunks = Math.clamp(chunks, 1, targets.size());

        targets = reChunk(targets, chunks);

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

        random.setSeed(randomSeed);

        // what percentage of objects to affect
        var limitPercent = JsonUtil.getOrDefault(filter, "l", JsonElement::getAsFloat, 0f);
        // 0 = none, 1 = duration, 2 = distribution
        var limitBehavior = JsonUtil.getOrDefault(filter, "d", JsonElement::getAsInt, 0);

        // limitPercent = 75 means 75% of lights are affected
        // if limitBehavior & 2: the event duration is 75% as long too
        // idk how distribution is affected

        removeValues(targets, coveredIds);

        for (var arr : targets) {
            for (var t : arr) {
                if (!coveredIds.contains(t)) {
                    coveredIds.add(t);
                }
            }
        }

        if (randomBehavior == 2) {
            ArrayList<int[]> reordered = new ArrayList<>();
            var s = targets.size();
            for (int i = 0; i < s; i++) {
                var index = random.nextInt(0, targets.size());
                reordered.add(targets.remove(index));
            }
            targets = reordered;
        }



        return targets;
    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var rawColorEventBoxes = json.getAsJsonArray("lightColorEventBoxGroups");

        var rawRotationEvents = json.getAsJsonArray("lightRotationEventBoxGroups");

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

                var filter = subEvent.getAsJsonObject("f");

                var targets = processFilter(lightCount, coveredIds, filter);

                // 0 = step, 1 = wave
                var beatDistributionValue = JsonUtil.getOrDefault(subEvent, "w", JsonElement::getAsFloat, 0f);
                var beatDistributionType = JsonUtil.getOrDefault(subEvent, "d", JsonElement::getAsInt, 0);

                // 0 = step, 1 = wave
                var brightnessDistributionValue = JsonUtil.getOrDefault(subEvent, "r", JsonElement::getAsInt, 0) > 0;
                var brightnessDistributionType = JsonUtil.getOrDefault(subEvent, "t", JsonElement::getAsInt, 0);

                // whether distribution affects the first event in the sequence
                var brightnessDistributionAffectsFirst = JsonUtil.getOrDefault(subEvent, "b", JsonElement::getAsInt, 0);


                var brightnessDistributionEasing = JsonUtil.getOrDefault(subEvent, "i", JsonElement::getAsInt, -1);
                var easingFunction = Easing.getEasing(String.valueOf(brightnessDistributionEasing));




            });

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
