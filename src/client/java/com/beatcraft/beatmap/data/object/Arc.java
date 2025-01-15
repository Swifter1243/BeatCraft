package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Arc extends GameplayObject {

    private float headAngleOffset;
    private CutDirection headCutDirection;
    private float headMagnitude;

    private float tailAngleOffset;
    private CutDirection tailCutDirection;
    private float tailMagnitude;

    private NoteType noteType;
    private Color color;

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (noteType == NoteType.RED) {
            color = setDifficulty.getColorScheme().getNoteLeftColor();
        } else {
            color = setDifficulty.getColorScheme().getNoteRightColor();
        }
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Difficulty difficulty) {
        return super.loadV3(json, difficulty);
    }

    @Override
    public GameplayObject loadV4(JsonObject json, JsonArray colorNoteData, Difficulty difficulty) {
        return super.loadV4(json, colorNoteData, difficulty);
    }
}
