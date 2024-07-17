package com.beatcraft.utils;

import org.joml.*;
import org.joml.Math;

public class MathUtil {
    public static final float DEG2RAD = (float)Math.PI / 180f;
    public static final float RAD2DEG = 180f / (float)Math.PI;

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
    public static Quaternionf eulerToQuaternion(Vector3f euler) {
        return new Quaternionf().rotateXYZ(euler.x * DEG2RAD, euler.y * DEG2RAD, euler.z * DEG2RAD);
    }
    public static float normalizeAngle(float angle) {
        angle = angle % 360;
        return angle < 0 ? (angle + 360) : angle;
    }
    public static float degreesBetween(float a, float b) {
        a = normalizeAngle(a);
        b = normalizeAngle(b);
        float difference = Math.abs(a - b);
        return Math.min(difference, 360 - difference);
    }
    public static float getVectorAngleDegrees(Vector2f v) {
        float radians = Math.atan2(v.y, v.x);
        return (float) Math.toDegrees(radians);
    }
}
