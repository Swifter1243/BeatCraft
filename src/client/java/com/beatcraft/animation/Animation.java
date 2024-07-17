package com.beatcraft.animation;

import com.beatcraft.animation.pointdefinition.*;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.AnimateTrack;
import com.beatcraft.beatmap.data.IBeatmapData;
import com.beatcraft.event.AnimatedPropertyEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Function;

public class Animation extends AnimationPropertyContainer<FloatPointDefinition, Vector3PointDefinition, Vector4PointDefinition, QuaternionPointDefinition> implements IBeatmapData<Animation> {

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
        color = getPointDefinition(json, "_color", difficulty, Vector4PointDefinition::new);

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
        color = getPointDefinition(json, "color", difficulty, Vector4PointDefinition::new);

        return this;
    }
    private <T> T getPointDefinition(JsonObject json, String property, Difficulty difficulty, Function<JsonArray, T> factory) {
        if (!json.has(property)) {
            return null;
        }

        JsonElement element = json.get(property);
        if (JsonHelper.isString(element)) {
            throw new NotImplementedException("TODO: Difficulty point definitions");
        } else {
            return factory.apply(element.getAsJsonArray());
        }
    }

    public EventContainer toEventContainer(AnimateTrack animateTrack) {
        return new EventContainer(this, animateTrack);
    }

    public static class EventContainer extends AnimationPropertyContainer<AnimatedPropertyEvent<Float>, AnimatedPropertyEvent<Vector3f>, AnimatedPropertyEvent<Vector4f>, AnimatedPropertyEvent<Quaternionf>> {
        private static <T> AnimatedPropertyEvent<T> propertyToEvent(PointDefinition<T> property, AnimateTrack animateTrack) {
            if (property == null) {
                return null;
            } else {
                return property.toAnimatedPropertyEvent(animateTrack);
            }
        }

        public EventContainer(Animation animation, AnimateTrack animateTrack) {
            offsetPosition = propertyToEvent(animation.getOffsetPosition(), animateTrack);
            offsetWorldRotation = propertyToEvent(animation.getOffsetWorldRotation(), animateTrack);
            localRotation = propertyToEvent(animation.getLocalRotation(), animateTrack);
            localPosition = propertyToEvent(animation.getLocalPosition(), animateTrack);
            definitePosition = propertyToEvent(animation.getDefinitePosition(), animateTrack);
            position = propertyToEvent(animation.getPosition(), animateTrack);
            rotation = propertyToEvent(animation.getRotation(), animateTrack);
            scale = propertyToEvent(animation.getScale(), animateTrack);
            dissolve = propertyToEvent(animation.getDissolve(), animateTrack);
            dissolveArrow = propertyToEvent(animation.getDissolveArrow(), animateTrack);
            interactable = propertyToEvent(animation.getInteractable(), animateTrack);
            time = propertyToEvent(animation.getTime(), animateTrack);
            color = propertyToEvent(animation.getColor(), animateTrack);
        }
    }
}
