package com.beatcraft.client.beatmap.data;

public enum NoteType {
    RED,
    BLUE,
    FAKE_RED,
    FAKE_BLUE;

    public NoteType opposite() {
        return (this == BLUE || this == RED)
            ? (this == BLUE) ? RED : BLUE
            : (this == FAKE_BLUE) ? FAKE_RED : FAKE_BLUE;
    }
}