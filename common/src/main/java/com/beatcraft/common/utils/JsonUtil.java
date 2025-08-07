package com.beatcraft.common.utils;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.function.Function;

public class JsonUtil {
    public static Vector2f getVector2(JsonArray array) {
        float x = getOrDefault(array, 0, JsonElement::getAsFloat, 0f);
        float y = getOrDefault(array, 1, JsonElement::getAsFloat, 0f);
        return new Vector2f(x, y);
    }

    public static Vector3f getVector3(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        return getVector3(array);
    }
    public static Vector3f getVector3(JsonArray array) {
        float x = getOrDefault(array, 0, JsonElement::getAsFloat, 0f);
        float y = getOrDefault(array, 1, JsonElement::getAsFloat, 0f);
        float z = getOrDefault(array, 2, JsonElement::getAsFloat, 0f);
        return new Vector3f(x, y, z);
    }

    public static Vector3f getVector3f(JsonArray array, HashMap<String, Float> variableLookup) {
        float[] values = new float[3];

        for (int i = 0; i < 3; i++) {
            JsonElement element = array.get(i);

            if (!element.isJsonPrimitive()) {
                throw new IllegalArgumentException("Expected a primitive at index " + i);
            }

            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                values[i] = primitive.getAsFloat();
            } else if (primitive.isString()) {
                String key = primitive.getAsString();
                Float value = variableLookup.get(key);
                if (value == null) {
                    throw new IllegalArgumentException("Variable '" + key + "' not found in lookup map");
                }
                values[i] = value;
            } else {
                throw new IllegalArgumentException("Unsupported primitive type at index " + i);
            }
        }

        return new Vector3f(values[0], values[1], values[2]);
    }

    public static Vector4f getVector4(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        return getVector4(array);
    }
    public static Vector4f getVector4(JsonArray array) {
        float x = getOrDefault(array, 0, JsonElement::getAsFloat, 0f);
        float y = getOrDefault(array, 1, JsonElement::getAsFloat, 0f);
        float z = getOrDefault(array, 2, JsonElement::getAsFloat, 0f);
        float w = getOrDefault(array, 3, JsonElement::getAsFloat, 0f);
        return new Vector4f(x, y, z, w);
    }

    public static<T> T getOrDefault(JsonObject json, String key, Function<JsonElement, T> getter, T fallback) {
        if (json.has(key)) {
            return getter.apply(json.get(key));
        } else {
            return fallback;
        }
    }

    public static<T> T getOrDefault(JsonArray array, int index, Function<JsonElement, T> getter, T fallback) {
        if (array.size() > index) {
            return getter.apply(array.get(index));
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