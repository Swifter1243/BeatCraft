package com.beatcraft.client.animation.base_providers;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.ColorScheme;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.google.gson.JsonArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Map.entry;

public class BaseProviderHandler {

    public final BeatmapController controller;

    public BaseProviderHandler(BeatmapController player) {
        controller = player;
    }

    private final HashMap<String, ValueProvider> baseProviders = new HashMap<>();
    private final HashSet<UpdatableValue> updatableBaseProviders = new HashSet<>();
    private final HashMap<String, UpdatableValue> updatableProviders = new HashMap<>();

    public void update() {
        updatableProviders.values().forEach(UpdatableValue::update);
        updatableBaseProviders.forEach(UpdatableValue::update);
    }

    public void clear() {
        updatableProviders.clear();
    }

    public ValueProvider getProvider(String provider) {
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

    public ValueProvider parseFromJson(JsonArray json, int expectedSize) {
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


    ///  these updaters use dynamic values so they will work for any map
    public void setupDynamicProviders() {
        baseProviders.clear();
        updatableBaseProviders.clear();

        var baseHeadPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.headPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseHeadLocalPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.headPos;
            var o = controller.logic.playerGlobalPosition;
            out[0] = v.x - o.x;
            out[1] = v.y - o.y;
            out[2] = v.z - o.z;
        });
        var baseHeadRotation = new BaseRotationProvider(out -> {
            out.set(controller.logic.headRot);
        });
        var baseHeadLocalRotation = new BaseRotationProvider(out -> {
            var h = controller.logic.headRot;
            var o = controller.logic.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
            out.set(h.mul(o));
            MemoryPool.releaseSafe(o);
        });
        var baseHeadLocalScale = new BaseValueProvider(3, out -> {
            // TODO: implement scale I guess
            out[0] = 1;
            out[1] = 1;
            out[2] = 1;
        });

        var baseLeftHandPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.leftSaberPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseLeftHandLocalPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.leftSaberPos;
            var o = controller.logic.playerGlobalPosition;
            out[0] = v.x - o.x;
            out[1] = v.y - o.y;
            out[2] = v.z - o.z;
        });
        var baseLeftHandRotation = new BaseRotationProvider(out -> {
            out.set(controller.logic.leftSaberRotation);
        });
        var baseLeftHandLocalRotation = new BaseRotationProvider(out -> {
            var h = controller.logic.leftSaberRotation;
            var o = controller.logic.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
            out.set(h.mul(o));
            MemoryPool.releaseSafe(o);
        });
        var baseLeftHandLocalScale = new BaseValueProvider(3, out -> {
            // TODO: implement scale I guess
            out[0] = 1;
            out[1] = 1;
            out[2] = 1;
        });

        var baseRightHandPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.rightSaberPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseRightHandLocalPosition = new BaseValueProvider(3, out -> {
            var v = controller.logic.rightSaberPos;
            var o = controller.logic.playerGlobalPosition;
            out[0] = v.x - o.x;
            out[1] = v.y - o.y;
            out[2] = v.z - o.z;
        });
        var baseRightHandRotation = new BaseRotationProvider(out -> {
            out.set(controller.logic.rightSaberRotation);
        });
        var baseRightHandLocalRotation = new BaseRotationProvider(out -> {
            var h = controller.logic.rightSaberRotation;
            var o = controller.logic.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
            out.set(h.mul(o));
            MemoryPool.releaseSafe(o);
        });
        var baseRightHandLocalScale = new BaseValueProvider(3, out -> {
            // TODO: implement scale I guess
            out[0] = 1;
            out[1] = 1;
            out[2] = 1;
        });


        var baseCombo = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getCombo();
        });
        var baseMultipliedScore = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getScore();
        });
        var baseMaxPossibleMultipliedScore = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getMaxPossibleScore();
        });
        var baseModifiedScore = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getScore();
        });
        var baseMaxPossibleModifiedScore = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getMaxPossibleScore();
        });
        var baseRelativeScore = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getAccuracy();
        });
        var baseMultiplier = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getBonusModifier();
        });
        var baseEnergy = new BaseValueProvider(1, out -> {
            out[0] = controller.logic.getHealthPercentage();
        });
        var baseSongTime = new BaseValueProvider(1, out -> {
            out[0] = controller.currentSeconds;
        });
        var baseSongLength = new BaseValueProvider(1, out -> {
            if (controller.info != null) {
                out[0] = controller.info.getSongDuration();
            }
        });


        baseProviders.putAll(Map.ofEntries(
            entry("baseHeadPosition", baseHeadPosition),
            entry("baseHeadLocalPosition", baseHeadLocalPosition),
            entry("baseHeadRotation", baseHeadRotation),
            entry("baseHeadLocalRotation", baseHeadLocalRotation),
            entry("baseHeadLocalScale", baseHeadLocalScale),

            entry("baseLeftHandPosition", baseLeftHandPosition),
            entry("baseLeftHandLocalPosition", baseLeftHandLocalPosition),
            entry("baseLeftHandRotation", baseLeftHandRotation),
            entry("baseLeftHandLocalRotation", baseLeftHandLocalRotation),
            entry("baseLeftHandLocalScale", baseLeftHandLocalScale),

            entry("baseRightHandPosition", baseRightHandPosition),
            entry("baseRightHandLocalPosition", baseRightHandLocalPosition),
            entry("baseRightHandRotation", baseRightHandRotation),
            entry("baseRightHandLocalRotation", baseRightHandLocalRotation),
            entry("baseRightHandLocalScale", baseRightHandLocalScale),

            entry("baseCombo", baseCombo),
            entry("baseMultipliedScore", baseMultipliedScore),
            entry("baseImmediateMaxPossibleMultipliedScore", baseMaxPossibleMultipliedScore),
            entry("baseModifiedScore", baseModifiedScore),
            entry("baseImmediateMaxPossibleModifiedScore", baseMaxPossibleModifiedScore),
            entry("baseRelativeScore", baseRelativeScore),
            entry("baseMultiplier", baseMultiplier),
            entry("baseEnergy", baseEnergy),
            entry("baseSongTime", baseSongTime),
            entry("baseSongLength", baseSongLength)
        ));
        updatableBaseProviders.addAll(baseProviders.values().stream().map(v -> (UpdatableValue) v).toList());

    }

    private BaseValueProvider colorProvider(Color color) {
        return new BaseValueProvider(4, out -> {
            out[0] = color.getRed();
            out[1] = color.getGreen();
            out[2] = color.getBlue();
            out[3] = color.getAlpha();
        });
    }

    public void setupStaticProviders(ColorScheme cs) {

        var note0 = cs.getNoteLeftColor();
        var note1 = cs.getNoteRightColor();
        var obstacle = cs.getObstacleColor();
        // skip saber colors for now

        var envC0 = cs.getEnvironmentLeftColor();
        var envC1 = cs.getEnvironmentRightColor();
        var envCW = cs.getEnvironmentWhiteColor();

        var envB0 = cs.getEnvironmentLeftColorBoost();
        var envB1 = cs.getEnvironmentRightColorBoost();
        var envBW = cs.getEnvironmentWhiteColorBoost();

        baseProviders.putAll(Map.ofEntries(
            entry("baseNote0Color", colorProvider(note0)),
            entry("baseNote1Color", colorProvider(note1)),
            entry("baseObstacleColor", colorProvider(obstacle)),
            entry("baseSaberAColor", colorProvider(note0)),
            entry("baseSaberBColor", colorProvider(note1)),
            entry("baseEnvironmentColor0", colorProvider(envC0)),
            entry("baseEnvironmentColor1", colorProvider(envC1)),
            entry("baseEnvironmentColorW", colorProvider(envCW)),
            entry("baseEnvironmentColor0Boost", colorProvider(envB0)),
            entry("baseEnvironmentColor1Boost", colorProvider(envB1)),
            entry("baseEnvironmentColorWBoost", colorProvider(envBW))
        ));

    }

}
