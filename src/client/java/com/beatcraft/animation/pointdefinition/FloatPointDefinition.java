package com.beatcraft.animation.pointdefinition;

public class FloatPointDefinition extends PointDefinition<float> {

    @Override
    protected float interpolatePoints(int a, int b, float time) {
        float left = points.get(a).value;
        float right = points.get(b).value;
        return (right - left) * time + left;
    }
}
