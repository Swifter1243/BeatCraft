package com.beatcraft.replay;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.utils.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Replayer {

    private static final ArrayList<PlayFrame> frames = new ArrayList<>();
    public static boolean runReplay = false;

    public static void loadReplay(String replayFile) throws IOException {
        String path = MinecraftClient.getInstance().runDirectory + "/replay/" + replayFile;

        String rawData = Files.readString(Path.of(path));

        JsonObject json = JsonParser.parseString(rawData).getAsJsonObject();

        String song = json.get("song").getAsString();
        String set = json.get("set").getAsString();
        String diff = json.get("diff").getAsString();

        JsonArray frameData = json.getAsJsonArray("frames");

        frameData.forEach(data -> {
            JsonObject obj = data.getAsJsonObject();
            PlayFrame frame = PlayFrame.load(obj);
            frames.add(frame);
        });

        SongData data = BeatCraftClient.songs.getFiltered(song).getFirst();

        runReplay = true;
        SongData.BeatmapInfo info = data.getBeatMapInfo(set, diff);

        try {
            BeatmapPlayer.setupDifficultyFromFile(info.getBeatmapLocation().toString());
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void reset() {
        frames.clear();
        runReplay = false;
    }

    public static void update(float beat) {
        if (!runReplay || beat < 0) return;
        if (frames.isEmpty()) return;
        PlayFrame previous = null;
        PlayFrame next = null;

        int i = 0;
        while (true) {
            if (i >= frames.size()) {
                runReplay = false;
                return;
            }
            if (frames.get(i).beat() <= beat) {
                previous = frames.get(i);
                if (i+1 < frames.size()) {
                    next = frames.get(i + 1);
                } else {
                    next = null;
                }
            } else {
                break;
            }
            i++;
        }

        if (previous == null || next == null) {
            if (next == null) runReplay = false;
            return;
        }

        float sb = previous.beat();
        float eb = next.beat();

        float f = MathUtil.inverseLerp(sb, eb, beat);

        Vector3f leftSaberPos = MathUtil.lerpVector3(previous.leftSaberPosition(), next.leftSaberPosition(), f);
        Vector3f rightSaberPos = MathUtil.lerpVector3(previous.rightSaberPosition(), next.rightSaberPosition(), f);
        Quaternionf leftSaberRot = MathUtil.lerpQuaternion(previous.leftSaberRotation(), next.leftSaberRotation(), f);
        Quaternionf rightSaberRot = MathUtil.lerpQuaternion(previous.rightSaberRotation(), next.rightSaberRotation(), f);

        GameLogicHandler.updateRightSaber(rightSaberPos, rightSaberRot);
        GameLogicHandler.updateLeftSaber(leftSaberPos, leftSaberRot);

        Vector3f lep = new Vector3f(0, 1, 0).rotate(leftSaberRot).add(leftSaberPos);
        Vector3f rep = new Vector3f(0, 1, 0).rotate(rightSaberRot).add(rightSaberPos);

        DebugRenderer.renderLine(leftSaberPos, lep, 0xFFFF0000, 0xFFFF0000);
        DebugRenderer.renderLine(rightSaberPos, rep, 0xFF0000FF, 0xFF0000FF);

    }

    public static void seek(float beat) {

    }


}
