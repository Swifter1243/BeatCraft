package com.beatcraft.render;

import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.beatmap.BeatmapLoader;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.utils.MathUtil;
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

    private static long lastNanoTime = 0;
    private static long elapsedNanoTime = 0;
    private static float playbackSpeed = 1;
    private static boolean isPlaying = false;

    public static float getCurrentBeat() {
        if (currentInfo == null) return 0;
        return MathUtil.secondsToBeats(getCurrentSeconds(), currentInfo.getBpm());
    }

    public static float getCurrentSeconds() {
        return nanoToSeconds(elapsedNanoTime);
    }

    private static void setCurrentSeconds(float seconds) {
        elapsedNanoTime = secondsToNano(seconds);
    }

    private static void setCurrentBeat(float beat) {
        if (currentInfo == null) return;
        float seconds = MathUtil.beatsToSeconds(beat, currentInfo.getBpm());
        elapsedNanoTime = secondsToNano(seconds);
    }

    private static float nanoToSeconds(long nanoseconds) {
        return nanoseconds / 1000000000F;
    }

    private static long secondsToNano(float seconds) {
        return (long)(seconds * 1000000000);
    }

    private static void updateLastNanoTime() {
        lastNanoTime = System.nanoTime();
    }

    private static long getNanoDeltaTime() {
        long nanoDeltaTime = System.nanoTime() - lastNanoTime;
        updateLastNanoTime();

        // Prevent lag spikes that are too big.
        if (nanoDeltaTime > 1000000000) {
            return 0;
        }

        return nanoDeltaTime;
    }

    public static float getPlaybackSpeed() {
        return playbackSpeed;
    }
    public static void setPlaybackSpeed(float speed) {
        BeatmapAudioPlayer.beatmapAudio.setPlaybackSpeed(speed);
        BeatmapAudioPlayer.syncTimeWithBeatmap();
        playbackSpeed = speed;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void play() {
        BeatmapAudioPlayer.syncTimeWithBeatmap();
        isPlaying = true;
    }
    public static void play(float beat) {
        setCurrentBeat(beat);
        BeatmapAudioPlayer.syncTimeWithBeatmap();
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
        long deltaNanoSeconds = getNanoDeltaTime();

        boolean shouldMapPlay = isPlaying && !mc.isPaused() && BeatmapAudioPlayer.ready();
        if (shouldMapPlay) {
            elapsedNanoTime += deltaNanoSeconds * playbackSpeed;
        }

        // Handle Audio
        BeatmapAudioPlayer.onFrame();

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
