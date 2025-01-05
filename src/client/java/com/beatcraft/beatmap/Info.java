package com.beatcraft.beatmap;

import com.beatcraft.data.types.Color;
import com.beatcraft.beatmap.data.ColorScheme;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.HashMap;

public class Info {
    private float bpm;
    private String environmentName;
    private String songFilename;
    private String mapDirectory;

    private final HashMap<String, StyleSet> styleSets = new HashMap<>();

    public float getBpm() {
        return bpm;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getSongFilename() {
        return songFilename;
    }

    public String getMapDirectory() {
        return mapDirectory;
    }

    public HashMap<String, StyleSet> getStyleSets() {
        return styleSets;
    }

    public static class StyleSet {
        public HashMap<String, SetDifficulty> difficulties = new HashMap<>();
    }

    public String attachPathToMapDirectory(String path) {
        return Path.of(this.getMapDirectory(), path).toString();
    }

    public static Info from(JsonObject json, String path) {
        Info info = new Info();

        info.mapDirectory = Path.of(path).getParent().toString();

        info.bpm = json.get("_beatsPerMinute").getAsFloat();
        info.environmentName = json.get("_environmentName").getAsString();
        info.songFilename = info.attachPathToMapDirectory(json.get("_songFilename").getAsString());

        return info;
    }

    public static class SetDifficulty {
        private float njs;
        private float offset;
        private ColorScheme colorScheme;

        public static SetDifficulty from(JsonObject json, Info info) {
            SetDifficulty setDifficulty = new SetDifficulty();

            setDifficulty.njs = json.get("_noteJumpMovementSpeed").getAsFloat();
            setDifficulty.offset = json.get("_noteJumpStartBeatOffset").getAsFloat();
            setDifficulty.colorScheme = ColorScheme.getEnvironmentColorScheme(info.getEnvironmentName());

            if (json.has("_customData")) {
                JsonObject customData = json.get("_customData").getAsJsonObject();

                if (customData.has("_colorLeft")) {
                    setDifficulty.getColorScheme().setNoteLeftColor(Color.fromJsonObject(customData.get("_colorLeft").getAsJsonObject()));
                }
                if (customData.has("_colorRight")) {
                    setDifficulty.getColorScheme().setNoteRightColor(Color.fromJsonObject(customData.get("_colorRight").getAsJsonObject()));
                }
                if (customData.has("_obstacleColor")) {
                    setDifficulty.getColorScheme().setObstacleColor(Color.fromJsonObject(customData.get("_obstacleColor").getAsJsonObject()));
                }
                if (customData.has("_envColorLeft")) {
                    setDifficulty.getColorScheme().setEnvironmentLeftColor(Color.fromJsonObject(customData.get("_envColorLeft").getAsJsonObject()));
                }
                if (customData.has("_envColorLeftBoost")) {
                    setDifficulty.getColorScheme().setEnvironmentLeftColorBoost(Color.fromJsonObject(customData.get("_envColorLeftBoost").getAsJsonObject()));
                }
                if (customData.has("_envColorRight")) {
                    setDifficulty.getColorScheme().setEnvironmentRightColor(Color.fromJsonObject(customData.get("_envColorRight").getAsJsonObject()));
                }
                if (customData.has("_envColorRightBoost")) {
                    setDifficulty.getColorScheme().setEnvironmentRightColorBoost(Color.fromJsonObject(customData.get("_envColorRightBoost").getAsJsonObject()));
                }
                if (customData.has("_envColorWhite")) {
                    setDifficulty.getColorScheme().setEnvironmentWhiteColor(Color.fromJsonObject(customData.get("_envColorWhite").getAsJsonObject()));
                }
                if (customData.has("_envColorWhiteBoost")) {
                    setDifficulty.getColorScheme().setEnvironmentWhiteColorBoost(Color.fromJsonObject(customData.get("_envColorWhiteBoost").getAsJsonObject()));
                }
            }

            return setDifficulty;
        }

        public float getNjs() {
            return njs;
        }

        public float getOffset() {
            return offset;
        }

        public ColorScheme getColorScheme() {
            return colorScheme;
        }
    }
}
