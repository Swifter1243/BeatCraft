package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;

public interface ScorableObject {
    NoteType score$getNoteType();
    CutDirection score$getCutDirection();
}
