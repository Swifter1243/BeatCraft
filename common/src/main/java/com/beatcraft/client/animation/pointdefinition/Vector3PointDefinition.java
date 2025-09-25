package com.beatcraft.client.animation.pointdefinition;

import com.beatcraft.client.animation.base_providers.Vector3fReader;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.utils.JsonUtil;
import com.beatcraft.common.utils.MathUtil;
import com.google.gson.JsonArray;
import org.joml.Vector3f;

public class Vector3PointDefinition extends PointDefinition<Vector3f> {
    public Vector3PointDefinition(BeatmapController map, JsonArray json) throws RuntimeException {
        super(map, json);
    }

    @Override
    protected Vector3f interpolatePoints(int a, int b, float time) {
        Point<Vector3f> right = points.get(b);

        if (right.isSpline()) {
            return splineInterpolation(a, b, time);
        } else {
            Point<Vector3f> left = points.get(a);
            return MathUtil.lerpVector3(left.getValue(), right.getValue(), time);
        }
    }

    @Override
    protected int getValueLength(JsonArray ignored) {
        return 3;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Vector3f> point, boolean isSimple) {
        if (isSimple) {
            Vector3f vector = JsonUtil.getVector3(json);
            point.setValue(vector);
        } else {
            if (isModifier(json)) {
                var v = map.baseProvider.parseFromJson(json, 3);
                point.setValue(new Vector3fReader(v.getValues()));
            } else {
                var v = JsonUtil.getVector3(json);
                point.setValue(v);
            }
        }
    }

    protected Vector3f splineInterpolation(int a, int b, float time) {
        Vector3f pa = points.get(a).getValue();
        Vector3f pb = points.get(b).getValue();

        // Catmull-Rom Spline
        Vector3f p0 = a - 1 < 0 ? pa : points.get(a - 1).getValue();
        Vector3f p3 = b + 1 > points.size() - 1 ? pb : points.get(b + 1).getValue();

        float tt = time * time;
        float ttt = tt * time;

        float q0 = -ttt + (2.0f * tt) - time;
        float q1 = (3.0f * ttt) - (5.0f * tt) + 2.0f;
        float q2 = (-3.0f * ttt) + (4.0f * tt) + time;
        float q3 = ttt - tt;

        return new Vector3f(
            0.5f * ((p0.x * q0) + (pa.x * q1) + (pb.x * q2) + (p3.x * q3)),
            .5f * ((p0.y * q0) + (pa.y * q1) + (pb.y * q2) + (p3.y * q3)),
            .5f * ((p0.z * q0) + (pa.z * q1) + (pb.z * q2) + (p3.z * q3))
        );
    }
}