package com.beatcraft.utils;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {
    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }

    public static float clamp01(float x) {
        return Math.clamp(x, 0, 1);
    }

    public static float secondsToBeats(float seconds, float bpm) {
        return seconds * (bpm / 60);
    }

    public static float beatsToSeconds(float beats, float bpm) {
        return beats * (60 / bpm);
    }

    public static final float DEG2RAD = 180f / (float)Math.PI;
    public static final float RAD2DEG = (float)Math.PI / 180f;

    public static Quaternionf eulerToQuaternion(Vector3f euler) {
        euler = euler.mul(DEG2RAD);
        return new Quaternionf().rotateXYZ(euler.x, euler.y, euler.z);
    }
}
