package com.beatcraft.animation;

import com.beatcraft.animation.track.Track;
import com.beatcraft.event.AnimatedPropertyEventHandler;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AnimationState extends AnimationPropertyContainer<Float, Vector3f, Vector4f, Quaternionf> {
    private void applyOperation(Track.AnimationProperties properties, Function<AnimatedPropertyEventHandler<?>, ?> operation) {
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

    public void applySeek(float beat, Track.AnimationProperties properties) {
        applyOperation(properties, (handler) -> handler.seek(beat));
    }

    public void applyUpdate(float beat, Track.AnimationProperties properties) {
        applyOperation(properties, (handler) -> handler.update(beat));
    }

    public AnimationState combineWithOther(AnimationState other) {
        AnimationState state = new AnimationState();

        offsetPosition = combineProperties(getOffsetPosition(), other.getOffsetPosition(), AnimationState::add);
        offsetWorldRotation = combineProperties(getOffsetWorldRotation(), other.getOffsetWorldRotation(), AnimationState::combineRotations);
        localRotation = combineProperties(getLocalRotation(), other.getLocalRotation(), AnimationState::combineRotations);
        localPosition = combineProperties(getLocalPosition(), other.getLocalPosition(), AnimationState::add);
        definitePosition = combineProperties(getDefinitePosition(), other.getDefinitePosition(), AnimationState::add);
        position = combineProperties(getPosition(), other.getPosition(), AnimationState::add);
        rotation = combineProperties(getRotation(), other.getRotation(), AnimationState::combineRotations);
        scale = combineProperties(getScale(), other.getScale(), AnimationState::multiply);
        dissolve = combineProperties(getDissolve(), other.getDissolve(), AnimationState::multiply);
        dissolveArrow = combineProperties(getDissolveArrow(), other.getDissolveArrow(), AnimationState::multiply);
        interactable = combineProperties(getInteractable(), other.getInteractable(), AnimationState::multiply);
        time = combineProperties(getTime(), other.getTime(), AnimationState::multiply);
        color = combineProperties(getColor(), other.getColor(), AnimationState::multiply);

        return state;
    }

    private static float multiply(float a, float b) {
        return a * b;
    }
    private static Vector4f multiply(Vector4f a, Vector4f b) {
        return new Vector4f().set(a).mul(b);
    }
    private static Vector3f multiply(Vector3f a, Vector3f b) {
        return new Vector3f().set(a).mul(b);
    }
    private static Vector3f add(Vector3f a, Vector3f b) {
        return new Vector3f().set(a).add(b);
    }

    private static Quaternionf combineRotations(Quaternionf a, Quaternionf b) {
        return new Quaternionf().set(a).mul(b);
    }

    private <T> T combineProperties(T a, T b, BiFunction<T, T, T> operation) {
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
