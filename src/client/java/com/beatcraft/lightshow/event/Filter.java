package com.beatcraft.lightshow.event;

import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Filter implements Iterable<Triplet<Integer[], Float, Float>> {

    @Override
    public @NotNull Iterator<Triplet<Integer[], Float, Float>> iterator() {
        return new TargetIterator(ordering, targets);
    }

    public static class TargetIterator implements Iterator<Triplet<Integer[], Float, Float>> {

        private Integer[] ordering;
        private List<Triplet<Integer[], Float, Float>> targets;
        private int index = 0;

        protected TargetIterator(Integer[] ordering, List<Triplet<Integer[], Float, Float>> targets) {
            this.ordering = ordering;
            this.targets = targets;
        }

        @Override
        public boolean hasNext() {
            return index < ordering.length;
        }

        @Override
        public Triplet<Integer[], Float, Float> next() {
            return targets.get(ordering[index++]);
        }
    }

    // targets: {
    //     [lightID, durationMod, distributionMod],
    //     ...
    // }
    private List<Triplet<Integer[], Float, Float>> targets;
    private Integer[] ordering;

    public List<Integer> getTargets() {
        var out = new ArrayList<Integer>();
        for (var targetSet : targets) {
            for (var target : targetSet.getA()) {
                out.add(target);
            }
        }
        return out;
    }

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

            var f = ((float) i) / ((float) chunks-1);
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

    public static Filter processFilter(Random random, int lightCount, ArrayList<Integer> coveredIds, JsonObject filter) {
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
        var type = JsonUtil.getOrDefault(filter, "f", JsonElement::getAsInt, 1);

        type = type % 2;
        // division:
        // p0: number of sections to divide the objects into
        // p1: which section to process
        // step & offset:
        // p0: starting light
        // p1: step value
        var p0 = JsonUtil.getOrDefault(filter, "p", JsonElement::getAsInt, 0);
        var p1 = JsonUtil.getOrDefault(filter, "t", JsonElement::getAsInt, 0);
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

        ArrayList<float[]> weights = new ArrayList<>();
        if (reverse) {
            targets = new ArrayList<>(targets.reversed());
            for (var t : targets) {
                weights.add(new float[]{t.getB(), t.getC()});
            }
            for (int i = 0; i < targets.size(); i++) {
                var t = targets.get(i);
                targets.set(i, new Triplet<>(t.getA(), weights.get(targets.size()-1-i)[0], weights.get(targets.size()-1-i)[1]));
            }
        }

        if (type == 1) {
            targets = new ArrayList<>(getSection(targets, (float) p0, p1));
        } else {
            targets = getStepOffset(targets, Math.clamp(p0, 1, lightCount+1), Math.max(1, p1));
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

        if (reverse) {
            ordering = new ArrayList<>(ordering.reversed());
        }

        removeValues(targets, coveredIds);

        for (var arr : targets) {
            for (var t : arr.getA()) {
                if (!coveredIds.contains(t)) {
                    coveredIds.add(t);
                }
            }
        }
        return new Filter(targets, ordering.toArray(new Integer[0]));
    }

    public Filter(List<Triplet<Integer[], Float, Float>> targets, Integer[] ordering) {
        this.targets = targets;
        this.ordering = ordering;
    }

    public int chunkCount() {
        return targets.size();
    }


}
