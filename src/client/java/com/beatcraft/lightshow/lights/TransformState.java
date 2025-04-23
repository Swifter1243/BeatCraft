package com.beatcraft.lightshow.lights;

public class TransformState {

    public enum Axis {
        RX,
        RY,
        RZ,
        TX,
        TY,
        TZ,
    }

    private Axis axis;
    private float value;

    public TransformState(Axis axis, float value) {
        this.axis = axis;
        this.value = value;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public float getValue() {
        return this.value;
    }

}
