package com.beatcraft.render;

import com.beatcraft.beatmap.BeatmapLoader;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.math.GenericMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BeatmapPlayer {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public static Difficulty currentBeatmap = null;
    public static Info currentInfo = null;

    public static float currentBeat = 0;
    public static float playbackSpeed = 1;
    public static boolean isPlaying = false;

    public static void play() {
        isPlaying = true;
    }
    public static void play(float beat) {
        BeatmapPlayer.currentBeat = beat;
        isPlaying = true;
    }

    public static void pause() {
        isPlaying = false;
    }

    public static void restart() {
        play(0);
    }

    public static void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        // Progress time
        if (isPlaying && !mc.isPaused()) {
            float deltaTime = 1.0f / (float)mc.getCurrentFps();
            currentBeat += GenericMath.secondsToBeats(deltaTime, currentInfo.bpm) * playbackSpeed;
        }

        // Render beatmap
        if (currentBeatmap != null) {
            for (var obj : currentBeatmap.colorNotes) {
                obj.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
            }
        }
    }

    public static void setupDifficultyFromFile(String path) throws IOException {
        Path p = Paths.get(path);
        String infoPath = p.getParent().toString() + "/Info.dat";
        currentInfo = BeatmapLoader.getInfoFromFile(infoPath);
        currentBeatmap = BeatmapLoader.getDifficultyFromFile(path, currentInfo);
    }
}
