package com.beatcraft.math;

public class GenericMath {
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
}
