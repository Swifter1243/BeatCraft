package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public class BeatmapObject {
    public float njs = 20;
    public float offset = 0;
    public float beat = 0;
    public int x = 0;
    public int y = 0;

    public BeatmapObject load(JsonObject json, Info.SetDifficulty setDifficulty) {
        beat = json.get("b").getAsFloat();
        x = json.get("x").getAsInt();
        y = json.get("y").getAsInt();

        if (json.has("customData")) {
            JsonObject customData = json.get("customData").getAsJsonObject();

            if (customData.has("noteJumpStartBeatOffset")) {
                offset = customData.get("noteJumpStartBeatOffset").getAsFloat();
            }
            else {
                offset = setDifficulty.offset;
            }
            if (customData.has("noteJumpMovementSpeed")) {
                njs = customData.get("noteJumpMovementSpeed").getAsFloat();
            }
            else {
                njs = setDifficulty.njs;
            }
        }

        return this;
    }
}
