package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.common.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BombNote extends GameplayObject {
    private Color color = new Color(0.2f, 0.2f, 0.2f);

    public BombNote(BeatmapController map) {
        super(map);
    }

    @Override
    public BombNote loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        if (json.has("_customData")) {
            JsonObject customData = json.get("_customData").getAsJsonObject();

            if (customData.has("_color")) {
                color = Color.fromJsonArray(customData.get("_color").getAsJsonArray());
            }
        }

        return this;
    }

    @Override
    public BombNote loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        if (json.has("customData")) {
            JsonObject customData = json.get("customData").getAsJsonObject();

            if (customData.has("color")) {
                color = Color.fromJsonArray(customData.get("color").getAsJsonArray());
            }
        }

        return this;
    }

    @Override
    public BombNote loadV4(JsonObject json, JsonArray colorNoteData, Difficulty difficulty) {
        super.loadV4(json, colorNoteData, difficulty);

        // customData?

        return this;
    }

    public Color getColor() {
        return color;
    }
}
