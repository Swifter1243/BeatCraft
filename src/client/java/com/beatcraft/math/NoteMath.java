package com.beatcraft.math;

import com.beatcraft.beatmap.data.CutDirection;
import org.joml.Math;
import org.joml.Quaternionf;

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

    private static Quaternionf rotationZDegrees(float degrees) {
        return new Quaternionf().rotateZ(Math.toRadians(degrees));
    }

    public static Quaternionf rotationFromCut(CutDirection cutDirection) {
        return switch (cutDirection) {
            case UP -> rotationZDegrees(180);
            case DOWN, DOT -> new Quaternionf();
            case LEFT -> rotationZDegrees(90);
            case RIGHT -> rotationZDegrees(-90);
            case UP_LEFT -> rotationZDegrees(135);
            case UP_RIGHT -> rotationZDegrees(-135);
            case DOWN_LEFT -> rotationZDegrees(45);
            case DOWN_RIGHT -> rotationZDegrees(-45);
        };
    }
}
