package com.beatcraft.client.animation;

public abstract class AnimationPropertyContainer<FloatType, Vector3Type, Vector4Type, QuaternionType, ColorType> {
    protected Vector3Type offsetPosition;
    protected QuaternionType offsetWorldRotation;
    protected QuaternionType localRotation;
    protected Vector3Type localPosition;
    protected Vector3Type definitePosition;
    protected Vector3Type position;
    protected QuaternionType rotation;
    protected Vector3Type scale;
    protected FloatType dissolve;
    protected FloatType dissolveArrow;
    protected FloatType interactable;
    protected FloatType time;
    protected ColorType color;

    public Vector3Type getOffsetPosition() {
        return offsetPosition;
    }

    public QuaternionType getOffsetWorldRotation() {
        return offsetWorldRotation;
    }

    public QuaternionType getLocalRotation() {
        return localRotation;
    }

    public Vector3Type getLocalPosition() {
        return localPosition;
    }

    public Vector3Type getDefinitePosition() {
        return definitePosition;
    }

    public Vector3Type getPosition() {
        return position;
    }

    public QuaternionType getRotation() {
        return rotation;
    }

    public Vector3Type getScale() {
        return scale;
    }

    public FloatType getDissolve() {
        return dissolve;
    }

    public FloatType getDissolveArrow() {
        return dissolveArrow;
    }

    public FloatType getInteractable() {
        return interactable;
    }

    public FloatType getTime() {
        return time;
    }

    public ColorType getColor() {
        return color;
    }
}
