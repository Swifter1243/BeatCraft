package com.beatcraft.beatmap.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ChromaMaterial {
    // currently shader does nothing, im not doing shaders for this everything will just be unlit :shrug:

    protected String shader;
    protected JsonArray color;

    public ChromaMaterial load(JsonObject json) {
        shader = json.get("shader").toString();
        color = json.get("colour").getAsJsonArray();
        return this;
    }
}
