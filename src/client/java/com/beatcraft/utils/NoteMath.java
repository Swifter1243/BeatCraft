package com.beatcraft.utils;

import com.beatcraft.beatmap.data.CutDirection;

public class NoteMath {
    public static Jumps getJumps(float njs, float offset, float bpm) {
        float hjd = 4;
        float num = 60 / bpm;

        while (njs * num * hjd > 17.999f)
            hjd /= 2;

        hjd += offset;

        if (hjd < 0.25f) hjd = 0.25f;
        float jd = hjd * num * njs * 2;

        return new Jumps(hjd, jd);
    }

    public record Jumps(float halfDuration, float jumpDistance) {}

    public static float degreesFromCut(CutDirection cutDirection) {
        return switch (cutDirection) {
            case UP -> 180;
            case DOWN, DOT -> 0;
            case LEFT -> 90;
            case RIGHT -> -90;
            case UP_LEFT -> 135;
            case UP_RIGHT -> -135;
            case DOWN_LEFT -> 45;
            case DOWN_RIGHT -> -45;
        };
    }
}
