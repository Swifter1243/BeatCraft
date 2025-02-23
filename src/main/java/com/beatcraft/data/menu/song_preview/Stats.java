package com.beatcraft.data.menu.song_preview;

import com.google.gson.JsonObject;

public record Stats(
    int plays,
    int downloads,
    int upvotes,
    int downvotes,
    float score
) {
    public static Stats loadJson(JsonObject json) {
        int plays = json.get("plays").getAsInt();
        int downloads = json.get("downloads").getAsInt();
        int upvotes = json.get("upvotes").getAsInt();
        int downvotes = json.get("downvotes").getAsInt();
        float score = json.get("score").getAsFloat();
        return new Stats(
            plays, downloads,
            upvotes, downvotes, score
        );
    }
}
