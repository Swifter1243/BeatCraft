package com.beatcraft.client.animation.pointdefinition;

import com.beatcraft.client.animation.base_providers.ValueReader;

import java.util.function.Function;

public class Point<T> {
    private T value;
    private float time = 0;
    private Function<Float, Float> easing;
    private boolean spline;
    private boolean fromProvider = false;
    private ValueReader<T> provider;

    public T getValue() {
        if (fromProvider) {
            return provider.get();
        }
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setValue(ValueReader<T> reader) {
        provider = reader;
        fromProvider = true;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public Function<Float, Float> getEasing() {
        return easing;
    }

    public void setEasing(Function<Float, Float> easing) {
        this.easing = easing;
    }

    public boolean isSpline() {
        return spline;
    }

    public void setSpline(boolean spline) {
        this.spline = spline;
    }
}
