package com.beatcraft.animation.pointdefinition;

import com.beatcraft.BeatCraft;
import com.beatcraft.base_providers.BaseProviderHandler;
import com.beatcraft.base_providers.ColorReader;
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
            if (isModifier(json)) {
                var v = BaseProviderHandler.parseFromJson(json, 4);
                point.setValue(new ColorReader(v.getValues()));
            } else {
                var v = JsonUtil.getVector4(json);
                point.setValue(new Color(v.x, v.y, v.z, v.w));
            }
        }
    }
}
