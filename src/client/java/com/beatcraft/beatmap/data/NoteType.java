package com.beatcraft.beatmap.data;

public enum NoteType {
    RED,
    BLUE;

    public NoteType opposite() {
        return (this == BLUE) ? RED : BLUE;
    }
}
