package com.beatcraft.replay;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ReplayHandler {

    public static final int VERSION = 1;

    private static boolean recordPlayback = false;

    private static final ArrayList<ReplayInfo> replayData = new ArrayList<>();

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
            var fullPath = replay.getAbsolutePath();

            if (!fullPath.endsWith(".replay")) continue;

            var info = loadReplay(fullPath);
            replayData.add(info);
        }

    }

    public static void recordNextMap() {
        recordPlayback = true;
    }

    public static void cancelRecording() {
        recordPlayback = false;
    }

    public static boolean isRecording() {
        return recordPlayback;
    }

    public static void setup(String mapID, String set, String diff) {
        if (mapID == null) {
            BeatCraft.LOGGER.warn("Map has no ID and therefore won't be able to be played back properly, so recording is canceled");
            return;
        }

        PlayRecorder.songID = mapID;
        PlayRecorder.difficultySet = set;
        PlayRecorder.difficulty = diff;

    }

    public static ReplayInfo loadReplay(String path) {
        try (RandomAccessFile file = new RandomAccessFile(path, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024); // Enough for metadata
            channel.read(buffer);
            buffer.flip();

            int replayVersion = buffer.getInt();

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

            return new ReplayInfo(mapID, name, set, diff, path);

        } catch (Exception e) {
            BeatCraft.LOGGER.error("Error loading replay file '{}'", path, e);
            return null;
        }
    }

    private static String lookup(String id) {
        var song = BeatCraftClient.songs.getById(id);
        return String.format(
            "%s | %s",
            song.getTitle(),
            song.getMappers()
        );
    }

}
