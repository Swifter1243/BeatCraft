package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.HashMap;

public class Info {
    public float bpm;
    public String environmentName;
    public String songFilename;
    public String mapDirectory;

    public HashMap<String, StyleSet> styleSets = new HashMap<>();
    public static class StyleSet {
        public HashMap<String, SetDifficulty> difficulties = new HashMap<>();
    }

    public String attachPathToMapDirectory(String path) {
        return Path.of(this.mapDirectory, path).toString();
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
        public float njs;
        public float offset;
        public ColorScheme colorScheme;

        public static SetDifficulty from(JsonObject json, Info info) {
            SetDifficulty setDifficulty = new SetDifficulty();

            setDifficulty.njs = json.get("_noteJumpMovementSpeed").getAsFloat();
            setDifficulty.offset = json.get("_noteJumpStartBeatOffset").getAsFloat();
            setDifficulty.colorScheme = ColorScheme.getEnvironmentColorScheme(info.environmentName);

            if (json.has("_customData")) {
                JsonObject customData = json.get("_customData").getAsJsonObject();

                if (customData.has("_colorLeft")) {
                    setDifficulty.colorScheme.noteLeftColor = Color.fromJsonObject(customData.get("_colorLeft").getAsJsonObject());
                }
                if (customData.has("_colorRight")) {
                    setDifficulty.colorScheme.noteRightColor = Color.fromJsonObject(customData.get("_colorRight").getAsJsonObject());
                }
                if (customData.has("_obstacleColor")) {
                    setDifficulty.colorScheme.obstacleColor = Color.fromJsonObject(customData.get("_obstacleColor").getAsJsonObject());
                }
                if (customData.has("_envColorLeft")) {
                    setDifficulty.colorScheme.environmentLeftColor = Color.fromJsonObject(customData.get("_envColorLeft").getAsJsonObject());
                }
                if (customData.has("_envColorLeftBoost")) {
                    setDifficulty.colorScheme.environmentLeftColorBoost = Color.fromJsonObject(customData.get("_envColorLeftBoost").getAsJsonObject());
                }
                if (customData.has("_envColorRight")) {
                    setDifficulty.colorScheme.environmentRightColor = Color.fromJsonObject(customData.get("_envColorRight").getAsJsonObject());
                }
                if (customData.has("_envColorRightBoost")) {
                    setDifficulty.colorScheme.environmentRightColorBoost = Color.fromJsonObject(customData.get("_envColorRightBoost").getAsJsonObject());
                }
                if (customData.has("_envColorWhite")) {
                    setDifficulty.colorScheme.environmentWhiteColor = Color.fromJsonObject(customData.get("_envColorWhite").getAsJsonObject());
                }
                if (customData.has("_envColorWhiteBoost")) {
                    setDifficulty.colorScheme.environmentWhiteColorBoost = Color.fromJsonObject(customData.get("_envColorWhiteBoost").getAsJsonObject());
                }
            }

            return setDifficulty;
        }
    }
}
