package com.beatcraft.beatmap.data;

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
    public ColorNote loadV2(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV2(json, setDifficulty);

        angleOffset = 0; // not existent in V2
        cutDirection = CutDirection.values()[json.get("_cutDirection").getAsInt()];
        noteType = NoteType.values()[json.get("_type").getAsInt()];

        applyColorScheme(setDifficulty);

        if (json.has("_customData")) {
            JsonObject customData = json.get("_customData").getAsJsonObject();

            if (customData.has("_color")) {
                color = Color.fromJsonArray(customData.get("_color").getAsJsonArray());
            }
        }

        return this;
    }

    @Override
    public ColorNote loadV3(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV3(json, setDifficulty);

        angleOffset = json.get("a").getAsFloat();
        cutDirection = CutDirection.values()[json.get("d").getAsInt()]; // what the fuck
        noteType = NoteType.values()[json.get("c").getAsInt()];

        applyColorScheme(setDifficulty);

        if (json.has("customData")) {
            JsonObject customData = json.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
        }

        return this;
    }

    @Override
    public ColorNote loadV4(JsonObject objectJson, JsonObject lutJson, Info.SetDifficulty setDifficulty) {
        super.loadV4(objectJson, lutJson, setDifficulty);

        angleOffset = lutJson.get("a").getAsFloat();
        cutDirection = CutDirection.values()[lutJson.get("d").getAsInt()]; // what the fuck
        noteType = NoteType.values()[lutJson.get("c").getAsInt()];

        applyColorScheme(setDifficulty);

        if (objectJson.has("customData")) {
            JsonObject customData = objectJson.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
        }

        return this;
    }
}
