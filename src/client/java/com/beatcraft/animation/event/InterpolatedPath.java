package com.beatcraft.animation.event;

import com.beatcraft.animation.pointdefinition.PointDefinition;

public class InterpolatedPath<T> extends Path<T> {
    private final PointDefinition<T> pointsA;
    private final PointDefinition<T> pointsB;
    private final float normalTime;

    public InterpolatedPath(PointDefinition<T> pointsA, PointDefinition<T> pointsB, float normalTime) {
        this.pointsA = pointsA;
        this.pointsB = pointsB;
        this.normalTime = normalTime;
    }

    @Override
    public T interpolate(float time, Interpolation<T> interpolation) {
        T resultA = pointsA.interpolate(time);
        T resultB = pointsB.interpolate(time);
        return interpolation.apply(resultA, resultB, normalTime);
    }
}
