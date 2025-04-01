package com.beatcraft.replay;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.menu.ModifierMenuPanel;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReplayHandler {

    public static final int VERSION = 1;

    private static boolean recordPlayback = false;

    private static final ArrayList<ReplayInfo> replayData = new ArrayList<>();

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
        String replayFolder = MinecraftClient.getInstance().runDirectory.toPath() + "/beatcraft/replay/";

        File folder = new File(replayFolder);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                BeatCraft.LOGGER.error("Failed to create replay folder");
                return;
            }
        }

        File[] replayFiles = folder.listFiles(File::isFile);

        if (replayFiles == null) {
            BeatCraft.LOGGER.error("Failed to load replays");
            return;
        }

        replayData.clear();

        for (File replay : replayFiles) {
            BeatCraft.LOGGER.info("Found potential replay file: '{}'", replay.getAbsolutePath());
            var fullPath = replay.getAbsolutePath();

            if (!fullPath.endsWith(".replay")) continue;

            var info = loadReplay(fullPath);
            if (info != null) replayData.add(info);
        }


        HUDRenderer.modifierMenuPanel.setupReplayPage();
    }

    public static void recordNextMap() {
        recordPlayback = true;
    }

    public static void cancelRecording() {
        recordPlayback = false;
        PlayRecorder.outputFile = null;
    }

    public static boolean isRecording() {
        return recordPlayback;
    }

    private static String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.now().format(formatter);
    }

    public static void setup(String mapID, String set, String diff) {
        if (mapID == null) {
            BeatCraft.LOGGER.warn("Map has no ID and therefore won't be able to be played back properly, so recording is canceled");
            return;
        }

        PlayRecorder.outputFile = String.format("beatcraft-%s-%s-%s-%s.replay", mapID, SongDownloader.filterString(set), SongDownloader.filterString(diff), getCurrentTimestamp());
        PlayRecorder.songID = mapID;
        PlayRecorder.difficultySet = set;
        PlayRecorder.difficulty = diff;

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
            BeatCraft.LOGGER.error("Error loading replay file '{}'", path, e);
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

        BeatCraft.LOGGER.info("Loaded replay: {}, {}, {}, {}", mapID, name, set, diff);
        return new ReplayInfo(mapID, name == null ? "MAP NOT FOUND" : name, set, diff, path, name != null);

    }

    private static String lookup(String id) {
        var song = BeatCraftClient.songs.getById(id);
        if (song == null) return null;
        return String.format(
            "%s | %s",
            song.getTitle(),
            song.getMappers()
        );
    }

}
