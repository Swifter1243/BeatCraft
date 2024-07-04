package com.beatcraft.math;

import com.beatcraft.data.CutDirection;
import org.joml.Math;
import org.joml.Quaternionf;

public class NoteMath {
    private static Quaternionf rotationZDegrees(float degrees) {
        return new Quaternionf().rotateZ(Math.toRadians(degrees));
    }

    public static Quaternionf rotationFromCut(CutDirection cutDirection) {
        return switch (cutDirection) {
            case UP -> rotationZDegrees(180);
            case DOWN, DOT -> new Quaternionf();
            case LEFT -> rotationZDegrees(-90);
            case RIGHT -> rotationZDegrees(90);
            case UP_LEFT -> rotationZDegrees(-135);
            case UP_RIGHT -> rotationZDegrees(135);
            case DOWN_LEFT -> rotationZDegrees(-45);
            case DOWN_RIGHT -> rotationZDegrees(45);
        };
    }
}
