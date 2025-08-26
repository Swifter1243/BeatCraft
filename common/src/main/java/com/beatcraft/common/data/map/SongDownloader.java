package com.beatcraft.common.data.map;
import com.beatcraft.Beatcraft;
import com.beatcraft.common.data.map.song_preview.SongPreview;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SongDownloader {

    public enum OrderSort {
        None(null),
        Latest("Latest"),
        Relevance("Relevance"),
        Rating("Rating"),
        Curated("Curated"),
        Random("Random");

        private final String val;

        OrderSort(String val) {
            this.val = val;
        }

        public String val() {
            return val;
        }
    }

    public enum LeaderBoardSort {
        None(null),
        All("All"),
        Ranked("Ranked"),
        BeatLeader("BeatLeader"),
        ScoreSaber("ScoreSaber");

        private final String val;

        LeaderBoardSort(String val) {
            this.val = val;
        }

        public String val() {
            return val;
        }
    }

    public enum AutomapperOption {
        BOTH(true, true),
        AI(false, null),
        NO_AI(null, false);

        private final Boolean val;
        private final Boolean val2;

        AutomapperOption(Boolean val, Boolean val2) {
            this.val = val;
            this.val2 = val2;
        }

        public Boolean valSearch() {
            return val;
        }
        public Boolean valLatest() {
            return val2;
        }
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String SEARCH_LATEST_URL = "https://api.beatsaver.com/maps/latest";
    private static final String SEARCH_USER_URL = "https://api.beatsaver.com/maps/uploader/{userId}/{page}";
    private static final String SEARCH_ID_URL = "https://id/{mapId}";
    private static final String SEARCH_URL = "https://api.beatsaver.com/search/v1/";
    private static final String MAP_ID_URL = "https://api.beatsaver.com/maps/id/";
    private static final String ID_FROM_NAME_URL = "https://api.beatsaver.com/users/name/{name}";

    private static final int PAGE_SIZE = 7;

    public static String search = "";
    public static String before = null;
    public static AutomapperOption automapper = AutomapperOption.NO_AI;
    public static int page = 0;
    public static OrderSort order = OrderSort.None;
    public static ArrayList<SongPreview> songPreviews = new ArrayList<>();
    public static LeaderBoardSort leaderBoardSort = LeaderBoardSort.All;
    public static Boolean noodle = null;
    public static Boolean chroma = null;
    public static Boolean verified = null;
    private static CompletableFuture<Void> loadRequest = null;

    public static class SearchQueryBuilder {

        public int page = 0;

        public Boolean ascending = null;
        public AutomapperOption automapper = AutomapperOption.NO_AI;
        public ArrayList<String> characteristics = new ArrayList<>();
        public Boolean chroma = null;
        public Boolean cinema = null;
        public ArrayList<String> collaborator = new ArrayList<>();
        public Boolean curated = null;
        public ArrayList<String> environments = new ArrayList<>();
        //public Boolean followed = null;
        public Instant from = null;
        public Boolean fullSpread = null;
        public LeaderBoardSort leaderboard = LeaderBoardSort.None;
        public Float maxBlStars = null;
        public Float maxBpm = null;
        public Integer maxDownVotes = null;
        public Integer maxDuration = null;
        public Float maxNps = null;
        public Float maxRating = null;
        public Float maxSsStars = null;
        public Integer maxUpVotes = null;
        public Integer maxVotes = null;
        //public Boolean me = null;
        public Float minBlStars = null;
        public Float minBpm = null;
        public Integer minDownVotes = null;
        public Integer minDuration = null;
        public Float minNps = null;
        public Float minRating = null;
        public Float minSsStars = null;
        public Integer minUpVotes = null;
        public Integer minVotes = null;
        public Boolean noodle = null;
        public OrderSort order = OrderSort.None;
        public String q = "";
        public String tags = null;
        public Instant to = null;
        public Boolean verified = null;
        public Boolean vivify = null;

        public String buildUrl() {
            StringBuilder url = new StringBuilder("https://api.beatsaver.com/search/text/" + page);
            ArrayList<String> params = new ArrayList<>();

            addParam(params, "pageSize", String.valueOf(PAGE_SIZE));
            addParam(params, "q", q);
            addBool(params, "ascending", ascending);
            if (automapper != null && automapper != AutomapperOption.NO_AI) {
                addBool(params, "automapper", automapper.valSearch());
            }
            addList(params, "characteristics", characteristics);
            addBool(params, "chroma", chroma);
            addBool(params, "cinema", cinema);
            addList(params, "collaborator", collaborator);
            addBool(params, "curated", curated);
            addList(params, "environments", environments);
            addDate(params, "from", from);
            addBool(params, "fullSpread", fullSpread);
            if (leaderboard != null && leaderboard != LeaderBoardSort.None) {
                addParam(params, "leaderboard", leaderboard.val());
            }
            addFloat(params, "maxBlStars", maxBlStars);
            addFloat(params, "maxBpm", maxBpm);
            addInt(params, "maxDownVotes", maxDownVotes);
            addInt(params, "maxDuration", maxDuration);
            addFloat(params, "maxNps", maxNps);
            addFloat(params, "maxRating", maxRating);
            addFloat(params, "maxSsStars", maxSsStars);
            addInt(params, "maxUpVotes", maxUpVotes);
            addInt(params, "maxVotes", maxVotes);
            addFloat(params, "minBlStars", minBlStars);
            addFloat(params, "minBpm", minBpm);
            addInt(params, "minDownVotes", minDownVotes);
            addInt(params, "minDuration", minDuration);
            addFloat(params, "minNps", minNps);
            addFloat(params, "minRating", minRating);
            addFloat(params, "minSsStars", minSsStars);
            addInt(params, "minUpVotes", minUpVotes);
            addInt(params, "minVotes", minVotes);
            addBool(params, "noodle", noodle);
            if (order != null && order != OrderSort.None) {
                addParam(params, "order", order.val());
            }
            addParam(params, "tags", tags);
            addDate(params, "to", to);
            addBool(params, "verified", verified);
            addBool(params, "vivify", vivify);

            if (!params.isEmpty()) {
                url.append("?").append(String.join("&", params));
            }

            return url.toString();
        }

        private void addParam(ArrayList<String> params, String key, String value) {
            if (value != null && !value.isEmpty()) {
                params.add(key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }

        private void addBool(ArrayList<String> params, String key, Boolean value) {
            if (value != null) {
                params.add(key + "=" + value.toString());
            }
        }

        private void addInt(ArrayList<String> params, String key, Integer value) {
            if (value != null) {
                params.add(key + "=" + value);
            }
        }

        private void addFloat(ArrayList<String> params, String key, Float value) {
            if (value != null) {
                params.add(key + "=" + value);
            }
        }

        private void addDate(ArrayList<String> params, String key, Instant value) {
            if (value != null) {
                params.add(key + "=" + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
            }
        }

        private void addList(ArrayList<String> params, String key, ArrayList<String> list) {
            if (list != null && !list.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (String item : list) {
                    if (item != null && !item.isEmpty()) {
                        joiner.add(item);
                    }
                }
                if (joiner.length() > 0) {
                    params.add(key + "=" + URLEncoder.encode(joiner.toString(), StandardCharsets.UTF_8));
                }
            }
        }
    }

    public static final SearchQueryBuilder queryBuilder = new SearchQueryBuilder();

    public static void pageLeft(Runnable after) {
        page = Math.max(page-1, 0);
        loadFromSearch(after);
    }

    public static void pageRight(Runnable after) {
        page++;
        loadFromSearch(after);
    }

    public static void downloadSong(SongPreview preview, String runDirectory, Runnable after) {
        CompletableFuture.runAsync(() -> {
            _downloadSong(preview, runDirectory);
        }).thenRun(after);
    }

    /// passes the path to the song folder to `after` if the download was successful
    public static void downloadFromId(String id, String runDirectory, Runnable after) {
        CompletableFuture.runAsync(() -> _downloadFromId(id, runDirectory, after));
    }

    private static void _downloadFromId(String id, String runDirectory, Runnable after) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MAP_ID_URL + id))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                Beatcraft.LOGGER.error("Failed to download song '{}'", id);
                return;
            }

            String rawJson = response.body();
            JsonObject responseJson = JsonParser.parseString(rawJson).getAsJsonObject();

            SongPreview preview = SongPreview.loadJson(responseJson);

            _downloadSong(preview, runDirectory);

            String songFolder = runDirectory + "/beatmaps/" + preview.id() + " (" + filterString(preview.metaData().songName() + " - " + preview.metaData().levelAuthorName()) + ")";

            if (new File(songFolder).exists()) {
                after.run();
            } else {
                Beatcraft.LOGGER.error("Something went wrong with processing downloaded song!");
            }

        } catch (IOException | InterruptedException e) {
            Beatcraft.LOGGER.error("Failed to download song '{}'", id, e);
            Thread.currentThread().interrupt();
        }
    }

    public static String filterString(String in) {
        String replaced = in.replaceAll("[^a-zA-Z0-9._\\-+()\\[\\]' ]", "_");

        if (replaced.length() > 150) {
            replaced = replaced.substring(0, 100);
        }
        return replaced;
    }

    private static void _downloadSong(SongPreview preview, String runDirectory) {
        String url = preview.versions().getFirst().downloadURL();
        String unzipTo = runDirectory + "/beatmaps/" + preview.id() + " (" + filterString(preview.metaData().songName() + " - " + preview.metaData().levelAuthorName()) + ")";
        String path = unzipTo + ".zip";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200 || response.body() == null) {
                Beatcraft.LOGGER.error("Failed to download song: {}", response.statusCode());
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                outputStream.write(response.body());
            }

            unzip(path, unzipTo);

        } catch (IOException | InterruptedException e) {
            Beatcraft.LOGGER.error("Failed to download song!", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void unzip(String source, String destination) {

        try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(source))) {

            ZipEntry zipEntry = inputStream.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = zipEntry.getName().endsWith(File.separator);

                Path newPath = zipSlipProtect(zipEntry, Path.of(destination));

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    Files.copy(inputStream, newPath, StandardCopyOption.REPLACE_EXISTING);

                }

                zipEntry = inputStream.getNextEntry();

            }
            inputStream.closeEntry();

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to unzip song!", e);
        }
        try {
            Files.deleteIfExists(Path.of(source));

            convertAllToPng(destination);

        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to remove temporary zip!", e);
        }
    }

    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }

    private static int currentRequestId = 0;
    public static void loadFromSearch(Runnable after) {
        final int id = ++currentRequestId;
        if (loadRequest != null) {
            loadRequest.cancel(false);
        }
        loadRequest = CompletableFuture
            .runAsync(SongDownloader::_loadFromSearch)
            .thenRun(() -> {
                if (id == currentRequestId) {
                    after.run();
                    loadRequest = null;
                }
            });
    }

    public static final Lock listModifyLock = new ReentrantLock();

    private static void _loadFromSearch() {
        if (queryBuilder.q.isEmpty()) {
            loadLatest();
            return;
        }

        ArrayList<SongPreview> localPreviews = new ArrayList<>();

        String searchQuery = queryBuilder.buildUrl();

        HttpRequest searchRequest = HttpRequest.newBuilder()
            .uri(URI.create(searchQuery))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(searchRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                Beatcraft.LOGGER.error("Failed to find songs from BeatSaver!");
                return;
            }

            String rawJson = response.body();
            JsonObject responseJson = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonArray docs = responseJson.getAsJsonArray("docs");

            docs.forEach(rawSongJson -> {
                JsonObject songJson = rawSongJson.getAsJsonObject();
                SongPreview preview = SongPreview.loadJson(songJson);
                localPreviews.add(preview);
            });

            listModifyLock.lock();
            try {
                songPreviews.clear();
                songPreviews.addAll(localPreviews);
            } finally {
                listModifyLock.unlock();
            }

        } catch (IOException | InterruptedException e) {
            Beatcraft.LOGGER.error("Failed to connect to BeatSaver!", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void loadLatest() {

        songPreviews.clear();
        page = 0;

        String listQuery = SEARCH_LATEST_URL + "?pageSize=" + PAGE_SIZE;

        if (before != null) {
            listQuery += "&before=" + before;
        }

        if (automapper.valLatest() != null) {
            listQuery += "&automapper=" + automapper.valLatest();
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(listQuery))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                Beatcraft.LOGGER.error("Failed to fetch songs from BeatSaver!");
                return;
            }

            String rawBody = response.body();
            JsonObject responseJson = JsonParser.parseString(rawBody).getAsJsonObject();
            JsonArray docs = responseJson.getAsJsonArray("docs");

            docs.forEach(rawSongJson -> {
                JsonObject songJson = rawSongJson.getAsJsonObject();
                SongPreview preview = SongPreview.loadJson(songJson);
                songPreviews.add(preview);
            });

        } catch (IOException | InterruptedException e) {
            Beatcraft.LOGGER.error("Failed to connect to BeatSaver!", e);
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    public static void convertToPng(File inputFile, File outputFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("Invalid image file: " + inputFile.getAbsolutePath());
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage convertedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = convertedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();

        ImageIO.write(convertedImage, "png", outputFile);

        Files.deleteIfExists(inputFile.toPath());
    }

    public static void convertAllToPng(String folder) {
        File dir = new File(folder);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Invalid directory: " + folder);
            return;
        }

        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
        if (files == null) return;

        for (File file : files) {
            String outputFileName = file.getAbsolutePath().replaceAll("(?i)\\.jpe?g$", ".png");
            File outputFile = new File(outputFileName);

            try {
                convertToPng(file, outputFile);
            } catch (IOException e) {
                Beatcraft.LOGGER.error("Failed to convert image to png '{}'", file.getName(), e);
            }
        }
    }

}
