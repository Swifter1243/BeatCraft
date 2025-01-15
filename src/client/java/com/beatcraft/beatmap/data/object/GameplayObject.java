package com.beatcraft.beatmap.data.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.Animation;
import com.beatcraft.animation.track.ObjectTrackContainer;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.utils.JsonUtil;
import com.beatcraft.utils.NoteMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;
import org.joml.Quaternionf;

public abstract class GameplayObject extends BeatmapObject {
    protected float njs;
    protected float offset;
    protected float x;
    protected float y;
    private Quaternionf localRotation;
    private Quaternionf worldRotation;
    private final ObjectTrackContainer trackContainer = new ObjectTrackContainer();
    private final Animation pathAnimation = new Animation();
    private NoteMath.Jumps jumps;

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

            if (customData.has("_coordinates")) {
                JsonArray coordinates = customData.getAsJsonArray("_coordinates");
                x = coordinates.get(0).getAsInt() + 2.0f;
                y = coordinates.get(1).getAsInt();
            }

            if (customData.has("_position")) {
                JsonArray coordinates = customData.getAsJsonArray("_position");
                x = coordinates.get(0).getAsFloat() + 2.0f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("_track")) {
                trackContainer.loadTracks(customData.get("_track"), difficulty.getTrackLibrary());
            }

            if (customData.has("_animation")) {
                pathAnimation.loadV2(customData.get("_animation").getAsJsonObject(), difficulty);
            }
        }

        loadJumps(difficulty.getInfo());

        return this;
    }

    @Override
    public GameplayObject loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        x = JsonUtil.getOrDefault(json, "x", JsonElement::getAsInt, 0);
        y = JsonUtil.getOrDefault(json, "y", JsonElement::getAsInt, 0);
        offset =  difficulty.getSetDifficulty().getOffset();
        njs =  difficulty.getSetDifficulty().getNjs();

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            offset = JsonHelper.getFloat(customData, "noteJumpStartBeatOffset", offset);
            njs = JsonHelper.getFloat(customData, "noteJumpMovementSpeed", njs);
            worldRotation = JsonUtil.getQuaternion(customData, "worldRotation", null);
            localRotation = JsonUtil.getQuaternion(customData, "localRotation", null);

            if (customData.has("coordinates")) {
                JsonArray coordinates = customData.getAsJsonArray("coordinates");
                x = coordinates.get(0).getAsInt() + 2.0f;
                y = coordinates.get(1).getAsInt();
            }

            if (customData.has("position")) {
                JsonArray coordinates = customData.getAsJsonArray("position");
                x = coordinates.get(0).getAsFloat() + 2.0f;
                y = coordinates.get(1).getAsFloat();
            }

            if (customData.has("track")) {
                trackContainer.loadTracks(customData.get("track"), difficulty.getTrackLibrary());
            }

            if (customData.has("animation")) {
                pathAnimation.loadV3(customData.get("animation").getAsJsonObject(), difficulty);
            }
        }

        loadJumps(difficulty.getInfo());

        return this;
    }

    public GameplayObject loadV4(JsonObject json, JsonArray colorNoteData, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        int index = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);

        offset =  difficulty.getSetDifficulty().getOffset();
        njs =  difficulty.getSetDifficulty().getNjs();

        // TODO: read customData

        loadJumps(difficulty.getInfo());

        if (index >= colorNoteData.size()) {
            BeatCraft.LOGGER.info("index > size");
            return this;
        }

        JsonObject noteData = colorNoteData.get(index).getAsJsonObject();
        x = JsonUtil.getOrDefault(noteData, "x", JsonElement::getAsInt, 0);
        y = JsonUtil.getOrDefault(noteData, "y", JsonElement::getAsInt, 0);

        return this;
    }

    protected void loadJumps(Info info) {
        this.jumps = NoteMath.getJumps(njs, offset, info.getBpm());
    }

    public float getNjs() {
        return njs;
    }

    public float getOffset() {
        return offset;
    }

    public float getX() {
        return x;
    }

    public float getY() {
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

    public Animation getPathAnimation() {
        return pathAnimation;
    }

    public NoteMath.Jumps getJumps() {
        return jumps;
    }
}
