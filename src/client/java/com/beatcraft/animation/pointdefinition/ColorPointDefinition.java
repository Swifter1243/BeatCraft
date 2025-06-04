package com.beatcraft.animation.pointdefinition;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.SaberSyncedColor;
import com.beatcraft.data.types.Color;
import com.beatcraft.utils.JsonUtil;
import com.beatcraft.utils.MathUtil;
import com.google.gson.JsonArray;

public class ColorPointDefinition extends PointDefinition<Color> {

    public ColorPointDefinition(JsonArray json) throws RuntimeException {
        super(json);
    }

    @Override
    protected Color interpolatePoints(int a, int b, float time) {
        var left = points.get(a).getValue();
        var right = points.get(b).getValue();
        return MathUtil.lerpColor(left, right, time);
    }

    @Override
    protected int getValueLength(JsonArray inner) {
        if (inner.get(0).isJsonPrimitive()) {
            var prim = inner.get(0).getAsJsonPrimitive();
            if (prim.isString()) {
                return 1;
            }
        }
        return 4;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Color> point, boolean isSimple) {
        if (isSimple && json.size() == 3) {
            var v = JsonUtil.getVector3(json);
            point.setValue(new Color(v.x, v.y, v.z, 1f));
        } else {
            if (json.get(0).isJsonPrimitive() && json.get(0).getAsJsonPrimitive().isString()) {
                // baseNote0Color or baseNote1Color
                var col = json.get(0).getAsString();
                if (col.equals("baseNote0Color")) {
                    point.setValue(new SaberSyncedColor(0));
                } else if (col.equals("baseNote1Color")) {
                    point.setValue(new SaberSyncedColor(1));
                }
            } else {
                var v = JsonUtil.getVector4(json);
                point.setValue(new Color(v.x, v.y, v.z, v.w));
            }
        }
    }
}
