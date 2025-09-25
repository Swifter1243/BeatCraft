package com.beatcraft.client.animation.pointdefinition;


import com.beatcraft.client.animation.base_providers.Vector4fReader;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.utils.JsonUtil;
import com.beatcraft.common.utils.MathUtil;
import com.google.gson.JsonArray;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vector4PointDefinition extends PointDefinition<Vector4f> {

    public Vector4PointDefinition(BeatmapController map, JsonArray json) throws RuntimeException {
        super(map, json);
    }

    @Override
    protected Vector4f interpolatePoints(int a, int b, float time) {
        Vector4f right = points.get(b).getValue();
        Vector4f left = points.get(a).getValue();
        return MathUtil.lerpVector4(left, right, time);
    }

    @Override
    protected int getValueLength(JsonArray ignored) {
        return 4;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Vector4f> point, boolean isSimple) {
        if (isSimple && json.size() == 3) {
            Vector3f v = JsonUtil.getVector3(json);
            point.setValue(new Vector4f(v.x, v.y, v.z, 1f));
        } else {
            if (isModifier(json)) {
                var v = map.baseProvider.parseFromJson(json, 4);
                point.setValue(new Vector4fReader(v.getValues()));
            } else {
                Vector4f v = JsonUtil.getVector4(json);
                point.setValue(v);
            }
        }
    }
}