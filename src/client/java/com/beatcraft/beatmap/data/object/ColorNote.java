package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.data.types.Color;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

public class ColorNote extends GameplayObject {
    private float angleOffset;
    private CutDirection cutDirection;
    private NoteType noteType;
    private Color color;
    private boolean disableNoteLook = false;
    private boolean disableNoteGravity = false;

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
            disableNoteLook = JsonHelper.getBoolean(customData, "_disableNoteLook", false);
            disableNoteGravity = JsonHelper.getBoolean(customData, "_disableNoteGravity", false);
        }

        return this;
    }

    @Override
    public ColorNote loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        angleOffset = json.get("a").getAsFloat();
        cutDirection = CutDirection.values()[json.get("d").getAsInt()]; // what the fuck
        noteType = NoteType.values()[json.get("c").getAsInt()];

        applyColorScheme(difficulty.getSetDifficulty());

        if (json.has("customData")) {
            JsonObject customData = json.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
            disableNoteLook = JsonHelper.getBoolean(customData, "disableNoteLook", false);
            disableNoteGravity = JsonHelper.getBoolean(customData, "disableNoteGravity", false);
        }

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
}
