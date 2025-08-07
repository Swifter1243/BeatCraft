package com.beatcraft.client.animation;


import com.beatcraft.client.animation.pointdefinition.PointDefinition;
import com.beatcraft.client.animation.track.AnimatedProperties;
import com.beatcraft.common.data.types.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.BiFunction;

public class AnimationState extends AnimationPropertyContainer<Float, Vector3f, Vector4f, Quaternionf, Color> {

    public void seekFromProperties(float beat, AnimatedProperties properties) {
        offsetPosition = properties.offsetPosition.seek(beat);
        offsetWorldRotation = properties.offsetWorldRotation.seek(beat);
        localRotation = properties.localRotation.seek(beat);
        localPosition = properties.localPosition.seek(beat);
        definitePosition = properties.definitePosition.seek(beat);
        position = properties.position.seek(beat);
        rotation = properties.rotation.seek(beat);
        scale = properties.scale.seek(beat);
        dissolve = properties.dissolve.seek(beat);
        dissolveArrow = properties.dissolveArrow.seek(beat);
        interactable = properties.interactable.seek(beat);
        time = properties.time.seek(beat);
        color = properties.color.seek(beat);
    }

    public void updateFromProperties(float beat, AnimatedProperties properties) {
        offsetPosition = properties.offsetPosition.update(beat);
        offsetWorldRotation = properties.offsetWorldRotation.update(beat);
        localRotation = properties.localRotation.update(beat);
        localPosition = properties.localPosition.update(beat);
        definitePosition = properties.definitePosition.update(beat);
        position = properties.position.update(beat);
        rotation = properties.rotation.update(beat);
        scale = properties.scale.update(beat);
        dissolve = properties.dissolve.update(beat);
        dissolveArrow = properties.dissolveArrow.update(beat);
        interactable = properties.interactable.update(beat);
        time = properties.time.update(beat);
        color = properties.color.update(beat);
    }

    private static <T> T interpolateProperty(PointDefinition<T> pointDefinition, float time) {
        if (pointDefinition == null) {
            return null;
        } else {
            return pointDefinition.interpolate(time);
        }
    }

    public static AnimationState fromAnimation(Animation animation, float normalTime) {
        AnimationState state = new AnimationState();

        state.offsetPosition = interpolateProperty(animation.offsetPosition, normalTime);
        state.offsetWorldRotation = interpolateProperty(animation.offsetWorldRotation, normalTime);
        state.localRotation = interpolateProperty(animation.localRotation, normalTime);
        state.localPosition = interpolateProperty(animation.localPosition, normalTime);
        state.definitePosition = interpolateProperty(animation.definitePosition, normalTime);
        state.position = interpolateProperty(animation.position, normalTime);
        state.rotation = interpolateProperty(animation.rotation, normalTime);
        state.scale = interpolateProperty(animation.scale, normalTime);
        state.dissolve = interpolateProperty(animation.dissolve, normalTime);
        state.dissolveArrow = interpolateProperty(animation.dissolveArrow, normalTime);
        state.interactable = interpolateProperty(animation.interactable, normalTime);
        state.time = interpolateProperty(animation.time, normalTime);
        state.color = interpolateProperty(animation.color, normalTime);

        return state;
    }

    public static AnimationState combine(AnimationState a, AnimationState b) {
        AnimationState state = new AnimationState();

        state.offsetPosition = combineProperties(a.getOffsetPosition(), b.getOffsetPosition(), AnimationState::add);
        state.offsetWorldRotation = combineProperties(a.getOffsetWorldRotation(), b.getOffsetWorldRotation(), AnimationState::combineRotations);
        state.localRotation = combineProperties(a.getLocalRotation(), b.getLocalRotation(), AnimationState::combineRotations);
        state.localPosition = combineProperties(a.getLocalPosition(), b.getLocalPosition(), AnimationState::add);
        state.definitePosition = combineProperties(a.getDefinitePosition(), b.getDefinitePosition(), AnimationState::add);
        state.position = combineProperties(a.getPosition(), b.getPosition(), AnimationState::add);
        state.rotation = combineProperties(a.getRotation(), b.getRotation(), AnimationState::combineRotations);
        state.scale = combineProperties(a.getScale(), b.getScale(), AnimationState::multiply);
        state.dissolve = combineProperties(a.getDissolve(), b.getDissolve(), AnimationState::multiply);
        state.dissolveArrow = combineProperties(a.getDissolveArrow(), b.getDissolveArrow(), AnimationState::multiply);
        state.interactable = combineProperties(a.getInteractable(), b.getInteractable(), AnimationState::multiply);
        state.time = combineProperties(a.getTime(), b.getTime(), AnimationState::multiply);
        state.color = combineProperties(a.getColor(), b.getColor(), AnimationState::multiply);

        return state;
    }

    private static float multiply(float a, float b) {
        return a * b;
    }
    private static Color multiply(Color a, Color b) {
        return new Color(a.getRed() * b.getRed(), a.getGreen() * b.getGreen(), a.getBlue() * b.getBlue(), a.getAlpha() * b.getAlpha());
    }
    private static Vector4f multiply(Vector4f a, Vector4f b) {
        return new Vector4f(a).mul(b);
    }
    private static Vector3f multiply(Vector3f a, Vector3f b) {
        return new Vector3f(a).mul(b);
    }
    private static Vector3f add(Vector3f a, Vector3f b) {
        return new Vector3f(a).add(b);
    }
    private static Quaternionf combineRotations(Quaternionf a, Quaternionf b) {
        return new Quaternionf(a).mul(b);
    }

    private static <T> T combineProperties(T a, T b, BiFunction<T, T, T> operation) {
        if (a == null && b == null) {
            return null;
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return operation.apply(a, b);
        }
    }
}
