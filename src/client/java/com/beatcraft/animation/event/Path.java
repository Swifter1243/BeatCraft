package com.beatcraft.animation.event;

public abstract class Path<T> {
    @FunctionalInterface
    public interface Interpolation<T> {
        T apply(T a, T b, float f);
    }

    public abstract T interpolate(float time, Interpolation<T> interpolation);
}
