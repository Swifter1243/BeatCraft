package com.beatcraft.animation;

import com.beatcraft.animation.event.AnimatedPropertyEventHandler;
import com.beatcraft.animation.pointdefinition.PointDefinition;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AnimationState extends AnimationPropertyContainer<Float, Vector3f, Vector4f, Quaternionf> {
    private void applyEventHandlerOperation(AnimatedProperties properties, Function<AnimatedPropertyEventHandler<?>, ?> operation) {
        offsetPosition = (Vector3f) operation.apply(properties.getOffsetPosition());
        offsetWorldRotation = (Quaternionf) operation.apply(properties.getOffsetWorldRotation());
        localRotation = (Quaternionf) operation.apply(properties.getLocalRotation());
        localPosition = (Vector3f) operation.apply(properties.getLocalPosition());
        definitePosition = (Vector3f) operation.apply(properties.getDefinitePosition());
        position = (Vector3f) operation.apply(properties.getPosition());
        rotation = (Quaternionf) operation.apply(properties.getRotation());
        scale = (Vector3f) operation.apply(properties.getScale());
        dissolve = (Float) operation.apply(properties.getDissolve());
        dissolveArrow = (Float) operation.apply(properties.getDissolveArrow());
        interactable = (Float) operation.apply(properties.getInteractable());
        time = (Float) operation.apply(properties.getTime());
        color = (Vector4f) operation.apply(properties.getColor());
    }

    public void applySeek(float beat, AnimatedProperties properties) {
        applyEventHandlerOperation(properties, (handler) -> handler.seek(beat));
    }

    public void applyUpdate(float beat, AnimatedProperties properties) {
        applyEventHandlerOperation(properties, (handler) -> handler.update(beat));
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
