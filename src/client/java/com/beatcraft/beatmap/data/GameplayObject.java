package com.beatcraft.beatmap.data;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;
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
            JsonObject customData = json.getAsJsonObject("_customData");

            offset = JsonHelper.getFloat(customData, "_noteJumpStartBeatOffset", difficulty.getSetDifficulty().getOffset());
            njs = JsonHelper.getFloat(customData, "_noteJumpMovementSpeed", difficulty.getSetDifficulty().getNjs());
            worldRotation = JsonUtil.getQuaternion(customData, "_rotation", null);
            localRotation = JsonUtil.getQuaternion(customData, "_localRotation", null);
        }

        return this;
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        x = json.get("x").getAsInt();
        y = json.get("y").getAsInt();

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            offset = JsonHelper.getFloat(customData, "noteJumpStartBeatOffset", difficulty.getSetDifficulty().getOffset());
            njs = JsonHelper.getFloat(customData, "noteJumpMovementSpeed", difficulty.getSetDifficulty().getNjs());
            worldRotation = JsonUtil.getQuaternion(customData, "worldRotation", null);
            localRotation = JsonUtil.getQuaternion(customData, "localRotation", null);
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
