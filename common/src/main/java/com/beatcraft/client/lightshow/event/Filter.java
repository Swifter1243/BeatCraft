package com.beatcraft.client.lightshow.event;

import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Filter implements Iterable<Filter.FilterTarget> {

    private final ArrayList<FilterTarget> targets;

    public static class FilterTarget {

        public FilterTarget(int[] lightIDs, float durationMod, float distributionMod) {
            this.lightIDs = lightIDs;
            this.durationMod = durationMod;
            this.distributionMod = distributionMod;
        }

        int[] lightIDs;
        float durationMod;
        float distributionMod;

    }

    private static ArrayList<FilterTarget> chunk(ArrayList<FilterTarget> input, int chunks) {
        var output = new ArrayList<FilterTarget>();
        var sz = input.size();

        for (var i = 0; i < chunks; ++i) {
            var start = (int) Math.floor((double) i * sz / chunks);
            var end = (int) Math.floor((double) (i+1) * sz / chunks);

            var merged = new int[end-start];

            for (int j = start; j < end; ++j) {
                merged[j-start] = input.get(j).lightIDs[0];
            }

            output.add(new FilterTarget(merged, i / (float) chunks, i / (float) chunks));
        }

        return output;
    }

    public Filter(int lightCount, ArrayList<Integer> coveredIDs, JsonObject filter) {

        var chunks = JsonUtil.getOrDefault(filter, "c", JsonElement::getAsInt, 0);
        var type = JsonUtil.getOrDefault(filter, "f", JsonElement::getAsInt, 1);
        var p0 = JsonUtil.getOrDefault(filter, "p", JsonElement::getAsInt, 1);
        var p1 = JsonUtil.getOrDefault(filter, "t", JsonElement::getAsInt, 0);
        boolean reverse = JsonUtil.getOrDefault(filter, "r", JsonElement::getAsInt, 0) > 0;
        var randomBehavior = JsonUtil.getOrDefault(filter, "n", JsonElement::getAsInt, 0);
        var randomSeed = JsonUtil.getOrDefault(filter, "s", JsonElement::getAsInt, 0);
        var limitPercent = JsonUtil.getOrDefault(filter, "l", JsonElement::getAsFloat, 1f);
        var limitBehavior = JsonUtil.getOrDefault(filter, "d", JsonElement::getAsInt, 0);

        if (chunks <= 0 || chunks > lightCount) chunks = lightCount;

        if (limitPercent == 0) limitPercent = 1f;

        var _rawTargets = new ArrayList<FilterTarget>();

        for (int i = 0; i < lightCount; ++i) {
            _rawTargets.add(new FilterTarget(
                new int[]{reverse ? lightCount-1-i : i},
                0, 0
            ));
        }

        var rawTargets = chunk(_rawTargets, chunks);

        // TODO: randomization

        Stream<FilterTarget> targets;
        var lc = rawTargets.size();

        if (type == 1) { // Division
            var range = 1f / p0;
            var low = p1 * range;
            var high = (p1 + 1) * range;

            targets = IntStream.range(0, lc).filter(i ->
                low <= (i / (float) lc) && (i / (float) lc) < high
            ).mapToObj(rawTargets::get);

        } else { // 2: Step and Offset
            if (p1 == 0) p1 = 1;
            var step = p1;
            targets = IntStream.range(0, lc).filter(i ->
                // start at p0-1 (1-indexed to 0-indexed conversion)
                // and then choose every p1-th element
                (i - (p0-1)) % step == 0
            ).mapToObj(rawTargets::get);

        }

        var preLimitedTargets = targets.collect(Collectors.toCollection(ArrayList::new));
        var preLimitedSize = preLimitedTargets.size();

        var ids = 0;
        for (var target : preLimitedTargets) {
            if (preLimitedSize != 1) {
                target.distributionMod = ids / (float) (preLimitedSize-1);
                target.durationMod = ids / (float) (preLimitedSize-1);
            }
            ++ids;
        }

        var limitedTargets = preLimitedTargets.stream()
            .limit((int) (preLimitedSize * limitPercent))
            .collect(Collectors.toCollection(ArrayList::new));

        var targetCount = limitedTargets.size();

        if (targetCount > 1) {
            var remap = (preLimitedSize - 1) / (float) (targetCount - 1);

            for (var target : limitedTargets) {
                if ((limitBehavior & 1) > 0) { // duration
                    target.durationMod *= remap;
                }
                if ((limitBehavior & 2) > 0) { // distribution
                    target.distributionMod *= remap;
                }
            }
        }


        this.targets = limitedTargets.stream()
            .map(t -> {
                var newLen = 0;
                for (var idx : t.lightIDs) {
                    if (!coveredIDs.contains(idx)) newLen++;
                }
                if (newLen == t.lightIDs.length) return t;
                var filtered = new int[newLen];
                var i = 0;
                for (var idx : t.lightIDs) {
                    if (coveredIDs.contains(idx)) continue;
                    filtered[i++] = idx;
                }
                t.lightIDs = filtered;
                return t;
            })
            .filter(t -> t.lightIDs.length > 0)
            .collect(Collectors.toCollection(ArrayList::new));

        for (var t : this.targets) {
            for (var id : t.lightIDs) {
                if (!coveredIDs.contains(id)) coveredIDs.add(id);
            }
        }

    }

    public int chunkCount() {
        return targets.size();
    }


    @Override
    public @NotNull Iterator<FilterTarget> iterator() {
        return this.new FilterIterator();
    }

    public class FilterIterator implements Iterator<FilterTarget> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < Filter.this.targets.size();
        }

        @Override
        public FilterTarget next() {
            return Filter.this.targets.get(index++);
        }
    }

}
