package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.data.types.Color;

public interface ScorableObject {
    Color score$getColor();
    NoteType score$getNoteType();
    CutDirection score$getCutDirection();
}
