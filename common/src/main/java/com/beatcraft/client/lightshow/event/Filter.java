package com.beatcraft.client.lightshow.event;

import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Filter implements Iterable<Triplet<Integer[], Float, Float>> {


    public static Filter processFilter(int lightCount, ArrayList<Integer> coveredIds, JsonObject filter) {

        var chunks = JsonUtil.getOrDefault(filter, "c", JsonElement::getAsInt, 0);
        var type = JsonUtil.getOrDefault(filter, "f", JsonElement::getAsInt, 0);
        var p0 = JsonUtil.getOrDefault(filter, "p", JsonElement::getAsInt, 0);
        var p1 = JsonUtil.getOrDefault(filter, "t", JsonElement::getAsInt, 0);
        boolean reverse = JsonUtil.getOrDefault(filter, "r", JsonElement::getAsInt, 0) > 0;
        var randomBehavior = JsonUtil.getOrDefault(filter, "n", JsonElement::getAsInt, 0);
        var randomSeed = JsonUtil.getOrDefault(filter, "s", JsonElement::getAsInt, 0);
        var limitPercent = JsonUtil.getOrDefault(filter, "l", JsonElement::getAsFloat, 0f);
        var limitBehavior = JsonUtil.getOrDefault(filter, "d", JsonElement::getAsInt, 0);

        if (chunks == 0) chunks = lightCount;

        var chunkSize = (int) Math.ceil(lightCount / (float) chunks);
        var chunkCount = (int) Math.ceil(lightCount / (float) chunkSize);

        if (type == 1) { // division = 1
            int sector = (int) Math.ceil(chunkCount / (float) p0);
            if (reverse) {
                int start = chunkCount - sector * p1 - 1;
                return new Filter(
                    start, Math.max(0, start - sector + 1),
                    lightCount, randomBehavior, randomSeed, chunkSize,
                    limitBehavior, limitPercent, coveredIds
                );
            }
            int sector2 = sector * p1;
            return new Filter(
                sector2, Math.min(chunkCount - 1, sector2 + sector - 1),
                lightCount, randomBehavior, randomSeed,
                chunkSize, limitBehavior, limitPercent, coveredIds
            );
        } else { // step/offset = 2
            var size = (float) (chunkCount - p0);
            var count = p1 == 0 ? 1 : (int) Math.ceil(size / p1);
            if (reverse) {
                return new Filter(
                    chunkCount - 1 - p0, -p1, count, lightCount,
                    randomBehavior, randomSeed, chunkSize,
                    limitBehavior, limitPercent, coveredIds
                );
            }
            return new Filter(
                p0, p1, count,
                lightCount, randomBehavior, randomSeed,
                chunkSize, limitBehavior, limitPercent, coveredIds
            );
        }

    }

    protected final int start, step, count, lightCount, randomBehavior, randomSeed, chunkSize, limitBehavior, visibleCount, chunkCount;
    protected final float limitPercentage;

    private ArrayList<Triplet<Integer[], Float, Float>> targets;

    private void randomize(ArrayList<Integer> values, int seed) {
        var random = new Random(seed);

        var out = new ArrayList<Integer>();
        for (var i : values) {
            var insertionIndex = random.nextInt(out.size() + 1);
            if (insertionIndex == out.size()) {
                out.add(i);
                continue;
            }
            out.add(out.get(insertionIndex));
            out.set(insertionIndex, i);
        }

        values.clear();
        values.addAll(out);

    }

    private void reOrder(ArrayList<Integer> ordering, boolean randomize, int limit, int count, int seed) {
        var out = new ArrayList<Integer>();
        var index = 0;
        if (randomize) {
            var random = new Random(seed);
            var picked = 0;
            for (var o : ordering) {
                if (random.nextInt(count - index) > limit - picked) {
                    picked++;
                    out.add(o);
                }
                else {
                    out.add(-1);
                }
                index++;
            }
        } else {
            for (var o : ordering) {
                if (index < limit) {
                    out.add(o);
                } else {
                    out.add(-1);
                }
                index++;
            }
        }

        ordering.clear();
        ordering.addAll(out);

    }

    private ArrayList<Pair<Integer, Integer>> zip(ArrayList<Integer> left, ArrayList<Integer> right) {
        var count = Math.min(left.size(), right.size());
        var out = new ArrayList<Pair<Integer, Integer>>();
        for (var i = 0; i < count; i++) {
            var r = right.get(i);
            if (r == -1) continue;
            out.add(new Pair<>(left.get(i), r));
        }
        return out;
    }

    public Filter(int start, int end, int lightCount, int randomBehavior, int randomSeed, int chunkSize, int limitBehavior, float limitPercent, ArrayList<Integer> coveredIds) {
        this(
            start, end - start >= 0 ? 1 : -1,
            Math.abs(end - start) + 1, lightCount,
            randomBehavior, randomSeed, chunkSize,
            limitBehavior, limitPercent, coveredIds
        );
    }

    public Filter(int start, int step, int count, int lightCount, int randomBehavior, int randomSeed, int chunkSize, int limitBehavior, float limitPercentage, ArrayList<Integer> coveredIds) {
        this.start = start;
        this.step = step;
        this.count = count;
        this.lightCount = lightCount;
        this.randomBehavior = randomBehavior;
        this.randomSeed = randomSeed;
        this.chunkSize = chunkSize;
        this.limitBehavior = limitBehavior;
        this.limitPercentage = limitPercentage == 0f ? 1f : limitPercentage;
        this.visibleCount = this.limitPercentage == 1f ? count : (int) Math.ceil(limitPercentage * (float) count);

        // chunk count needs to be known, so enumerate immediately

        var values = new ArrayList<Integer>();
        var value = start;
        for (int i = 0; i < lightCount; i++) {
            values.add(value);
            value += step;
        }

        if (randomBehavior != 0 && ((randomBehavior & 0b1) == 0)) {
            randomize(values, randomSeed);
        }
        var ordering = new ArrayList<Integer>();
        for (var o = 0; o < count; o++) ordering.add(o);

        if (visibleCount > 0) {
            reOrder(ordering, (randomBehavior & 0b10) > 0, visibleCount, count, randomSeed);
        }

        var orderedIds = zip(values, ordering);
        var limitedIndex = 0;

        var maxDurationIndex = 1;
        var maxDistributionIndex = 1;

        var limitDuration = (limitBehavior & 0b1) > 0;
        var limitDistribution = (limitBehavior & 0b10) > 0;

        targets = new ArrayList<>();
        for (var pair : orderedIds) {
            var id = pair.getA();
            var index = pair.getB();

            var durationMod = limitDuration ? limitedIndex : index;
            var distributionMod = limitDistribution ? limitedIndex : index;
            maxDurationIndex = Math.max(maxDurationIndex, durationMod);
            maxDistributionIndex = Math.max(maxDistributionIndex, distributionMod);

            var indices = new ArrayList<Integer>();
            for (int localIdx = 0; localIdx < chunkSize; localIdx++) {
                var n = id * chunkSize + localIdx;
                if (n >= lightCount) break;

                indices.add(n);

            }

            coveredIds.addAll(indices);
            targets.add(new Triplet<>(indices.toArray(new Integer[0]), (float) durationMod, (float) distributionMod));

            limitedIndex++;
        }
        chunkCount = targets.size();

        for (int i = 0; i < targets.size(); i++) {
            var t = targets.get(i);
            targets.set(
                i,
                new Triplet<>(t.getA(), t.getB() / (float) maxDurationIndex, t.getC() / (float) maxDistributionIndex)
            );
        }


    }

    public int chunkCount() {
        return chunkCount;
    }


    @Override
    public @NotNull Iterator<Triplet<Integer[], Float, Float>> iterator() {
        return new FilterIterator(this);
    }

    public static class FilterIterator implements Iterator<Triplet<Integer[], Float, Float>> {

        private final Filter filter;
        private int index = 0;

        public FilterIterator(Filter filter) {
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return index < filter.targets.size();
        }

        @Override
        public Triplet<Integer[], Float, Float> next() {
            return filter.targets.get(index++);
        }
    }

}
