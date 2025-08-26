package com.beatcraft.common.data.map.song_preview;

import com.google.gson.JsonObject;

public record UploaderData(
    int id,
    String name,
    String avatarURL,
    String playlistURL
) {
    public static UploaderData loadJson(JsonObject json) {
        int id = json.get("id").getAsInt();
        String name = json.get("name").getAsString();
        String avatarURL = json.get("avatar").getAsString();
        String playlistUrl = json.get("playlistUrl").getAsString();
        return new UploaderData(id, name, avatarURL, playlistUrl);
    }
}