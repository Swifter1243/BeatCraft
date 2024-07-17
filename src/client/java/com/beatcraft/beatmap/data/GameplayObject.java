package com.beatcraft.beatmap.data;

import com.beatcraft.animation.track.ObjectTrackContainer;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;
import org.joml.Quaternionf;

public abstract class GameplayObject extends BeatmapObject {
    private float njs;
    private float offset;
    private int x;
    private int y;
    private Quaternionf localRotation;
    private Quaternionf worldRotation;
    private ObjectTrackContainer trackContainer = new ObjectTrackContainer();

    @Override
    public GameplayObject loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        x = json.get("_lineIndex").getAsInt();
        y = json.get("_lineLayer").getAsInt();
        offset =  difficulty.getSetDifficulty().getOffset();
        njs =  difficulty.getSetDifficulty().getNjs();

        if (json.has("_customData")) {
            JsonObject customData = json.getAsJsonObject("_customData");

            offset = JsonHelper.getFloat(customData, "_noteJumpStartBeatOffset", offset);
            njs = JsonHelper.getFloat(customData, "_noteJumpMovementSpeed", njs);
            worldRotation = JsonUtil.getQuaternion(customData, "_rotation", null);
            localRotation = JsonUtil.getQuaternion(customData, "_localRotation", null);

            if (customData.has("_track")) {
                trackContainer = new ObjectTrackContainer(customData.get("_track"), difficulty.getTrackLibrary());
            }
        }

        return this;
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        x = json.get("x").getAsInt();
        y = json.get("y").getAsInt();
        offset =  difficulty.getSetDifficulty().getOffset();
        njs =  difficulty.getSetDifficulty().getNjs();

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            offset = JsonHelper.getFloat(customData, "noteJumpStartBeatOffset", offset);
            njs = JsonHelper.getFloat(customData, "noteJumpMovementSpeed", njs);
            worldRotation = JsonUtil.getQuaternion(customData, "worldRotation", null);
            localRotation = JsonUtil.getQuaternion(customData, "localRotation", null);

            if (customData.has("track")) {
                trackContainer = new ObjectTrackContainer(customData.get("track"), difficulty.getTrackLibrary());
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

    public ObjectTrackContainer getTrackContainer() {
        return trackContainer;
    }
}
