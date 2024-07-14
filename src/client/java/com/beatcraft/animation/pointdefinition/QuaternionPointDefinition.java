package com.beatcraft.animation.pointdefinition;

import org.joml.Quaternionf;

public class QuaternionPointDefinition extends PointDefinition<Quaternionf> {
    @Override
    protected Quaternionf interpolatePoints(int a, int b, float time) {
        Quaternionf left = points.get(a).getValue();
        Quaternionf right = points.get(b).getValue();
        return left.slerp(right, time);
    }
}
