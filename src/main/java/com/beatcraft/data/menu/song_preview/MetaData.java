package com.beatcraft.data.menu.song_preview;

import com.google.gson.JsonObject;

public record MetaData(
    float bpm,
    int duration,
    String songName,
    String songSubName,
    String songAuthorName,
    String levelAuthorName
) {
    public static MetaData loadJson(JsonObject json) {
        float bpm = json.get("bpm").getAsFloat();
        int duration = json.get("duration").getAsInt();
        String songName = json.get("songName").getAsString();
        String songSubName = json.get("songSubName").getAsString();
        String songAuthorName = json.get("songAuthorName").getAsString();
        String levelAuthorName = json.get("levelAuthorName").getAsString();
        return new MetaData(
            bpm, duration, songName, songSubName,
            songAuthorName, levelAuthorName
        );
    }
}
