package com.beatcraft.client.debug;

import java.util.HashMap;

public class BeatcraftDebug {

    private static final HashMap<String, Object> debugValueMap = new HashMap<>();

    public static void bindValue(String key, Object value) {
        debugValueMap.put(key, value);
    }

    public static Object getValue(String key) {
        return getValue(key, null);
    }

    public static Object getValue(String key, Object fallback) {
        return debugValueMap.getOrDefault(key, fallback);
    }

    public static void removeValue(String key) {
        debugValueMap.remove(key);
    }

}
