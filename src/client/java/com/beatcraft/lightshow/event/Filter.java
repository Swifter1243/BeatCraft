package com.beatcraft.lightshow.event;

import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Triplet;

import java.util.Iterator;
import java.util.List;

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
            return index < ordering.length-1;
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

    public Filter(List<Triplet<Integer[], Float, Float>> targets, Integer[] ordering) {
        this.targets = targets;
        this.ordering = ordering;
    }

    public int chunkCount() {
        return targets.size();
    }


}
