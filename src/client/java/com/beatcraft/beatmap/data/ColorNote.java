package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public class ColorNote extends GameplayObject {
    public float angleOffset;
    public CutDirection cutDirection;
    public NoteColor noteColor;

    @Override
    public ColorNote loadV2(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV2(json, setDifficulty);

        angleOffset = 0; // not existent in V2
        cutDirection = CutDirection.values()[json.get("_cutDirection").getAsInt()];
        noteColor = NoteColor.values()[json.get("_type").getAsInt()];

        return this;
    }

    @Override
    public ColorNote loadV3(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV3(json, setDifficulty);

        angleOffset = json.get("a").getAsFloat();
        cutDirection = CutDirection.values()[json.get("d").getAsInt()]; // what the fuck
        noteColor = NoteColor.values()[json.get("c").getAsInt()];

        return this;
    }

    @Override
    public ColorNote loadV4(JsonObject objectJson, JsonObject lutJson, Info.SetDifficulty setDifficulty) {
        super.loadV4(objectJson, lutJson, setDifficulty);

        angleOffset = lutJson.get("a").getAsFloat();
        cutDirection = CutDirection.values()[lutJson.get("d").getAsInt()]; // what the fuck
        noteColor = NoteColor.values()[lutJson.get("c").getAsInt()];

        return this;
    }
}
