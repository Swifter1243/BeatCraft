package com.beatcraft.animation.pointdefinition;

import java.util.function.Function;

public class Point<T> {
    private T value;
    private float time = 0;
    private Function<Float, Float> easing;
    private boolean spline;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
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
