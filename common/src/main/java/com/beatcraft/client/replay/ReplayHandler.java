package com.beatcraft.client.replay;


import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.map.SongDownloader;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReplayHandler {

    private final BeatmapController controller;

    public static final int VERSION = 1;

    private static boolean recordPlayback = false;

    private static final ArrayList<ReplayInfo> replayData = new ArrayList<>();

    public ReplayHandler(BeatmapController controller) {
        this.controller = controller;
    }

    public static int getReplayCount() {
        return replayData.size();
    }

    public static List<ReplayInfo> getReplays(int startIndex, int length) {
        ArrayList<ReplayInfo> out = new ArrayList<>();
        for (int i = startIndex; i < startIndex+length-1; i++) {
            if (i < replayData.size()) {
                out.add(replayData.get(i));
            }
        }
        return out;
    }

    public static void loadReplays() {
        loadReplays(true);
    }

    public static void loadReplays(boolean updateReplayScreen) {
        String replayFolder = Minecraft.getInstance().gameDirectory.toPath() + "/beatcraft/replay/";

        File folder = new File(replayFolder);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Beatcraft.LOGGER.error("Failed to create replay folder");
                return;
            }
        }

        File[] replayFiles = folder.listFiles(File::isFile);

        if (replayFiles == null) {
            Beatcraft.LOGGER.error("Failed to load replays");
            return;
        }

        replayData.clear();

        for (File replay : replayFiles) {
            //BeatCraft.LOGGER.info("Found potential replay file: '{}'", replay.getAbsolutePath());
            var fullPath = replay.getAbsolutePath();

            if (!fullPath.endsWith(".replay")) continue;

            var info = loadReplay(fullPath);
            if (info != null) replayData.add(info);
        }

        if (updateReplayScreen) {
            for (var controller : BeatmapManager.beatmaps) {
                controller.hudRenderer.modifierMenuPanel.setupReplayPage();
            }
        }
    }

    public void recordNextMap() {
        recordPlayback = true;
        controller.hudRenderer.modifierMenuPanel.setReplayToggleState(true);
    }

    public void cancelRecording() {
        recordPlayback = false;
        controller.playRecorder.outputFile = null;

        controller.hudRenderer.modifierMenuPanel.setReplayToggleState(false);
    }

    public boolean isRecording() {
        return recordPlayback;
    }

    private static String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.now().format(formatter);
    }

    public void setup(String mapID, String set, String diff) {
        if (mapID == null) {
            Beatcraft.LOGGER.warn("Map has no ID and therefore won't be able to be played back properly, so recording is canceled");
            return;
        }

        controller.playRecorder.outputFile = String.format("beatcraft-%s-%s-%s-%s.replay", mapID, SongDownloader.filterString(set), SongDownloader.filterString(diff), getCurrentTimestamp());
        controller.playRecorder.songID = mapID;
        controller.playRecorder.difficultySet = set;
        controller.playRecorder.difficulty = diff;

    }

    public static ReplayInfo loadReplay(String path) {
        try (RandomAccessFile file = new RandomAccessFile(path, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(512); // Enough for metadata
            channel.read(buffer);
            buffer.flip();

            int replayVersion = buffer.getInt();

            if (replayVersion == 1) {
                return loadV1(buffer, path);
            } else {
                return null;
            }

        } catch (Exception e) {
            Beatcraft.LOGGER.error("Error loading replay file '{}'", path, e);
            return null;
        }
    }

    private static ReplayInfo loadV1(ByteBuffer buffer, String path) {

        int idSize = buffer.getInt();
        byte[] idBytes = new byte[idSize];
        buffer.get(idBytes);
        String mapID = new String(idBytes, StandardCharsets.UTF_8);

        int setSize = buffer.getInt();
        byte[] setBytes = new byte[setSize];
        buffer.get(setBytes);
        String set = new String(setBytes, StandardCharsets.UTF_8);

        int diffSize = buffer.getInt();
        byte[] diffBytes = new byte[diffSize];
        buffer.get(diffBytes);
        String diff = new String(diffBytes, StandardCharsets.UTF_8);

        String name = lookup(mapID);

        Beatcraft.LOGGER.info("Loaded replay: {}, {}, {}, {}", mapID, name, set, diff);
        return new ReplayInfo(mapID, name == null ? "MAP NOT FOUND" : name, set, diff, path, name != null);

    }

    private static String lookup(String id) {
        var song = BeatcraftClient.songs.getById(id);
        if (song == null) return null;
        return String.format(
            "%s | %s",
            song.getTitle(),
            song.getMappers()
        );
    }


    public static void delete(ReplayInfo info) {
        try {
            Files.deleteIfExists(Path.of(info.replayFilePath()));
            loadReplays(false);
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to delete replay file!", e);
        }
    }

}