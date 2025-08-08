package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.Info;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.beatmap.object.physical.PhysicalColorNote;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Vector3f;

public class Arc extends GameplayObject {

    public enum MidAnchorMode {
        STRAIGHT,
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    private CutDirection headCutDirection;
    private float headMagnitude;

    private CutDirection tailCutDirection;
    private float tailMagnitude;
    private float tailBeat;
    private float tailX;
    private float tailY;

    private MidAnchorMode midAnchorMode;

    private boolean _hasHeadNote = false;
    private boolean _hasTailNote = false;

    private PhysicalColorNote headNote = null;
    private PhysicalColorNote tailNote = null;

    private NoteType noteType;
    private Color color;

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (noteType == NoteType.RED) {
            color = setDifficulty.getColorScheme().getNoteLeftColor();
        } else {
            color = setDifficulty.getColorScheme().getNoteRightColor();
        }
    }

    private void checkForNotes(float b, float x, float y, float tb, float tx, float ty, Difficulty difficulty) {
        difficulty.colorNotes.forEach(c -> {
            if (c.getData().getBeat() == b && c.getData().getX() == x && c.getData().getY() == y) {
                _hasHeadNote = true;
                headNote = c;
            }
            if (c.getData().getBeat() == tb && c.getData().getX() == tx && c.getData().getY() == ty) {
                _hasTailNote = true;
                tailNote = c;
            }
        });
    }

    public Arc loadV2(JsonObject json, Difficulty difficulty) {
        noteType = NoteType.values()[json.get("_colorType").getAsInt()];

        beat = json.get("_headTime").getAsFloat();
        tailBeat = json.get("_tailTime").getAsFloat();

        njs = difficulty.getSetDifficulty().getNjs(beat);
        offset = difficulty.getSetDifficulty().getOffset();

        x = json.get("_headLineIndex").getAsFloat();
        y = json.get("_headLineLayer").getAsFloat();

        tailX = json.get("_tailLineIndex").getAsFloat();
        tailY = json.get("_tailLineLayer").getAsFloat();

        headCutDirection = CutDirection.values()[json.get("_headCutDirection").getAsInt()];
        tailCutDirection = CutDirection.values()[json.get("_tailCutDirection").getAsInt()];

        headMagnitude = json.get("_headControlPointLengthMultiplier").getAsFloat();
        tailMagnitude = json.get("_tailControlPointLengthMultiplier").getAsFloat();

        midAnchorMode = MidAnchorMode.values()[json.get("_sliderMidAnchorMode").getAsInt()];

        checkForNotes(beat, x, y, tailBeat, tailX, tailY, difficulty);
        loadJumps(difficulty.getInfo());
        applyColorScheme(difficulty.getSetDifficulty());

        return this;
    }

    public Arc loadV3(JsonObject json, Difficulty difficulty) {

        noteType = NoteType.values()[JsonUtil.getOrDefault(json, "c", JsonElement::getAsInt, 0)];

        beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);
        tailBeat = JsonUtil.getOrDefault(json, "tb", JsonElement::getAsFloat, 0f);

        njs = difficulty.getSetDifficulty().getNjs(beat);
        offset = difficulty.getSetDifficulty().getOffset();

        x = JsonUtil.getOrDefault(json, "x", JsonElement::getAsFloat, 0f);
        y = JsonUtil.getOrDefault(json, "y", JsonElement::getAsFloat, 0f);

        tailX = JsonUtil.getOrDefault(json, "tx", JsonElement::getAsFloat, 0f);
        tailY = JsonUtil.getOrDefault(json, "ty", JsonElement::getAsFloat, 0f);

        headCutDirection = CutDirection.values()[JsonUtil.getOrDefault(json, "d", JsonElement::getAsInt, 0)];
        tailCutDirection = CutDirection.values()[JsonUtil.getOrDefault(json, "tc", JsonElement::getAsInt, 0)];

        headMagnitude = JsonUtil.getOrDefault(json, "mu", JsonElement::getAsFloat, 0f);
        tailMagnitude = JsonUtil.getOrDefault(json, "tmu", JsonElement::getAsFloat, 0f);

        midAnchorMode = MidAnchorMode.values()[JsonUtil.getOrDefault(json, "m", JsonElement::getAsInt, 0)];

        checkForNotes(beat, x, y, tailBeat, tailX, tailY, difficulty);
        loadJumps(difficulty.getInfo());
        applyColorScheme(difficulty.getSetDifficulty());

        return this;
    }

    public Arc loadV4(JsonObject json, JsonArray arcsData, JsonArray colorNotesData, Difficulty difficulty) {

        beat = JsonUtil.getOrDefault(json, "hb", JsonElement::getAsFloat, 0f);
        tailBeat = JsonUtil.getOrDefault(json, "tb", JsonElement::getAsFloat, 0f);

        njs = difficulty.getSetDifficulty().getNjs(beat);
        offset = difficulty.getSetDifficulty().getOffset();

        int arcDataIndex = JsonUtil.getOrDefault(json, "ai", JsonElement::getAsInt, 0);
        int headNoteDataIndex = JsonUtil.getOrDefault(json, "hi", JsonElement::getAsInt, 0);
        int tailNoteDataIndex = JsonUtil.getOrDefault(json, "ti", JsonElement::getAsInt, 0);

        JsonObject arcMetaData = arcsData.get(arcDataIndex).getAsJsonObject();
        JsonObject headMetaData = colorNotesData.get(headNoteDataIndex).getAsJsonObject();
        JsonObject tailMetaData = colorNotesData.get(tailNoteDataIndex).getAsJsonObject();

        x = JsonUtil.getOrDefault(headMetaData, "x", JsonElement::getAsFloat, 0f);
        y = JsonUtil.getOrDefault(headMetaData, "y", JsonElement::getAsFloat, 0f);
        noteType = NoteType.values()[JsonUtil.getOrDefault(headMetaData, "c", JsonElement::getAsInt, 0)];
        headCutDirection = CutDirection.values()[JsonUtil.getOrDefault(headMetaData, "d", JsonElement::getAsInt, 0)];

        tailX = JsonUtil.getOrDefault(tailMetaData, "x", JsonElement::getAsFloat, 0f);
        tailY = JsonUtil.getOrDefault(tailMetaData, "y", JsonElement::getAsFloat, 0f);
        tailCutDirection = CutDirection.values()[JsonUtil.getOrDefault(tailMetaData, "d", JsonElement::getAsInt, 0)];

        headMagnitude = JsonUtil.getOrDefault(arcMetaData, "m", JsonElement::getAsFloat, 1.0f);
        tailMagnitude = JsonUtil.getOrDefault(arcMetaData, "tm", JsonElement::getAsFloat, 1.0f);
        midAnchorMode = MidAnchorMode.values()[JsonUtil.getOrDefault(arcMetaData, "a", JsonElement::getAsInt, 0)];

        checkForNotes(beat, x, y, tailBeat, tailX, tailY, difficulty);
        loadJumps(difficulty.getInfo());
        applyColorScheme(difficulty.getSetDifficulty());

        return this;
    }

    public static Vector3f cutDirectionToControlPoint(CutDirection cutDirection) {
        return switch (cutDirection) {
            case UP -> new Vector3f(0f, 1f, -1E-05f);
            case DOWN -> new Vector3f(0f, -1f, -1E-05f);
            case RIGHT -> new Vector3f(-1f, 0f, -1E-05f);
            case LEFT -> new Vector3f(1f, 0f, -1E-05f);
            case UP_RIGHT -> new Vector3f(-0.70710677f, 0.70710677f, -1E-05f);
            case UP_LEFT -> new Vector3f(0.70710677f, 0.70710677f, -1E-05f);
            case DOWN_RIGHT -> new Vector3f(-0.70710677f, -0.70710677f, -1E-05f);
            case DOWN_LEFT -> new Vector3f(0.70710677f, -0.70710677f, -1E-05f);
            case DOT -> new Vector3f(0, 0, 0);
        };
    }

    public Color getColor() {
        return color;
    }

    public float getTailX() {
        return tailX;
    }

    public float getTailY() {
        return tailY;
    }

    public float getTailBeat() {
        return tailBeat;
    }

    public CutDirection getTailCutDirection() {
        return tailCutDirection;
    }

    public CutDirection getHeadCutDirection() {
        return headCutDirection;
    }

    public float getHeadMagnitude() {
        return headMagnitude;
    }

    public float getTailMagnitude() {
        return tailMagnitude;
    }

    public boolean hasHeadNote() {
        return _hasHeadNote;
    }

    public boolean hasTailNote() {
        return _hasTailNote;
    }

    public PhysicalColorNote getHeadNote() {
        return headNote;
    }

    public PhysicalColorNote getTailNote() {
        return tailNote;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public MidAnchorMode getMidAnchorMode() {
        return midAnchorMode;
    }

}
