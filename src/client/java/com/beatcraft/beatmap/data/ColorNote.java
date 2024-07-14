package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;

public class ColorNote extends GameplayObject {
    private float angleOffset;
    private CutDirection cutDirection;
    private NoteType noteType;
    private Color color;

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
        }

        return this;
    }

    @Override
    public ColorNote loadV4(JsonObject objectJson, JsonObject lutJson, Difficulty difficulty) {
        super.loadV4(objectJson, lutJson, difficulty);

        angleOffset = lutJson.get("a").getAsFloat();
        cutDirection = CutDirection.values()[lutJson.get("d").getAsInt()]; // what the fuck
        noteType = NoteType.values()[lutJson.get("c").getAsInt()];

        applyColorScheme(difficulty.getSetDifficulty());

        if (objectJson.has("customData")) {
            JsonObject customData = objectJson.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
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
}
