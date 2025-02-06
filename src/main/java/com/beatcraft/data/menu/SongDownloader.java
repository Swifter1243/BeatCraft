package com.beatcraft.data.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.menu.song_preview.SongPreview;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

        public Boolean val() {
            return val;
        }
        public Boolean val2() {
            return val2;
        }
    }

    private static final OkHttpClient httpClient = new OkHttpClient();

    private static final String LATEST_URL = "https://api.beatsaver.com/maps/latest";
    private static final String SEARCH_URL = "https://api.beatsaver.com/search/v1/";

    private static final int PAGE_SIZE = 20;

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

    private static String filterString(String in) {
        String replaced = in.replaceAll("[^a-zA-Z0-9._\\-+()\\[\\]' ]", "_");

        if (replaced.length() > 150) {
            replaced = replaced.substring(0, 100);
        }
        return replaced;
    }

    private static void _downloadSong(SongPreview preview, String runDirectory) {
        String url = preview.versions().getFirst().downloadURL();

        String unzip_to = runDirectory + "/beatmaps/" + preview.id() + " (" + filterString(preview.metaData().songName() + " - " + preview.metaData().levelAuthorName()) + ")";
        String path = unzip_to + ".zip";

        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                BeatCraft.LOGGER.error("Failed to download song: {}", response);
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                outputStream.write(response.body().bytes());
            }

            unzip(path, unzip_to);

        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to download song!", e);
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
            BeatCraft.LOGGER.error("Failed to unzip song!", e);
        }
        try {
            Files.deleteIfExists(Path.of(source));
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to remove temporary zip!", e);
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
        if (search.isEmpty()) {
            loadLatest();
            return;
        }

        ArrayList<SongPreview> localPreviews = new ArrayList<>();

        String searchQuery = SEARCH_URL + page +
            "?q=" + URLEncoder.encode(search, StandardCharsets.UTF_8) +
            "&leaderboard=" + leaderBoardSort.val()
            ;

        if (order.val() != null) {
            searchQuery += "&order=" + order.val();
        }

        if (noodle != null) {
            searchQuery += "&noodle=" + noodle;
        }

        if (chroma != null) {
            searchQuery += "&chroma=" + chroma;
        }

        if (verified != null) {
            searchQuery += "&verified=" + verified;
        }

        Request searchRequest = new Request.Builder().url(searchQuery).build();

        try (Response response = httpClient.newCall(searchRequest).execute()) {
            if (!response.isSuccessful()) {
                BeatCraft.LOGGER.error("Failed to find songs from BeatSaver!");
                return;
            }
            if (response.body() == null) {
                BeatCraft.LOGGER.error("Failed to find songs from BeatSaver!");
                return;
            }

            String rawBody = response.body().string();

            JsonObject responseJson = JsonParser.parseString(rawBody).getAsJsonObject();

            JsonArray docs = responseJson.getAsJsonArray("docs");

            docs.forEach(rawSongJson -> {
                JsonObject songJson = rawSongJson.getAsJsonObject();
                SongPreview preview = SongPreview.loadJson(songJson);
                localPreviews.add(preview);
            });

            listModifyLock.lock();
            songPreviews.clear();
            songPreviews.addAll(localPreviews);
            listModifyLock.unlock();

        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to connect to beatsaver!", e);
        }

    }

    private static void loadLatest() {

        songPreviews.clear();
        page = 0;

        String listQuery = LATEST_URL + "?pageSize=" + PAGE_SIZE;

        if (before != null) {
            listQuery += "&before=" + before;
        }

        if (automapper.val2() != null) {
            listQuery += "&automapper=" + automapper.val2();
        }

        Request listRequest = new Request.Builder().url(listQuery).build();

        try (Response response = httpClient.newCall(listRequest).execute()) {
            if (!response.isSuccessful()) {
                BeatCraft.LOGGER.error("Failed to fetch songs from BeatSaver!");
                return;
            }
            if (response.body() == null) {
                BeatCraft.LOGGER.error("Failed to fetch songs from BeatSaver!");
                return;
            }

            String rawBody = response.body().string();

            JsonObject responseJson = JsonParser.parseString(rawBody).getAsJsonObject();

            JsonArray docs = responseJson.getAsJsonArray("docs");

            docs.forEach(rawSongJson -> {
                JsonObject songJson = rawSongJson.getAsJsonObject();
                SongPreview preview = SongPreview.loadJson(songJson);
                songPreviews.add(preview);
            });

        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to connect to beatsaver!", e);
        }

    }



}
