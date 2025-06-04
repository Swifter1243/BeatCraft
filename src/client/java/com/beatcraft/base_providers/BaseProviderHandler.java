package com.beatcraft.base_providers;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.types.Color;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.memory.MemoryPool;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Map.entry;

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

    }

    ///  these updaters use dynamic values so they will work for any map
    public static void setupDynamicProviders() {
        baseProviders.clear();
        updatableBaseProviders.clear();

        var baseHeadPosition = new BaseValueProvider(3, out -> {
            var v = GameLogicHandler.headPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseHeadLocalPosition = new BaseValueProvider(3, out -> {
            var v = GameLogicHandler.headPos;
            var o = BeatCraftClient.playerGlobalPosition;
            out[0] = v.x - (float) o.x;
            out[1] = v.y - (float) o.y;
            out[2] = v.z - (float) o.z;
        });
        var baseHeadRotation = new BaseRotationProvider(out -> {
            out.set(GameLogicHandler.headRot);
        });
        var baseHeadLocalRotation = new BaseRotationProvider(out -> {
            var h = GameLogicHandler.headRot;
            var o = BeatCraftClient.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
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
            var v = GameLogicHandler.leftSaberPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseLeftHandLocalPosition = new BaseValueProvider(3, out -> {
            var v = GameLogicHandler.leftSaberPos;
            var o = BeatCraftClient.playerGlobalPosition;
            out[0] = v.x - (float) o.x;
            out[1] = v.y - (float) o.y;
            out[2] = v.z - (float) o.z;
        });
        var baseLeftHandRotation = new BaseRotationProvider(out -> {
            out.set(GameLogicHandler.leftSaberRotation);
        });
        var baseLeftHandLocalRotation = new BaseRotationProvider(out -> {
            var h = GameLogicHandler.leftSaberRotation;
            var o = BeatCraftClient.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
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
            var v = GameLogicHandler.rightSaberPos;
            out[0] = v.x;
            out[1] = v.y;
            out[2] = v.z;
        });
        var baseRightHandLocalPosition = new BaseValueProvider(3, out -> {
            var v = GameLogicHandler.rightSaberPos;
            var o = BeatCraftClient.playerGlobalPosition;
            out[0] = v.x - (float) o.x;
            out[1] = v.y - (float) o.y;
            out[2] = v.z - (float) o.z;
        });
        var baseRightHandRotation = new BaseRotationProvider(out -> {
            out.set(GameLogicHandler.rightSaberRotation);
        });
        var baseRightHandLocalRotation = new BaseRotationProvider(out -> {
            var h = GameLogicHandler.rightSaberRotation;
            var o = BeatCraftClient.playerGlobalRotation.conjugate(MemoryPool.newQuaternionf());
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
            out[0] = GameLogicHandler.getCombo();
        });
        var baseMultipliedScore = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getScore();
        });
        var baseMaxPossibleMultipliedScore = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getMaxPossibleScore();
        });
        var baseModifiedScore = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getScore();
        });
        var baseMaxPossibleModifiedScore = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getMaxPossibleScore();
        });
        var baseRelativeScore = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getAccuracy();
        });
        var baseMultiplier = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getBonusModifier();
        });
        var baseEnergy = new BaseValueProvider(1, out -> {
            out[0] = GameLogicHandler.getHealthPercentage();
        });
        var baseSongTime = new BaseValueProvider(1, out -> {
            out[0] = BeatmapPlayer.getCurrentSeconds();
        });
        var baseSongLength = new BaseValueProvider(1, out -> {
            if (BeatmapPlayer.currentInfo != null) {
                out[0] = BeatmapPlayer.currentInfo.getSongDuration();
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

            entry("baseRightHandePosition", baseRightHandPosition),
            entry("baseRightHandeLocalPosition", baseRightHandLocalPosition),
            entry("baseRightHandeRotation", baseRightHandRotation),
            entry("baseRightHandeLocalRotation", baseRightHandLocalRotation),
            entry("baseRightHandeLocalScale", baseRightHandLocalScale),

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

    private static BaseValueProvider colorProvider(Color color) {
        return new BaseValueProvider(4, out -> {
            out[0] = color.getRed();
            out[1] = color.getGreen();
            out[2] = color.getBlue();
            out[3] = color.getAlpha();
        });
    }

    public static void setupStaticProviders() {
        var cs = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme();

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
