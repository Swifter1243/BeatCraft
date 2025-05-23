package com.beatcraft.beatmap;

import com.beatcraft.audio.AudioInfo;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.types.Color;
import com.beatcraft.beatmap.data.ColorScheme;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

public class Info {
    private float bpm;
    private String version;
    private String environmentName;
    private String songFilename;
    private String mapDirectory;
    private AudioInfo audioInfo = null;

    private final HashMap<String, StyleSet> styleSets = new HashMap<>();

    public float getBeat(float time, float speedModifier) {
        if (audioInfo == null) {
            return (bpm / 60f) * time * speedModifier;
        } else {
            return audioInfo.getBeat(time, speedModifier);
        }
    }

    public float getTime(float beat, float speedModifier) {
        if (audioInfo == null) {
            return beat * (60 / bpm);
        } else {
            return audioInfo.getTime(beat, speedModifier);
        }
    }

    public float getSongDuration() {
        if (audioInfo == null) {
            return BeatmapAudioPlayer.beatmapAudio.getSongDuration();
        } else {
            return audioInfo.getSongDuration();
        }
    }

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

    public int getMajorVersion() {
        return Integer.parseInt((Arrays.stream(version.split("\\.")).toList()).getFirst());
    }

    public int getMinorVersion() {
        return Integer.parseInt((Arrays.stream(version.split("\\.")).toList()).get(1));
    }

    public static Info from(JsonObject json, String path) throws IOException {
        Info info = new Info();

        info.mapDirectory = Path.of(path).getParent().toString();

        if (json.has("version")) {
            info.version = json.get("version").getAsString();
        } else {
            info.version = json.get("_version").getAsString();
        }

        if (json.has("audio")) {
            JsonObject audio = json.get("audio").getAsJsonObject();
            info.bpm = audio.get("bpm").getAsFloat();
            info.environmentName = json.get("environmentNames").getAsJsonArray().get(0).getAsString();
            info.songFilename = info.attachPathToMapDirectory(audio.get("songFilename").getAsString());
            String audioDataFileName = audio.get("audioDataFilename").getAsString();
            String audioDataRaw = Files.readString(Path.of(info.mapDirectory + "/" + audioDataFileName));
            JsonObject audioJson = JsonParser.parseString(audioDataRaw).getAsJsonObject();
            info.audioInfo = AudioInfo.loadV4(audioJson);
        } else {
            info.bpm = json.get("_beatsPerMinute").getAsFloat();
            info.environmentName = json.get("_environmentName").getAsString();
            info.songFilename = info.attachPathToMapDirectory(json.get("_songFilename").getAsString());

        }
        return info;
    }

    public static class SetDifficulty {
        private float njs;
        private float offset;
        private ColorScheme colorScheme;
        private String lightshowFile;

        public static SetDifficulty from(JsonObject json, Info info) {
            SetDifficulty setDifficulty = new SetDifficulty();

            if (json.has("noteJumpMovementSpeed")) {
                setDifficulty.njs = json.get("noteJumpMovementSpeed").getAsFloat();
                setDifficulty.offset = json.get("noteJumpStartBeatOffset").getAsFloat();
            }
            else {
                setDifficulty.njs = json.get("_noteJumpMovementSpeed").getAsFloat();
                setDifficulty.offset = json.get("_noteJumpStartBeatOffset").getAsFloat();
                if (setDifficulty.njs == 0) { // This fixes some old maps such as OST 1 songs
                    setDifficulty.njs = 16.0f;
                }
            }
            setDifficulty.colorScheme = ColorScheme.getEnvironmentColorScheme(info.getEnvironmentName());

            setDifficulty.lightshowFile = JsonUtil.getOrDefault(json, "lightshowDataFilename", JsonElement::getAsString, null);

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

        public @Nullable String getLightshowFile() {
            return lightshowFile;
        }
    }
}
