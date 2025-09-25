package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.Info;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class ColorNote extends GameplayObject implements ScorableObject {
    private float angleOffset;
    private CutDirection cutDirection;
    private NoteType noteType;
    private Color color;
    private boolean disableNoteLook = false;
    private boolean disableNoteGravity = false;

    public ColorNote(BeatmapController map) {
        super(map);
    }

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (getNoteType() == NoteType.RED) {
            color = setDifficulty.getColorScheme().getNoteLeftColor();
        } else {
            color = setDifficulty.getColorScheme().getNoteRightColor();
        }
    }

    @Override
    public ColorNote loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        angleOffset = 0; // not existent in V2
        cutDirection = CutDirection.values()[json.get("_cutDirection").getAsInt()];
        noteType = NoteType.values()[json.get("_type").getAsInt()];

        applyColorScheme(difficulty.getSetDifficulty());

        if (json.has("_customData")) {
            JsonObject customData = json.get("_customData").getAsJsonObject();

            if (customData.has("_color")) {
                color = Color.fromJsonArray(customData.get("_color").getAsJsonArray());
            }
            disableNoteLook = GsonHelper.getAsBoolean(customData, "_disableNoteLook", false);
            disableNoteGravity = GsonHelper.getAsBoolean(customData, "_disableNoteGravity", false);
        }

        return this;
    }

    @Override
    public ColorNote loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        angleOffset = JsonUtil.getOrDefault(json, "a", JsonElement::getAsFloat, 0f);
        cutDirection = CutDirection.values()[JsonUtil.getOrDefault(json, "d", JsonElement::getAsInt, 0)]; // what the fuck
        noteType = NoteType.values()[JsonUtil.getOrDefault(json, "c", JsonElement::getAsInt, 0)];

        applyColorScheme(difficulty.getSetDifficulty());

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
            disableNoteLook = GsonHelper.getAsBoolean(customData, "disableNoteLook", false);
            disableNoteGravity = GsonHelper.getAsBoolean(customData, "disableNoteGravity", false);
        }

        return this;
    }

    @Override
    public ColorNote loadV4(JsonObject json, JsonArray colorNoteData, Difficulty difficulty) {
        super.loadV4(json, colorNoteData, difficulty);

        int i = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        JsonObject noteData = colorNoteData.get(i).getAsJsonObject();

        angleOffset = JsonUtil.getOrDefault(noteData, "a", JsonElement::getAsInt, 0);
        cutDirection = CutDirection.values()[JsonUtil.getOrDefault(noteData, "d", JsonElement::getAsInt, 0)];
        noteType = NoteType.values()[JsonUtil.getOrDefault(noteData, "c", JsonElement::getAsInt, 0)];

        applyColorScheme(difficulty.getSetDifficulty());

        return this;
    }


    public float getAngleOffset() {
        return angleOffset;
    }

    public CutDirection getCutDirection() {
        return cutDirection;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public Color getColor() {
        return color;
    }

    public boolean isNoteLookDisabled() {
        return disableNoteLook;
    }

    public boolean isNoteGravityDisabled() {
        return disableNoteGravity;
    }

    @Override
    public Color score$getColor() {
        return color;
    }

    @Override
    public NoteType score$getNoteType() {
        return noteType;
    }

    @Override
    public CutDirection score$getCutDirection() {
        return getCutDirection();
    }
}