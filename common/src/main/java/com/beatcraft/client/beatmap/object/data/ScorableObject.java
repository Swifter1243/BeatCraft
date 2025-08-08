package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.common.data.types.Color;

public interface ScorableObject {
    Color score$getColor();
    NoteType score$getNoteType();
    CutDirection score$getCutDirection();
}