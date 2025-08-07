package com.beatcraft.client.animation.pointdefinition;

import com.beatcraft.client.animation.base_providers.BaseProviderHandler;
import com.beatcraft.client.animation.base_providers.FloatReader;
import com.google.gson.JsonArray;
import org.joml.Math;

public class FloatPointDefinition extends PointDefinition<Float> {

    public FloatPointDefinition(JsonArray json) throws RuntimeException {
        super(json);
    }

    @Override
    protected Float interpolatePoints(int a, int b, float time) {
        float left = points.get(a).getValue();
        float right = points.get(b).getValue();
        return Math.lerp(left, right, time);
    }

    @Override
    protected int getValueLength(JsonArray ignored) {
        return 1;
    }

    @Override
    protected void loadValue(JsonArray json, Point<Float> point, boolean isSimple) {
        if (isModifier(json)) {
            var p = BaseProviderHandler.parseFromJson(json, 1);
            point.setValue(new FloatReader(p.getValues()));
        } else {
            float value = json.get(0).getAsFloat();
            point.setValue(value);
        }
    }
}

