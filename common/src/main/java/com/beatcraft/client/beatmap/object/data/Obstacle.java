package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Obstacle extends GameplayObject {

    private float duration;
    private float width;
    private float height;
    private Color color;
    private Integer cachedColor = null;
    private boolean noodleSizing = false;

    public void loadCustomObstacleDataV2(JsonObject json, Difficulty difficulty) {
        if (json.has("_customData")) {
            JsonObject customData = json.getAsJsonObject("_customData");

            if (customData.has("_coordinates")) {
                JsonArray coordinates = customData.getAsJsonArray("_coordinates");
                x = coordinates.get(0).getAsFloat() + 1.9f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("_position")) {
                JsonArray coordinates = customData.getAsJsonArray("_position");
                x = coordinates.get(0).getAsFloat() + 1.9f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("_color")) {
                JsonArray color = customData.getAsJsonArray("_color");

                var col = JsonUtil.getVector4(color);

                this.color = new Color(col.x, col.y, col.z, col.w);
            }

            if (customData.has("_scale")) {
                JsonArray size = customData.getAsJsonArray("_scale");
                if (!size.isEmpty()) {
                    width = size.get(0).getAsFloat();

                    if (size.size() >= 2) {
                        height = size.get(1).getAsFloat();

                        if (size.size() == 3) {
                            duration = size.get(2).getAsFloat();
                            noodleSizing = true;
                        }
                    }

                }
            }
        }
    }

    public void loadCustomObstacleDataV3(JsonObject json, Difficulty difficulty) {
        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("coordinates")) {
                JsonArray coordinates = customData.getAsJsonArray("coordinates");
                x = coordinates.get(0).getAsFloat() + 1.9f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("position")) {
                JsonArray coordinates = customData.getAsJsonArray("position");
                x = coordinates.get(0).getAsFloat() + 1.9f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("color")) {
                JsonArray color = customData.getAsJsonArray("color");

                var col = JsonUtil.getVector4(color);

                this.color = new Color(col.x, col.y, col.z, col.w);
            }

            if (customData.has("size")) {
                JsonArray size = customData.getAsJsonArray("size");
                if (!size.isEmpty()) {
                    width = size.get(0).getAsFloat();

                    if (size.size() >= 2) {
                        height = size.get(1).getAsFloat();

                        if (size.size() == 3) {
                            duration = size.get(2).getAsFloat();
                            noodleSizing = true;
                        }
                    }

                }
            }

        }
    }

    @Override
    public Obstacle loadV2(JsonObject json, Difficulty difficulty) {

        this.beat = json.get("_time").getAsFloat();

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs(beat);

        this.duration = json.get("_duration").getAsFloat();
        this.x = json.get("_lineIndex").getAsFloat();
        this.width = json.get("_width").getAsFloat();

        int _type = json.get("_type").getAsInt();

        switch (_type) {
            case 0 -> {
                this.y = 0;
                this.height = 5;
            }
            case 1 -> {
                this.y = 2;
                this.height = 3;
            }
            case 2 -> {
                this.y = JsonUtil.getOrDefault(json, "_lineLayer", JsonElement::getAsFloat, 0f);
                this.height = JsonUtil.getOrDefault(json, "_height", JsonElement::getAsFloat, 5f);
            }
        }

        this.color = difficulty.getSetDifficulty().getColorScheme().getObstacleColor();

        loadCustomDataV2(json, difficulty);
        loadCustomObstacleDataV2(json, difficulty);

        loadJumps(difficulty.getInfo());

        return this;
    }

    @Override
    public Obstacle loadV3(JsonObject json, Difficulty difficulty) {
        this.beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs(beat);

        this.duration = JsonUtil.getOrDefault(json, "d", JsonElement::getAsFloat, 0f);
        this.x = JsonUtil.getOrDefault(json, "x", JsonElement::getAsFloat, 0f);
        this.y = JsonUtil.getOrDefault(json, "y", JsonElement::getAsFloat, 0f);
        this.width = JsonUtil.getOrDefault(json, "w", JsonElement::getAsFloat, 0f);
        this.height = JsonUtil.getOrDefault(json, "h", JsonElement::getAsFloat, 0f);


        this.color = difficulty.getSetDifficulty().getColorScheme().getObstacleColor();

        loadCustomDataV3(json, difficulty);
        loadCustomObstacleDataV3(json, difficulty);

        loadJumps(difficulty.getInfo());

        return this;
    }

    @Override
    public Obstacle loadV4(JsonObject json, JsonArray obstaclesData, Difficulty difficulty) {
        this.beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs(beat);

        int i = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        JsonObject data = obstaclesData.get(i).getAsJsonObject();

        this.duration = JsonUtil.getOrDefault(data, "d", JsonElement::getAsFloat, 0f);
        this.x = JsonUtil.getOrDefault(data, "x", JsonElement::getAsFloat, 0f);
        this.y = JsonUtil.getOrDefault(data, "y", JsonElement::getAsFloat, 0f);
        this.width = JsonUtil.getOrDefault(data, "w", JsonElement::getAsFloat, 0f);
        this.height = JsonUtil.getOrDefault(data, "h", JsonElement::getAsFloat, 0f);

        this.color = difficulty.getSetDifficulty().getColorScheme().getObstacleColor();

        loadJumps(difficulty.getInfo());

        return this;
    }

    public float getDuration() {
        return duration;
    }

    public float getLength(float njsDistance) {
        if (noodleSizing) {
            return duration;
        }
        return duration * njsDistance;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int getColor() {
        if (cachedColor == null) {
            cachedColor = color.toARGB(0.15f);
        }
        return cachedColor;
    }

}
