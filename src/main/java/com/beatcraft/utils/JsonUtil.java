package com.beatcraft.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Function;

public class JsonUtil {
    public static Vector3f getVector3(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        return getVector3(array);
    }
    public static Vector3f getVector3(JsonArray array) {
        float x = array.get(0).getAsFloat();
        float y = array.get(1).getAsFloat();
        float z = array.get(2).getAsFloat();
        return new Vector3f(x, y, z);
    }

    public static Vector4f getVector4(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        return getVector4(array);
    }
    public static Vector4f getVector4(JsonArray array) {
        float x = array.get(0).getAsFloat();
        float y = array.get(1).getAsFloat();
        float z = array.get(2).getAsFloat();
        float w = array.get(3).getAsFloat();
        return new Vector4f(x, y, z, w);
    }

    public static<T> T getOrDefault(JsonObject json, String key, Function<JsonElement, T> getter, T fallback) {
        if (json.has(key)) {
            return getter.apply(json.get(key));
        } else {
            return fallback;
        }
    }

    public static Quaternionf getQuaternion(JsonElement element) {
        Vector3f euler = getVector3(element);
        return MathUtil.eulerToQuaternion(euler);
    }
    public static Quaternionf getQuaternion(JsonArray array) {
        Vector3f euler = getVector3(array);
        return MathUtil.eulerToQuaternion(euler);
    }
    public static Quaternionf getQuaternion(JsonObject object, String property, Quaternionf defaultValue) {
        if (object.has(property)) {
            return getQuaternion(object.get(property));
        } else {
            return defaultValue;
        }
    }
}
