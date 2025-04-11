package com.beatcraft.replay;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.logic.GameLogicHandler;
import net.minecraft.client.MinecraftClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlayRecorder {


    public static String outputFile;
    public static String songID;
    public static String difficultySet;
    public static String difficulty;
    private static final ArrayList<PlayFrame> frames = new ArrayList<>();

    public static void update(float beat) {
        if (outputFile != null && BeatmapPlayer.isPlaying()) {
            frames.add(new PlayFrame(
                beat,
                new Vector3f(GameLogicHandler.leftSaberPos), new Quaternionf(GameLogicHandler.leftSaberRotation),
                new Vector3f(GameLogicHandler.rightSaberPos), new Quaternionf(GameLogicHandler.rightSaberRotation),
                new Vector3f(GameLogicHandler.headPos), new Quaternionf(GameLogicHandler.headRot)
            ));
        }
    }

    public static void seek(float beat) {
        frames.removeIf(frame -> frame.beat() >= beat);
    }

    public static void save() throws IOException {

        if (outputFile == null) return;

        String path = MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/beatcraft/replay/" + outputFile;

        //JsonObject json = new JsonObject();

        //json.addProperty("song", songName);
        //json.addProperty("set", difficultySet);
        //json.addProperty("diff", difficulty);


        var idBytes = songID.getBytes(StandardCharsets.UTF_8);
        var setBytes = difficultySet.getBytes(StandardCharsets.UTF_8);
        var diffBytes = difficulty.getBytes(StandardCharsets.UTF_8);

        var sizeID = idBytes.length;
        var sizeSet = setBytes.length;
        var sizeDiff = diffBytes.length;

        // storing an array of N play frames
        // each play frame is 1 float, 3 vector3fs, and 3 quaternionfs
        var outputFrames = ByteBuffer.allocate(sizeID + sizeSet + sizeDiff + ((Float.BYTES * ((3 * 4) + (3 * 3) + 1)) * frames.size()) + (Integer.BYTES * 5));

        outputFrames.putInt(ReplayHandler.VERSION);

        outputFrames.putInt(sizeID);
        outputFrames.put(idBytes);

        outputFrames.putInt(sizeSet);
        outputFrames.put(setBytes);

        outputFrames.putInt(sizeDiff);
        outputFrames.put(diffBytes);

        outputFrames.putInt(frames.size());
        frames.forEach(frame -> frame.write(outputFrames));

        Files.write(Path.of(path), outputFrames.array());

        ReplayHandler.loadReplays();

    }

    public static void reset() {
        frames.clear();
        outputFile = null;
        songID = null;
        difficultySet = null;
        difficulty = null;
    }

}
