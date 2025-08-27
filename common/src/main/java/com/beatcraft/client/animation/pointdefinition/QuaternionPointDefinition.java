package com.beatcraft.client.animation.pointdefinition;

import com.beatcraft.client.animation.base_providers.BaseProviderHandler;
import com.beatcraft.client.animation.base_providers.QuaternionReader;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.common.utils.JsonUtil;
import com.beatcraft.common.utils.MathUtil;
import com.google.gson.JsonArray;
import org.joml.Quaternionf;

public class QuaternionPointDefinition extends PointDefinition<Quaternionf> {
    public QuaternionPointDefinition(BeatmapPlayer map, JsonArray json) throws RuntimeException {
        super(map, json);
    }

    @Override
    protected Quaternionf interpolatePoints(int a, int b, float time) {
        Quaternionf left = points.get(a).getValue();
        Quaternionf right = points.get(b).getValue();
        return MathUtil.lerpQuaternion(left, right, time);
    }

    @Override
    protected int getValueLength(JsonArray ignored) {
        return 3;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Quaternionf> point, boolean isSimple) {
        if (isModifier(json)) {
            var p = map.baseProvider.parseFromJson(json, 4);
            point.setValue(new QuaternionReader(p.getValues()));
        } else {
            Quaternionf quaternion = JsonUtil.getQuaternion(json);
            point.setValue(quaternion);
        }
    }
}