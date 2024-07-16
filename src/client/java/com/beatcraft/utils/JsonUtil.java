package com.beatcraft.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class JsonUtil {
    public static Vector3f getVector3(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        float x = array.get(0).getAsFloat();
        float y = array.get(1).getAsFloat();
        float z = array.get(2).getAsFloat();
        return new Vector3f(x, y, z);
    }

    public static Quaternionf getQuaternion(JsonElement element) {
        Vector3f euler = getVector3(element);
        return MathUtil.eulerToQuaternion(euler);
    }
    public static Quaternionf getQuaternion(JsonObject object, String property, Quaternionf defaultValue) {
        if (object.has(property)) {
            return getQuaternion(object.get("property"));
        } else {
            return defaultValue;
        }
    }
}
