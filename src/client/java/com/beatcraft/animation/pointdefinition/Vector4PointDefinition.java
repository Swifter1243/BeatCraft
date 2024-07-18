package com.beatcraft.animation.pointdefinition;

import com.beatcraft.utils.JsonUtil;
import com.beatcraft.utils.MathUtil;
import com.google.gson.JsonArray;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vector4PointDefinition extends PointDefinition<Vector4f> {

    public Vector4PointDefinition(JsonArray json) throws RuntimeException {
        super(json);
    }

    @Override
    protected Vector4f interpolatePoints(int a, int b, float time) {
        Vector4f right = points.get(b).getValue();
        Vector4f left = points.get(a).getValue();
        return MathUtil.lerpVector4(left, right, time);
    }

    @Override
    protected int getValueLength() {
        return 4;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Vector4f> point, boolean isSimple) {
        if (isSimple && json.size() == 3) {
            Vector3f v = JsonUtil.getVector3(json);
            point.setValue(new Vector4f(v.x, v.y, v.z, 1f));
        } else {
            Vector4f v = JsonUtil.getVector4(json);
            point.setValue(v);
        }
    }
}
