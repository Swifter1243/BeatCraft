package com.beatcraft.animation.pointdefinition;

import org.joml.Vector4f;

public class Vector4PointDefinition extends PointDefinition<Vector4f> {

    @Override
    protected Vector4f interpolatePoints(int a, int b, float time) {
        Vector4f right = points.get(b).getValue();
        Vector4f left = points.get(a).getValue();
        return left.lerp(right, time);
    }
}
