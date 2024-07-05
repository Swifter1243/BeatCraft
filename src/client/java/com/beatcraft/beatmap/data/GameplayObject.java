package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

public abstract class GameplayObject extends BeatmapObject {
    public float njs = 20;
    public float offset = 0;
    public int x = 0;
    public int y = 0;

    @Override
    public GameplayObject loadV2(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV2(json, setDifficulty);

        x = json.get("_lineIndex").getAsInt();
        y = json.get("_lineLayer").getAsInt();

        if (json.has("_customData")) {
            JsonObject customData = json.get("_customData").getAsJsonObject();

            if (customData.has("_noteJumpStartBeatOffset")) {
                offset = customData.get("_noteJumpStartBeatOffset").getAsFloat();
            }
            else {
                offset = setDifficulty.offset;
            }
            if (customData.has("_noteJumpMovementSpeed")) {
                njs = customData.get("_noteJumpMovementSpeed").getAsFloat();
            }
            else {
                njs = setDifficulty.njs;
            }
        }

        return this;
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Info.SetDifficulty setDifficulty) {
        super.loadV3(json, setDifficulty);

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

    @Override
    public BeatmapObject loadV4(JsonObject objectJson, JsonObject lutJson, Info.SetDifficulty setDifficulty) {
        super.loadV4(objectJson, lutJson, setDifficulty);

        x = lutJson.get("x").getAsInt();
        y = lutJson.get("y").getAsInt();

        if (lutJson.has("customData")) {
            JsonObject customData = lutJson.get("customData").getAsJsonObject();

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
