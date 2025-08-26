package com.beatcraft.common.data.map;

import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SongData {

    public record BeatmapInfo(SongData parent, String mapFile, String lightshowFile, List<String> mappers, List<String> lighters) {
        @Override
        public @NotNull String toString() {
            return "BeatmapInfo{" +
                "mapFile='" + mapFile + '\'' +
                ", lightshowFile='" + lightshowFile + '\'' +
                ", mappers=" + mappers +
                ", lighters=" + lighters +
                '}';
        }

        public Path getBeatmapLocation() {
            return Path.of(parent.songFolder.toAbsolutePath() + "/" + mapFile);
        }
    }

    private final Path songFolder;

    private String title;
    private String subtitle;
    private String author;
    private ArrayList<String> mappers = new ArrayList<>();
    private float bpm;
    private float length;
    private String uid = null;

    private String coverImageFilename;

    private float previewStartTime;
    private float previewDuration;
    private String previewFilename;

    // Example structure:
    // {
    //    "Standard": {
    //      "Expert+": "ExpertPlusStandard.dat"
    //    }
    // }
    private final LinkedHashMap<String, LinkedHashMap<String, BeatmapInfo>> beatmaps = new LinkedHashMap<>();

    public SongData(String songFolder) throws IOException {
        File folder = new File(songFolder);

        String songIdBase = folder.getName();

        // Can't wait for this to cause issues when someone renames a song folder lol
        if (songIdBase.matches("[a-z0-9]+ \\(.*")) {
            uid = songIdBase.split(" ")[0];
        }

        this.songFolder = folder.toPath();

        File infoFile = getFile(songFolder, folder);

        if (infoFile == null) {
            throw new FileNotFoundException("Info.dat not found for song: '" + songFolder + "'");
        }

        String rawInfo = Files.readString(infoFile.toPath());

        JsonObject json = JsonParser.parseString(rawInfo).getAsJsonObject();

        if (json.has("_version")) {
            loadV2(json, folder.toPath());
        } else {
            loadV4(json, folder.toPath());
        }
        this.mappers = new ArrayList<>(this.mappers.stream().distinct().toList());
    }

    private static @Nullable File getFile(String songFolder, File folder) throws FileNotFoundException {
        if (!folder.exists()) {
            throw new FileNotFoundException("Cannot find folder: '" + songFolder + "'");
        }

        if (!folder.isDirectory()) {
            throw new FileNotFoundException("Expected a folder, not a file: '" + songFolder + "'");
        }

        File[] files = folder.listFiles();
        assert files != null;
        File infoFile = null;
        for (File file : files) {
            if (file.getName().equals("Info.dat") || file.getName().equals("info.dat")) {
                infoFile = file;
            }
        }
        return infoFile;
    }

    private void loadV2(JsonObject json, Path folder) {

        title = json.get("_songName").getAsString();
        subtitle = JsonUtil.getOrDefault(json, "_songSubName", JsonElement::getAsString, "");
        author = json.get("_songAuthorName").getAsString();
        String mapper = json.get("_levelAuthorName").getAsString();

        bpm = json.get("_beatsPerMinute").getAsFloat();

        previewStartTime = json.get("_previewStartTime").getAsFloat();
        previewDuration = json.get("_previewDuration").getAsFloat();

        previewFilename = folder.toAbsolutePath() + "/" + json.get("_songFilename").getAsString();

        coverImageFilename = folder.toAbsolutePath() + "/" + json.get("_coverImageFilename").getAsString();

        length = 0; // get from song file itself?

        JsonArray difficultySets = json.getAsJsonArray("_difficultyBeatmapSets");

        difficultySets.forEach(rawSet -> {
            JsonObject set = rawSet.getAsJsonObject();

            String setName = set.get("_beatmapCharacteristicName").getAsString();

            beatmaps.put(setName, new LinkedHashMap<>());

            JsonArray difficulties = set.getAsJsonArray("_difficultyBeatmaps");

            difficulties.forEach(o -> {
                JsonObject obj = o.getAsJsonObject();
                String diff = obj.get("_difficulty").getAsString().replace("ExpertPlus", "Expert+");
                String fileName = obj.get("_beatmapFilename").getAsString();

                if (obj.has("_customData")) {
                    JsonObject cd = obj.getAsJsonObject("_customData");
                    if (cd.has("_difficultyLabel")) {
                        diff = cd.get("_difficultyLabel").getAsString();
                    }
                }

                mappers.add(mapper);

                BeatmapInfo info = new BeatmapInfo(this, fileName, fileName, List.of(mapper), List.of(mapper));

                beatmaps.get(setName).put(diff, info);

            });

        });

    }

    private void loadV4(JsonObject json, Path folder) throws IOException {

        JsonObject songData = json.getAsJsonObject("song");
        JsonObject audioData = json.getAsJsonObject("audio");

        String audioDataFilename = audioData.get("audioDataFilename").getAsString();

        String rawAudioFileData = Files.readString(Path.of(folder.toString() + "/" + audioDataFilename));

        JsonObject rawAudioFileJson = JsonParser.parseString(rawAudioFileData).getAsJsonObject();

        float samples = rawAudioFileJson.get("songSampleCount").getAsFloat();
        float frequency = rawAudioFileJson.get("songFrequency").getAsFloat();

        length = samples / frequency;

        JsonArray beatmapInfo = json.getAsJsonArray("difficultyBeatmaps");

        title = songData.get("title").getAsString();
        subtitle = "";
        author = songData.get("author").getAsString();

        bpm = audioData.get("bpm").getAsFloat();

        previewFilename = folder.toAbsolutePath() + "/" + json.get("songPreviewFilename").getAsString();
        coverImageFilename = folder.toAbsolutePath() + "/" + json.get("coverImageFilename").getAsString();

        previewStartTime = audioData.get("previewStartTime").getAsFloat();
        previewDuration = audioData.get("previewDuration").getAsFloat();

        beatmapInfo.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

            String set = obj.get("characteristic").getAsString();
            String diff = obj.get("difficulty").getAsString().replace("ExpertPlus", "Expert+");

            String mapFile = obj.get("beatmapDataFilename").getAsString();
            String lightFile = obj.get("lightshowDataFilename").getAsString();

            JsonObject authorData = obj.getAsJsonObject("beatmapAuthors");

            JsonArray rawMappers = authorData.getAsJsonArray("mappers");
            JsonArray rawLighters = authorData.getAsJsonArray("lighters");

            ArrayList<String> mappers = new ArrayList<>();
            ArrayList<String> lighters = new ArrayList<>();

            rawMappers.forEach(m -> mappers.add(m.getAsString()));
            rawLighters.forEach(l -> lighters.add(l.getAsString()));

            this.mappers.addAll(mappers);
            this.mappers.addAll(lighters);

            BeatmapInfo info = new BeatmapInfo(this, mapFile, lightFile, mappers, lighters);

            if (!beatmaps.containsKey(set)) {
                beatmaps.put(set, new LinkedHashMap<>());
            }

            beatmaps.get(set).put(diff, info);

        });
    }


    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getId() {
        return uid;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getMappers() {
        return mappers;
    }

    public float getLength() {
        return length;
    }

    public float getBpm() {
        return bpm;
    }

    public float getPreviewDuration() {
        return previewDuration;
    }

    public float getPreviewStartTime() {
        return previewStartTime;
    }

    public String getCoverImageFilename() {
        return coverImageFilename;
    }

    public String getPreviewFilename() {
        return previewFilename;
    }

    public List<String> getDifficultySets() {
        return beatmaps.keySet().stream().toList();
    }

    public List<String> getDifficulties(String difficultySet) {
        return beatmaps.get(difficultySet).keySet().stream().toList();
    }

    public BeatmapInfo getBeatMapInfo(String difficultySet, String difficulty) {
        return beatmaps.get(difficultySet).get(difficulty);
    }

    public Path getSongFolder() {
        return songFolder;
    }


    @Override
    public String toString() {
        return "SongData{" +
            "songFolder=" + songFolder +
            ", title='" + title + '\'' +
            ", subtitle='" + subtitle + '\'' +
            ", author='" + author + '\'' +
            ", bpm=" + bpm +
            ", length=" + length +
            ", coverImageFilename='" + coverImageFilename + '\'' +
            ", previewStartTime=" + previewStartTime +
            ", previewDuration=" + previewDuration +
            ", previewFilename='" + previewFilename + '\'' +
            ", beatmaps=" + beatmaps +
            '}';
    }
}
