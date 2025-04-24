package com.beatcraft.lightshow.lights;

public record TransformState(com.beatcraft.lightshow.lights.TransformState.Axis axis, float value) {

    public enum Axis {
        RX,
        RY,
        RZ,
        TX,
        TY,
        TZ,
    }

    public TransformState copy() {
        return new TransformState(axis, value);
    }

}
