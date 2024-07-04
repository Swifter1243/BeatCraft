package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public class ColorNote extends BeatmapObject {
    public float angleOffset;
    public CutDirection cutDirection;
    public NoteColor noteColor;

    @Override
    public ColorNote load(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.load(json, setDifficulty);

        angleOffset = json.get("a").getAsFloat();
        cutDirection = CutDirection.values()[json.get("d").getAsInt()]; // what the fuck
        noteColor = NoteColor.values()[json.get("c").getAsInt()];

        return this;
    }
}
