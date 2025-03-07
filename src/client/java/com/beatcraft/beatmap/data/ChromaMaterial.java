package com.beatcraft.beatmap.data;

import com.beatcraft.data.types.Color;
import com.google.gson.JsonObject;

public class ChromaMaterial {
    // currently shader does nothing, im not doing shaders for this everything will just be unlit :shrug:
    protected String shader;
    protected Color color;

    public ChromaMaterial load(JsonObject json) {
        shader = json.get("shader").toString();
        color = Color.fromJsonArray(json.get("color").getAsJsonArray());
        return this;
    }
}
