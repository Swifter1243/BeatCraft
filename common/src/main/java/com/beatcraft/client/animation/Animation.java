package com.beatcraft.client.animation;


import com.beatcraft.Beatcraft;
import com.beatcraft.client.animation.event.AnimatedPathEventContainer;
import com.beatcraft.client.animation.pointdefinition.*;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.event.AnimateTrack;
import com.beatcraft.client.beatmap.object.data.IBeatmapData;
import com.beatcraft.client.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.client.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.common.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Animation extends AnimationPropertyContainer<FloatPointDefinition, Vector3PointDefinition, Vector4PointDefinition, QuaternionPointDefinition, ColorPointDefinition> implements IBeatmapData<Animation> {

    private BeatmapPlayer map;

    public Animation(BeatmapPlayer map) {
        this.map = map;
    }

    @Override
    public Animation loadV2(JsonObject json, Difficulty difficulty) {
        offsetPosition = getPointDefinition(map, json, "_position", difficulty, Vector3PointDefinition::new);
        offsetWorldRotation = getPointDefinition(map, json, "_rotation", difficulty, QuaternionPointDefinition::new);
        localRotation = getPointDefinition(map, json, "_localRotation", difficulty, QuaternionPointDefinition::new);
        localPosition = getPointDefinition(map, json, "_localPosition", difficulty, Vector3PointDefinition::new);
        definitePosition = getPointDefinition(map, json, "_definitePosition", difficulty, Vector3PointDefinition::new);
        position = getPointDefinition(map, json, "_position", difficulty, Vector3PointDefinition::new);
        rotation = getPointDefinition(map, json, "_rotation", difficulty, QuaternionPointDefinition::new);
        scale = getPointDefinition(map, json, "_scale", difficulty, Vector3PointDefinition::new);
        dissolve = getPointDefinition(map, json, "_dissolve", difficulty, FloatPointDefinition::new);
        dissolveArrow = getPointDefinition(map, json, "_dissolveArrow", difficulty, FloatPointDefinition::new);
        interactable = getPointDefinition(map, json, "_interactable", difficulty, FloatPointDefinition::new);
        time = getPointDefinition(map, json, "_time", difficulty, FloatPointDefinition::new);
        color = getPointDefinition(map, json, "_color", difficulty, ColorPointDefinition::new);

        return this;
    }

    @Override
    public Animation loadV3(JsonObject json, Difficulty difficulty) {
        offsetPosition = getPointDefinition(map, json, "offsetPosition", difficulty, Vector3PointDefinition::new);
        offsetWorldRotation = getPointDefinition(map, json, "offsetWorldRotation", difficulty, QuaternionPointDefinition::new);
        localRotation = getPointDefinition(map, json, "localRotation", difficulty, QuaternionPointDefinition::new);
        localPosition = getPointDefinition(map, json, "localPosition", difficulty, Vector3PointDefinition::new);
        definitePosition = getPointDefinition(map, json, "definitePosition", difficulty, Vector3PointDefinition::new);
        position = getPointDefinition(map, json, "position", difficulty, Vector3PointDefinition::new);
        rotation = getPointDefinition(map, json, "rotation", difficulty, QuaternionPointDefinition::new);
        scale = getPointDefinition(map, json, "scale", difficulty, Vector3PointDefinition::new);
        dissolve = getPointDefinition(map, json, "dissolve", difficulty, FloatPointDefinition::new);
        dissolveArrow = getPointDefinition(map, json, "dissolveArrow", difficulty, FloatPointDefinition::new);
        interactable = getPointDefinition(map, json, "interactable", difficulty, FloatPointDefinition::new);
        time = getPointDefinition(map, json, "time", difficulty, FloatPointDefinition::new);
        color = getPointDefinition(map, json, "color", difficulty, ColorPointDefinition::new);

        return this;
    }
    private <T> T getPointDefinition(BeatmapPlayer map, JsonObject json, String property, Difficulty difficulty, BiFunction<BeatmapPlayer, JsonArray, T> factory) {
        if (!json.has(property)) {
            return null;
        }

        JsonElement element = json.get(property);
        if (GsonHelper.isStringValue(element)) {
            String name = element.getAsString();
            if (difficulty.pointDefinitions.containsKey(name)) {
                return factory.apply(map, difficulty.pointDefinitions.get(name));
            } else {
                Beatcraft.LOGGER.warn("Point Definition [{}] does not exist! Skipping...", name);
                return null;
            }
        } else {
            return factory.apply(map, element.getAsJsonArray());
        }
    }

    public AnimatedPropertyEventContainer toAnimatedPropertyEvents(AnimateTrack animateTrack) {
        return new AnimatedPropertyEventContainer(this, animateTrack);
    }

    public AnimatedPathEventContainer toAnimatedPathEvents(AssignPathAnimation assignPathAnimation) {
        return new AnimatedPathEventContainer(this, assignPathAnimation);
    }

    public AnimationState toState(float time) {
        return AnimationState.fromAnimation(this, time);
    }
}
