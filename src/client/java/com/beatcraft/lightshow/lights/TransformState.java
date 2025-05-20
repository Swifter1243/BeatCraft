package com.beatcraft.lightshow.lights;

import org.jetbrains.annotations.NotNull;

public class TransformState {

    public enum Axis {
        RX,
        RY,
        RZ,
        TX,
        TY,
        TZ,
    }

    public Axis axis;
    public float value;

    public TransformState(Axis axis, float value) {
        this.set(axis, value);
    }

    public TransformState copy() {
        return new TransformState(axis, value);
    }

    public void set(Axis axis, float value) {
        this.axis = axis;
        this.value = value;
    }

    public void set(TransformState other) {
        set(other.axis, other.value);
    }

    @Override
    public @NotNull String toString() {
        return "TransformState{" +
            "axis=" + axis +
            ", value=" + value +
            '}';
    }
}
