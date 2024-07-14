package com.beatcraft.utils;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {
    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }

    public static float clamp01(float x) {
        return Math.clamp(0, 1, x);
    }

    public static float secondsToBeats(float seconds, float bpm) {
        return seconds * (bpm / 60);
    }

    public static float beatsToSeconds(float beats, float bpm) {
        return beats * (60 / bpm);
    }

    public static final float DEG2RAD = (float)Math.PI / 180f;
    public static final float RAD2DEG = 180f / (float)Math.PI;

    public static Quaternionf eulerToQuaternion(Vector3f euler) {
        return new Quaternionf().rotateXYZ(euler.x * DEG2RAD, euler.y * DEG2RAD, euler.z * DEG2RAD);
    }
}
