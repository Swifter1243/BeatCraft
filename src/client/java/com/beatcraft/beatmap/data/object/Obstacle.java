package com.beatcraft.beatmap.data.object;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Obstacle extends GameplayObject {

    private float duration;
    private float width;
    private float height;

    @Override
    public Obstacle loadV2(JsonObject json, Difficulty difficulty) {

        this.beat = json.get("_time").getAsFloat();

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs();

        this.duration = json.get("_duration").getAsFloat();
        this.x = json.get("_lineIndex").getAsInt();
        this.width = (float) json.get("_width").getAsInt();

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
                this.height = (float) JsonUtil.getOrDefault(json, "_height", JsonElement::getAsInt, 5);
            }
        }

        loadJumps(difficulty.getInfo());

        return this;
    }

    @Override
    public Obstacle loadV3(JsonObject json, Difficulty difficulty) {
        this.beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs();

        this.duration = JsonUtil.getOrDefault(json, "d", JsonElement::getAsFloat, 0f);
        this.x = (float) JsonUtil.getOrDefault(json, "x", JsonElement::getAsInt, 0);
        this.y = (float) JsonUtil.getOrDefault(json, "y", JsonElement::getAsInt, 0);
        this.width = (float) JsonUtil.getOrDefault(json, "w", JsonElement::getAsInt, 0);
        this.height = (float) JsonUtil.getOrDefault(json, "h", JsonElement::getAsInt, 0);

        loadJumps(difficulty.getInfo());

        return this;
    }

    @Override
    public Obstacle loadV4(JsonObject json, JsonArray obstaclesData, Difficulty difficulty) {
        this.beat = JsonUtil.getOrDefault(json, "b", JsonElement::getAsFloat, 0f);

        offset = difficulty.getSetDifficulty().getOffset();
        njs = difficulty.getSetDifficulty().getNjs();

        int i = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        JsonObject data = obstaclesData.get(i).getAsJsonObject();

        this.duration = JsonUtil.getOrDefault(data, "d", JsonElement::getAsFloat, 0f);
        this.x = (float) JsonUtil.getOrDefault(data, "x", JsonElement::getAsInt, 0);
        this.y = (float) JsonUtil.getOrDefault(data, "y", JsonElement::getAsInt, 0);
        this.width = (float) JsonUtil.getOrDefault(data, "w", JsonElement::getAsInt, 0);
        this.height = (float) JsonUtil.getOrDefault(data, "h", JsonElement::getAsInt, 0);

        loadJumps(difficulty.getInfo());

        return this;
    }

    public float getDuration() {
        return duration;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
