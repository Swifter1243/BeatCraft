package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonObject;
import org.joml.Quaternionf;

public abstract class GameplayObject extends BeatmapObject {
    private float njs = 20;
    private float offset = 0;
    private int x = 0;
    private int y = 0;
    private Quaternionf localRotation;
    private Quaternionf worldRotation;

    @Override
    public GameplayObject loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        x = json.get("_lineIndex").getAsInt();
        y = json.get("_lineLayer").getAsInt();

        if (json.has("_customData")) {
            JsonObject customData = json.get("_customData").getAsJsonObject();

            if (customData.has("_noteJumpStartBeatOffset")) {
                offset = customData.get("_noteJumpStartBeatOffset").getAsFloat();
            }
            else {
                offset = difficulty.getSetDifficulty().getOffset();
            }
            if (customData.has("_noteJumpMovementSpeed")) {
                njs = customData.get("_noteJumpMovementSpeed").getAsFloat();
            }
            else {
                njs = difficulty.getSetDifficulty().getNjs();
            }
            if (customData.has("_rotation")) {
                worldRotation = JsonUtil.getQuaternion(customData.get("_rotation"));
            }
            if (customData.has("_localRotation")) {
                localRotation = JsonUtil.getQuaternion(customData.get("_localRotation"));
            }
        }

        return this;
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        x = json.get("x").getAsInt();
        y = json.get("y").getAsInt();

        if (json.has("customData")) {
            JsonObject customData = json.get("customData").getAsJsonObject();

            if (customData.has("noteJumpStartBeatOffset")) {
                offset = customData.get("noteJumpStartBeatOffset").getAsFloat();
            }
            else {
                offset = difficulty.getSetDifficulty().getOffset();
            }
            if (customData.has("noteJumpMovementSpeed")) {
                njs = customData.get("noteJumpMovementSpeed").getAsFloat();
            }
            else {
                njs = difficulty.getSetDifficulty().getNjs();
            }
            if (customData.has("worldRotation")) {
                worldRotation = JsonUtil.getQuaternion(customData.get("worldRotation"));
            }
            if (customData.has("localRotation")) {
                localRotation = JsonUtil.getQuaternion(customData.get("localRotation"));
            }
        }

        return this;
    }

    public float getNjs() {
        return njs;
    }

    public float getOffset() {
        return offset;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Quaternionf getLocalRotation() {
        return localRotation;
    }

    public Quaternionf getWorldRotation() {
        return worldRotation;
    }
}
