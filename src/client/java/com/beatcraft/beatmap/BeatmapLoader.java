package com.beatcraft.beatmap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class BeatmapLoader {
    public static Info getInfoFromFile(String path) throws IOException {
        String jsonString = Files.readString(Paths.get(path));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        Info info = Info.from(json, path);

        if (json.has("difficultyBeatmaps")) {
            JsonArray styleSetsRaw = json.get("difficultyBeatmaps").getAsJsonArray();
            styleSetsRaw.forEach(styleSetRaw -> {
                JsonObject styleSetObject = styleSetRaw.getAsJsonObject();

                String styleKey = styleSetObject.get("characteristic").getAsString();
                if (!info.getStyleSets().containsKey(styleKey)) {
                    info.getStyleSets().put(styleKey, new Info.StyleSet());
                }

                Info.StyleSet styleSet = info.getStyleSets().get(styleKey);

                Info.SetDifficulty setDifficulty = Info.SetDifficulty.from(styleSetObject, info);
                String fileName = styleSetObject.get("beatmapDataFilename").getAsString();
                styleSet.difficulties.put(fileName, setDifficulty);

            });
        }
        else {
            JsonArray styleSetsRaw = json.get("_difficultyBeatmapSets").getAsJsonArray();
            styleSetsRaw.forEach(styleSetRaw -> {
                JsonObject styleSetObject = styleSetRaw.getAsJsonObject();
                Info.StyleSet styleSet = new Info.StyleSet();

                String styleKey = styleSetObject.get("_beatmapCharacteristicName").getAsString();
                info.getStyleSets().put(styleKey, styleSet);

                JsonArray difficultiesRaw = styleSetObject.get("_difficultyBeatmaps").getAsJsonArray();
                difficultiesRaw.forEach(difficultyRaw -> {
                    JsonObject difficultyObject = difficultyRaw.getAsJsonObject();
                    Info.SetDifficulty setDifficulty = Info.SetDifficulty.from(difficultyObject, info);
                    String fileName = difficultyObject.get("_beatmapFilename").getAsString();
                    styleSet.difficulties.put(fileName, setDifficulty);
                });
            });
        }
        return info;
    }

    public static String getPathFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public static Info.SetDifficulty getSetDifficulty(String fileName, Info info) {
        for (Info.StyleSet styleSet : info.getStyleSets().values()) {
            for (var entry : styleSet.difficulties.entrySet()) {
                if (Objects.equals(entry.getKey(), fileName)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private static int getMajorVersion(JsonObject json) {
        String version;
        if (json.has("version")) {
            version = json.get("version").getAsString();
        } else {
            version = json.get("_version").getAsString();
        }
        return Integer.parseInt(version.substring(0, 1));
    }

    public static Difficulty getDifficultyFromFile(String path, Info info) throws IOException {
        String fileName = getPathFileName(path);
        Info.SetDifficulty setDifficulty = getSetDifficulty(fileName, info);
        return getDifficultyFromFile(path, setDifficulty, info);
    }
    public static Difficulty getDifficultyFromFile(String path, Info.SetDifficulty setDifficulty, Info info) throws IOException {
        String jsonString = Files.readString(Paths.get(path));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        int majorVersion = getMajorVersion(json);
        switch (majorVersion) {
            case 2 -> {
                return new DifficultyV2(info, setDifficulty).load(json);
            }
            case 3 -> {
                return new DifficultyV3(info, setDifficulty).load(json);
            }
            case 4 -> {
                return new DifficultyV4(info, setDifficulty).load(json);
            }
            default -> throw new UnrecognizedFormatException();
        }
    }
}
