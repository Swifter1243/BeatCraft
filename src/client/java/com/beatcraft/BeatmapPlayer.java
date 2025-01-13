package com.beatcraft;

import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.beatmap.BeatmapLoader;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.logic.GameLogicHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

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
        //return MathUtil.secondsToBeats(getCurrentSeconds(), currentInfo.getBpm());
        return currentInfo.getBeat(getCurrentSeconds(), 1f);
    }

    public static float getCurrentSeconds() {
        return nanoToSeconds(elapsedNanoTime);
    }

    private static void setCurrentSeconds(float seconds) {
        elapsedNanoTime = secondsToNano(seconds);
    }

    private static void setCurrentBeat(float beat) {
        if (currentInfo == null) return;
        currentBeatmap.seek(beat);
        //float seconds = MathUtil.beatsToSeconds(beat, currentInfo.getBpm());
        float seconds = currentInfo.getTime(beat, 0);
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

    public static void onRender(MatrixStack matrices, Camera camera) {
        // Progress time
        long deltaNanoSeconds = getNanoDeltaTime();

        boolean shouldMapPlay = isPlaying && !mc.isPaused() && BeatmapAudioPlayer.isReady();
        if (shouldMapPlay) {
            elapsedNanoTime += (long) (deltaNanoSeconds * playbackSpeed);

            if (currentBeatmap != null) {
                currentBeatmap.update(getCurrentBeat());
            }
        }

        // Handle Audio
        BeatmapAudioPlayer.onFrame();

        // Render beatmap
        if (currentBeatmap != null) {
            currentBeatmap.render(matrices, camera);
            GameLogicHandler.update((double) deltaNanoSeconds / 1_000_000_000d);
        }
    }

    public static void setupDifficultyFromFile(String path) throws IOException {
        Path p = Paths.get(path);
        String infoPath = p.getParent().toString() + "/Info.dat";
        Info info = BeatmapLoader.getInfoFromFile(infoPath);
        currentBeatmap = BeatmapLoader.getDifficultyFromFile(path, info);
        currentInfo = info;
    }
}
