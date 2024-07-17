package com.beatcraft.animation.pointdefinition;

import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import org.joml.Quaternionf;

public class QuaternionPointDefinition extends PointDefinition<Quaternionf> {
    public QuaternionPointDefinition(JsonArray json) throws RuntimeException {
        super(json);
    }

    @Override
    protected Quaternionf interpolatePoints(int a, int b, float time) {
        Quaternionf left = points.get(a).getValue();
        Quaternionf right = points.get(b).getValue();
        return new Quaternionf(left).slerp(right, time);
    }

    @Override
    protected int getValueLength() {
        return 3;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Quaternionf> point, boolean isSimple) {
        Quaternionf quaternion = JsonUtil.getQuaternion(json);
        point.setValue(quaternion);
    }
}
