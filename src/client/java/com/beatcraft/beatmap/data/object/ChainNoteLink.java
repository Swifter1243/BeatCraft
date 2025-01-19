package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Vector3f;

public class ChainNoteLink extends GameplayObject implements ScorableObject {
    private float angleOffset = 0;
    private NoteType noteType;
    private Color color;
    private boolean disableNoteLook = false;
    private boolean disableNoteGravity = false;

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (noteType == NoteType.RED) {
            color = setDifficulty.getColorScheme().getNoteLeftColor();
        } else {
            color = setDifficulty.getColorScheme().getNoteRightColor();
        }
    }

    @Override
    public ChainNoteLink loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        noteType = NoteType.values()[json.get("c").getAsInt()];

        applyColorScheme(difficulty.getSetDifficulty());

        return this;
    }

    public ChainNoteLink loadV4(JsonObject json, JsonArray chainMetaData, NoteType color, Difficulty difficulty) {

        this.noteType = color;
        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs();

        applyColorScheme(difficulty.getSetDifficulty());

        loadJumps(difficulty.getInfo());

        return this;
    }

    public void setAngleOffset(float angle) {
        angleOffset = angle;
    }

    public void setPos(Vector3f pos) {
        x = pos.x;
        y = pos.y;
        beat = pos.z;
    }

    public float getAngleOffset() {
        return angleOffset;
    }

    public CutDirection getCutDirection() {
        return CutDirection.DOT;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public Color getColor() {
        return color;
    }

    public boolean isNoteLookDisable() {
        return disableNoteLook;
    }

    public boolean isNoteGravityDisabled() {
        return disableNoteGravity;
    }

    @Override
    public NoteType score$getNoteType() {
        return getNoteType();
    }

    @Override
    public CutDirection score$getCutDirection() {
        return getCutDirection();
    }
}
