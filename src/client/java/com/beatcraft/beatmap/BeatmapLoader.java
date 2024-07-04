package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.Info;
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

        Info info = new Info().load(json);

        JsonArray styleSetsRaw = json.get("_difficultyBeatmapSets").getAsJsonArray();
        styleSetsRaw.forEach(styleSetRaw -> {
            JsonObject styleSetObject = styleSetRaw.getAsJsonObject();
            Info.StyleSet styleSet = new Info.StyleSet();

            String styleKey = styleSetObject.get("_beatmapCharacteristicName").getAsString();
            info.styleSets.put(styleKey, styleSet);

            JsonArray difficultiesRaw = styleSetObject.get("_difficultyBeatmaps").getAsJsonArray();
            difficultiesRaw.forEach(difficultyRaw -> {
                JsonObject difficultyObject = difficultyRaw.getAsJsonObject();
                Info.SetDifficulty setDifficulty = new Info.SetDifficulty().load(difficultyObject);
                String fileName = difficultyObject.get("_beatmapFilename").getAsString();
                styleSet.difficulties.put(fileName, setDifficulty);
            });
        });

        return info;
    }

    public static String getPathFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public static Info.SetDifficulty getSetDifficulty(String fileName, Info info) {
        for (Info.StyleSet styleSet : info.styleSets.values()) {
            for (var entry : styleSet.difficulties.entrySet()) {
                if (Objects.equals(entry.getKey(), fileName)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public static Difficulty getDifficultyFromFile(String path, Info info) throws IOException {
        String fileName = getPathFileName(path);
        Info.SetDifficulty setDifficulty = getSetDifficulty(fileName, info);
        return getDifficultyFromFile(path, setDifficulty);
    }

    public static Difficulty getDifficultyFromFile(String path, Info.SetDifficulty setDifficulty) throws IOException {
        String jsonString = Files.readString(Paths.get(path));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        String version = "";
        if (json.has("version")) {
            version = json.get("version").getAsString();
        } else {
            version = json.get("_version").getAsString();
        }
        int majorVersion = Integer.parseInt(version.substring(0, 1));

        switch (majorVersion) {
            case 3: {
                return new DifficultyV3().load(json, setDifficulty);
            }
            default: {
                throw new UnrecognizedFormatException();
            }
        }
    }
}
