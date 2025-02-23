package com.beatcraft.replay;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.logic.GameLogicHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.MinecraftClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlayRecorder {

    public static String outputFile;
    public static String songName;
    public static String difficultySet;
    public static String difficulty;
    private static final ArrayList<PlayFrame> frames = new ArrayList<>();

    public static void update(float beat) {
        if (outputFile != null && BeatmapPlayer.isPlaying()) {
            frames.add(new PlayFrame(
                beat,
                new Vector3f(GameLogicHandler.leftSaberPos), new Quaternionf(GameLogicHandler.leftSaberRotation),
                new Vector3f(GameLogicHandler.rightSaberPos), new Quaternionf(GameLogicHandler.rightSaberRotation)
            ));
        }
    }

    public static void seek(float beat) {
        frames.removeIf(frame -> frame.beat() >= beat);
    }

    public static void save() throws IOException {

        if (outputFile == null) return;

        String path = MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/beatcraft/replay/" + outputFile;

        JsonObject json = new JsonObject();

        json.addProperty("song", songName);
        json.addProperty("set", difficultySet);
        json.addProperty("diff", difficulty);

        JsonArray outputFrames = new JsonArray();

        frames.forEach(frame -> frame.write(outputFrames));

        json.add("frames", outputFrames);

        Files.writeString(Path.of(path), json.toString());
    }

    public static void reset() {
        frames.clear();
        outputFile = null;
        songName = null;
        difficultySet = null;
        difficulty = null;
    }

}
