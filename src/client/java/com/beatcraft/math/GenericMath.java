package com.beatcraft.math;

public class GenericMath {
    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }

    public static float clamp01(float x) {
        return Math.clamp(x, 0, 1);
    }
}
