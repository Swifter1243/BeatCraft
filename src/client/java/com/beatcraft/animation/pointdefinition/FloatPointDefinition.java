package com.beatcraft.animation.pointdefinition;

import org.joml.Math;

public class FloatPointDefinition extends PointDefinition<Float> {

    @Override
    protected Float interpolatePoints(int a, int b, float time) {
        float left = points.get(a).value;
        float right = points.get(b).value;
        return Math.lerp(left, right, time);
    }
}
