package com.beatcraft.lightshow.lights;

public class TransformState {

    public enum Axis {
        UNKNOWN,
        TX,
        TY,
        TZ,
        RX,
        RY,
        RZ
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
