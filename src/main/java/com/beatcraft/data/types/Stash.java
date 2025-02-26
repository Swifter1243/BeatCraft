package com.beatcraft.data.types;


// stashes n of a data type, pushing to the front and automatically deleting the oldest data
// Main use case of this is for the saber trail effect

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Stash<T> implements Iterable<T> {

    @Override
    public @NotNull Iterator<T> iterator() {
        return new StashIterator<>(this, this.pointer, this.max_size);
    }

    public static class StashIterator<T> implements Iterator<T> {

        private final Stash<T> parent;
        private int current;
        private int passed;
        private final int size;

        public StashIterator(Stash<T> parent, int pointer, int maxSize) {
            this.parent = parent;
            this.size = maxSize;
            this.current = (pointer == 0) ? maxSize - 1 : pointer - 1; // Start at the most recent element
            this.passed = 0;
        }

        @Override
        public boolean hasNext() {
            return passed < size && parent.data.get(current) != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T out = parent.data.get(current);
            current = (current == 0) ? size - 1 : current - 1; // Move backward, wrapping around
            passed++;
            return out;
        }
    }

    private final ArrayList<T> data = new ArrayList<>();

    private int pointer = 0;
    private int max_size;
    private final boolean isTrail;

    private static int saberTrailSize = 120;

    public static void updateTrailSize(int size) {
        saberTrailSize = size;
    }

    public static int getTrailSize() {
        return saberTrailSize;
    }

    public boolean isEmpty() {
        return data.getFirst() == null;
    }

    public int getSize() {
        return this.max_size;
    }

    public Stash(int capacity, boolean isTrail) {
        this.isTrail = isTrail;
        max_size = capacity;
        for (int i = 0; i <= capacity; i++) {
            data.add(null);
        }
    }

    /// Resizing will clear all data
    public void resize(int newSize) {
        max_size = newSize;
        pointer = 0;
        data.clear();
        for (int i = 0; i <= newSize; i++) {
            data.add(null);
        }
    }

    public void push(T element) {
        if (isTrail && max_size != saberTrailSize) {
            resize(saberTrailSize);
        }
        data.set(pointer, element);
        pointer = (pointer + 1) % max_size;
    }

}
