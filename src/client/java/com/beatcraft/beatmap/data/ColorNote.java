package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public class ColorNote extends BeatmapObject {
    public float angleOffset = 0;
    public CutDirection cutDirection = CutDirection.DOWN;

    @Override
    public ColorNote load(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.load(json, setDifficulty);

        angleOffset = json.get("a").getAsFloat();
        cutDirection = CutDirection.values()[json.get("d").getAsInt()]; // what the fuck

        return this;
    }
}
