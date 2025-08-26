package com.beatcraft.common.data.map.song_preview;

import com.beatcraft.Beatcraft;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record SongPreview(
    String id,
    String name,
    String description,
    UploaderData uploaderData,
    MetaData metaData,
    Stats stats,
    String timeUploaded,
    boolean automapper,
    boolean ranked,
    boolean qualified,
    List<SongVersion> versions
) {
    public static SongPreview loadJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString().replace("\r\n", "\n");
        String description = json.get("description").getAsString().replace("\r\n", "\n");
        UploaderData uploader = UploaderData.loadJson(json.get("uploader").getAsJsonObject());
        MetaData metadata = MetaData.loadJson(json.get("metadata").getAsJsonObject());
        Stats stats = Stats.loadJson(json.get("stats").getAsJsonObject());
        String uploaded = json.get("uploaded").getAsString();
        boolean automapper = json.get("automapper").getAsBoolean();
        boolean ranked = json.get("ranked").getAsBoolean();
        boolean qualified = json.get("qualified").getAsBoolean();

        JsonArray rawVersions = json.getAsJsonArray("versions");
        ArrayList<SongVersion> versions = new ArrayList<>();
        rawVersions.forEach(rawVersion -> {
            JsonObject versionData = rawVersion.getAsJsonObject();
            try {
                SongVersion version = SongVersion.loadJson(versionData);
                versions.add(version);
            } catch (Exception e) {
                Beatcraft.LOGGER.info("Failed to parse response! {}", rawVersion, e);
            }
        });

        return new SongPreview(
            id, name, description, uploader, metadata, stats,
            uploaded, automapper, ranked, qualified, versions
        );
    }

    public String getSets() {
        ArrayList<String> sets = new ArrayList<>();
        for (SongVersion v : versions) {
            v.getSets().forEach(s -> {
                if (!sets.contains(s)) {
                    sets.add(s);
                }
            });
        }
        return String.join(" | ", sets);
    }

    public String getDiffs() {
        ArrayList<String> diffs = new ArrayList<>();
        for (SongVersion v : versions) {
            v.getDiffs().forEach(s -> {
                if (!diffs.contains(s)) {
                    diffs.add(s);
                }
            });
        }
        return String.join(" | ", diffs);
    }

}
