package com.beatcraft.animation;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.event.AnimatedPathEventContainer;
import com.beatcraft.animation.pointdefinition.*;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.beatmap.data.IBeatmapData;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

import java.util.function.Function;

public class Animation extends AnimationPropertyContainer<FloatPointDefinition, Vector3PointDefinition, Vector4PointDefinition, QuaternionPointDefinition, ColorPointDefinition> implements IBeatmapData<Animation> {

    @Override
    public Animation loadV2(JsonObject json, Difficulty difficulty) {
        offsetPosition = getPointDefinition(json, "_position", difficulty, Vector3PointDefinition::new);
        offsetWorldRotation = getPointDefinition(json, "_rotation", difficulty, QuaternionPointDefinition::new);
        localRotation = getPointDefinition(json, "_localRotation", difficulty, QuaternionPointDefinition::new);
        localPosition = getPointDefinition(json, "_localPosition", difficulty, Vector3PointDefinition::new);
        definitePosition = getPointDefinition(json, "_definitePosition", difficulty, Vector3PointDefinition::new);
        position = getPointDefinition(json, "_position", difficulty, Vector3PointDefinition::new);
        rotation = getPointDefinition(json, "_rotation", difficulty, QuaternionPointDefinition::new);
        scale = getPointDefinition(json, "_scale", difficulty, Vector3PointDefinition::new);
        dissolve = getPointDefinition(json, "_dissolve", difficulty, FloatPointDefinition::new);
        dissolveArrow = getPointDefinition(json, "_dissolveArrow", difficulty, FloatPointDefinition::new);
        interactable = getPointDefinition(json, "_interactable", difficulty, FloatPointDefinition::new);
        time = getPointDefinition(json, "_time", difficulty, FloatPointDefinition::new);
        color = getPointDefinition(json, "_color", difficulty, ColorPointDefinition::new);

        return this;
    }

    @Override
    public Animation loadV3(JsonObject json, Difficulty difficulty) {
        offsetPosition = getPointDefinition(json, "offsetPosition", difficulty, Vector3PointDefinition::new);
        offsetWorldRotation = getPointDefinition(json, "offsetWorldRotation", difficulty, QuaternionPointDefinition::new);
        localRotation = getPointDefinition(json, "localRotation", difficulty, QuaternionPointDefinition::new);
        localPosition = getPointDefinition(json, "localPosition", difficulty, Vector3PointDefinition::new);
        definitePosition = getPointDefinition(json, "definitePosition", difficulty, Vector3PointDefinition::new);
        position = getPointDefinition(json, "position", difficulty, Vector3PointDefinition::new);
        rotation = getPointDefinition(json, "rotation", difficulty, QuaternionPointDefinition::new);
        scale = getPointDefinition(json, "scale", difficulty, Vector3PointDefinition::new);
        dissolve = getPointDefinition(json, "dissolve", difficulty, FloatPointDefinition::new);
        dissolveArrow = getPointDefinition(json, "dissolveArrow", difficulty, FloatPointDefinition::new);
        interactable = getPointDefinition(json, "interactable", difficulty, FloatPointDefinition::new);
        time = getPointDefinition(json, "time", difficulty, FloatPointDefinition::new);
        color = getPointDefinition(json, "color", difficulty, ColorPointDefinition::new);

        return this;
    }
    private <T> T getPointDefinition(JsonObject json, String property, Difficulty difficulty, Function<JsonArray, T> factory) {
        if (!json.has(property)) {
            return null;
        }

        JsonElement element = json.get(property);
        if (JsonHelper.isString(element)) {
            String name = element.getAsString();
            if (difficulty.pointDefinitions.containsKey(name)) {
                return factory.apply(difficulty.pointDefinitions.get(name));
            } else {
                BeatCraft.LOGGER.warn("Point Definition [{}] does not exist! Skipping...", name);
                return null;
            }
        } else {
            return factory.apply(element.getAsJsonArray());
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
