package com.beatcraft.data.menu.song_preview;

import com.google.gson.JsonObject;

public record SongDifficulty(
    float njs,
    float offset,
    int noteCount,
    int bombCount,
    int obstacleCount,
    float nps,
    float length,
    String characteristic,
    String difficulty,
    int eventCount,
    boolean chroma,
    boolean me,
    boolean ne,
    boolean cinema,
    float seconds,
    int maxScore,
    String environment
) {
    public static SongDifficulty loadJson(JsonObject json) {
        float njs = json.get("njs").getAsFloat();
        float offset = json.get("offset").getAsFloat();
        int noteCount = json.get("notes").getAsInt();
        int bombCount = json.get("bombs").getAsInt();
        int obstacleCount = json.get("obstacles").getAsInt();
        float nps = json.get("nps").getAsFloat();
        float length = json.get("length").getAsFloat();
        String characteristic = json.get("characteristic").getAsString();
        String difficulty = json.get("difficulty").getAsString();
        int eventCount = json.get("events").getAsInt();
        boolean chroma = json.get("chroma").getAsBoolean();
        boolean me = json.get("me").getAsBoolean();
        boolean ne = json.get("ne").getAsBoolean();
        boolean cinema = json.get("cinema").getAsBoolean();
        float seconds = json.get("seconds").getAsFloat();
        int maxScore = json.get("maxScore").getAsInt();
        String env = json.get("environment").getAsString();
        return new SongDifficulty(
            njs, offset, noteCount, bombCount, obstacleCount,
            nps, length, characteristic, difficulty, eventCount,
            chroma, me, ne, cinema, seconds, maxScore, env
        );
    }
}
