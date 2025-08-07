package com.beatcraft.client.animation.base_providers;

import com.beatcraft.Beatcraft;
import com.google.gson.JsonArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BaseProviderHandler {


    private static final HashMap<String, ValueProvider> baseProviders = new HashMap<>();
    private static final HashSet<UpdatableValue> updatableBaseProviders = new HashSet<>();
    private static final HashMap<String, UpdatableValue> updatableProviders = new HashMap<>();

    public static void update() {
        updatableProviders.values().forEach(UpdatableValue::update);
        updatableBaseProviders.forEach(UpdatableValue::update);
    }

    public static void clear() {
        updatableProviders.clear();
    }

    public static ValueProvider getProvider(String provider) {
        var splits = provider.split("\\.");
        ValueProvider base = baseProviders.get(splits[0]);

        if (splits.length == 1) {
            return base;
        }

        if (updatableProviders.containsKey(provider)) {
            return updatableProviders.get(provider);
        }

        String subkey = splits[0];
        for (int i = 1; i < splits.length; i++) {
            var split = splits[i];
            subkey += "." + split;

            if (updatableProviders.containsKey(subkey)) {
                base = updatableProviders.get(subkey);
            } else {

                if (split.startsWith("s")) {
                    float mult = Float.parseFloat(split.substring(1).replace("_", "."));
                    base = (base instanceof RotationProvider)
                        ? new SmoothRotationProvider((RotationProvider) base, mult)
                        : new SmoothValueProvider(base.getValues(), mult);

                } else {
                    var parts = new int[split.length()];

                    for (int c = 0; c < split.length(); c++) {
                        parts[c] = switch (split.charAt(c)) {
                            case 'x' -> 0;
                            case 'y' -> 1;
                            case 'z' -> 2;
                            case 'w' -> 3;
                            default -> throw new IllegalArgumentException("swizzle can only contain x, y, z, and w");
                        };
                    }

                    base = new SwizzleProvider(base.getValues(), parts);

                }

                updatableProviders.put(subkey, (UpdatableValue) base);

            }

        }

        return base;
    }

    public static ValueProvider parseFromJson(JsonArray json, int expectedSize) {
        var a = json.get(0);
        if (a.isJsonPrimitive()) {
            if (a.getAsJsonPrimitive().isString()) {
                var s = a.getAsString();
                return getProvider(s);
            } else if (a.getAsJsonPrimitive().isNumber()) {
                float[] vals = new float[expectedSize];
                for (int i = 0; i < expectedSize; i++) {
                    var x = json.get(i);
                    if (x.isJsonPrimitive() && x.getAsJsonPrimitive().isNumber()) {
                        vals[i] = x.getAsFloat();
                    } else {
                        Beatcraft.LOGGER.warn("unhandled non-float value: {}", x);
                        vals[i] = 1;
                    }
                }
                return new StaticValueProvider(vals);
            } else {
                Beatcraft.LOGGER.warn("Using fallback provider of 1s");
                float[] vals = new float[expectedSize];
                Arrays.fill(vals, 1);
                return new StaticValueProvider(vals);
            }
        } else if (a.isJsonArray()) {
            var b = a.getAsJsonArray();
            float[] vals = new float[expectedSize];
            for (int i = 0; i < expectedSize; i++) {
                var x = b.get(i);
                if (x.isJsonPrimitive() && x.getAsJsonPrimitive().isNumber()) {
                    vals[i] = x.getAsFloat();
                } else {
                    Beatcraft.LOGGER.warn("unhandled non-float value (2): {}", x);
                    vals[i] = 1;
                }
            }
            return new StaticValueProvider(vals);
        } else {
            Beatcraft.LOGGER.warn("using fallback provider of 1s");
            var f = new float[expectedSize];
            Arrays.fill(f, 1);
            return new StaticValueProvider(f);
        }
    }

}
