package com.beatcraft.client.beatmap.data;

public enum CutDirection {
    UP(180),
    DOWN(0),
    LEFT(-90),
    RIGHT(90),
    UP_LEFT(-135),
    UP_RIGHT(135),
    DOWN_LEFT(-45),
    DOWN_RIGHT(45),
    DOT(0);

    public final float baseAngleDegrees;

    CutDirection(float baseAngleDegrees) {
        this.baseAngleDegrees = baseAngleDegrees;
    }

    public CutDirection opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case UP_LEFT -> DOWN_RIGHT;
            case UP_RIGHT -> DOWN_LEFT;
            case DOWN_LEFT -> UP_RIGHT;
            case DOWN_RIGHT -> UP_LEFT;
            case DOT -> DOT;
        };
    }
}
