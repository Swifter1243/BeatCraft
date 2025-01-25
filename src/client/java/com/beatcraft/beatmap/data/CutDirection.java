package com.beatcraft.beatmap.data;

public enum CutDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    UP_LEFT,
    UP_RIGHT,
    DOWN_LEFT,
    DOWN_RIGHT,
    DOT;

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
