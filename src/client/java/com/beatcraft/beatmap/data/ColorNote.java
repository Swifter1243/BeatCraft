package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;

public class ColorNote extends GameplayObject {
    public float angleOffset;
    public CutDirection cutDirection;
    public NoteType noteType;
    public Color color;

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (noteType == NoteType.RED) {
            color = setDifficulty.colorScheme.noteLeftColor;
        } else {
            color = setDifficulty.colorScheme.noteRightColor;
        }
    }

    @Override
    public ColorNote loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        angleOffset = 0; // not existent in V2
        cutDirection = CutDirection.values()[json.get("_cutDirection").getAsInt()];
        noteType = NoteType.values()[json.get("_type").getAsInt()];

        applyColorScheme(difficulty.setDifficulty);

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

        applyColorScheme(difficulty.setDifficulty);

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

        applyColorScheme(difficulty.setDifficulty);

        if (objectJson.has("customData")) {
            JsonObject customData = objectJson.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
        }

        return this;
    }
}
