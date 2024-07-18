package com.beatcraft.animation.event;

import com.beatcraft.animation.pointdefinition.PointDefinition;

public class StaticPath<T> extends Path<T> {
    private final PointDefinition<T> points;

    public StaticPath(PointDefinition<T> points) {
        this.points = points;
    }

    @Override
    public T interpolate(float time, Interpolation<T> interpolation) {
        return points.interpolate(time);
    }
}
