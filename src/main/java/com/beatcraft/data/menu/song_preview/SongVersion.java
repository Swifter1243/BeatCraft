package com.beatcraft.data.menu.song_preview;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record SongVersion(
    String hash,
    String state,
    List<SongDifficulty> diffs,
    String downloadURL,
    String coverURL,
    String previewURL
) {
    public static SongVersion loadJson(JsonObject json) {
        String hash = json.get("hash").getAsString();
        String state = json.get("state").getAsString();
        String downloadURL = json.get("downloadURL").getAsString();
        String coverURL = json.get("coverURL").getAsString();
        String previewURL = json.get("previewURL").getAsString();

        JsonArray rawDiffData = json.getAsJsonArray("diffs");
        ArrayList<SongDifficulty> diffs = new ArrayList<>();
        rawDiffData.forEach(rawDiff -> {
            JsonObject diffData = rawDiff.getAsJsonObject();
            SongDifficulty diff = SongDifficulty.loadJson(diffData);
            diffs.add(diff);
        });

        return new SongVersion(
            hash, state, diffs,
            downloadURL, coverURL, previewURL
        );
    }

    public List<String> getSets() {
        ArrayList<String> out = new ArrayList<>();
        diffs.forEach(d -> {
            out.add(d.characteristic());
        });
        return out;
    }

    public List<String> getDiffs() {
        ArrayList<String> out = new ArrayList<>();
        diffs.forEach(d -> {
            out.add(d.difficulty());
        });
        return out;
    }
}
