package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonObject;

public class BombNote extends GameplayObject {
    private Color color = new Color(0.2f, 0.2f, 0.2f);

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

    public Color getColor() {
        return color;
    }
}
