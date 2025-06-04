package com.beatcraft;

import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.base_providers.BaseProviderHandler;
import com.beatcraft.beatmap.BeatmapLoader;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.beatmap.data.ColorScheme;
import com.beatcraft.beatmap.data.SaberSyncedColor;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.networking.c2s.SongPauseC2SPayload;
import com.beatcraft.networking.c2s.SpeedSyncC2SPayload;
import com.beatcraft.render.HUDRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class BeatmapPlayer {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public static Difficulty currentBeatmap = null;
    public static Info currentInfo = null;

    private static ArrayList<Runnable> setupCalls = new ArrayList<>();

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

    public static void setCurrentBeat(float beat) {
        if (currentInfo == null) return;
        currentBeatmap.seek(beat);
        //float seconds = MathUtil.beatsToSeconds(beat, currentInfo.getBpm());
        float seconds = currentInfo.getTime(beat, 0);
        elapsedNanoTime = secondsToNano(seconds);
    }

    private static float nanoToSeconds(long nanoseconds) {
        return nanoseconds / 1_000_000_000F;
    }

    private static long secondsToNano(float seconds) {
        return (long)(seconds * 1_000_000_000);
    }

    private static void updateLastNanoTime() {
        lastNanoTime = System.nanoTime();
    }

    private static long getNanoDeltaTime() {
        long nanoDeltaTime = System.nanoTime() - lastNanoTime;
        updateLastNanoTime();

        // Prevent lag spikes that are too big.
        if (nanoDeltaTime > 1_000_000_000) {
            return 0;
        }

        return nanoDeltaTime;
    }

    public static float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public static void setPlaybackSpeed(float speed) {
        try {
            setPlaybackSpeed(speed, false);
        } catch (Exception e) {
            setupCalls.add(() -> setPlaybackSpeed(speed));
        }
    }

    public static void setPlaybackSpeed(float speed, boolean skipPacketSend) {
        BeatmapAudioPlayer.beatmapAudio.setPlaybackSpeed(speed);
        BeatmapAudioPlayer.syncTimeWithBeatmap();
        playbackSpeed = speed;
        if (!skipPacketSend) {
            ClientPlayNetworking.send(new SpeedSyncC2SPayload(speed));
        }
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
        pause(false);
    }

    public static void pause(boolean skipPacketSend) {
        isPlaying = false;
        if (!skipPacketSend) {
            ClientPlayNetworking.send(new SongPauseC2SPayload());
        }
    }

    public static void restart() {
        play(0);
    }

    public static void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        // Progress time
        long deltaNanoSeconds = getNanoDeltaTime();

        if (!setupCalls.isEmpty()) {
            for (var call : setupCalls) {
                call.run();
            }
            setupCalls.clear();
        }

        boolean shouldMapPlay = isPlaying && !mc.isPaused() && BeatmapAudioPlayer.isReady();
        if (shouldMapPlay) {
            elapsedNanoTime += (long) (deltaNanoSeconds * playbackSpeed);

            if (currentBeatmap != null) {
                currentBeatmap.update(getCurrentBeat(), (double) deltaNanoSeconds / 1_000_000_000d);
            }
        }

        // Handle Audio
        if (HUDRenderer.scene == HUDRenderer.MenuScene.InGame || HUDRenderer.scene == HUDRenderer.MenuScene.Paused) {
            BeatmapAudioPlayer.onFrame();
        }

        GameLogicHandler.preUpdate((double) deltaNanoSeconds / 1_000_000_000d, tickDelta);
        // Render beatmap
        if (currentBeatmap != null) {
            currentBeatmap.render(matrices, camera);
        }
        GameLogicHandler.update((double) deltaNanoSeconds / 1_000_000_000d, tickDelta);
    }

    public static void setupDifficultyFromFile(String path) throws IOException {
        Path p = Paths.get(path);
        String infoPath = p.getParent().toString() + "/Info.dat";
        Info info;
        try {
            info = BeatmapLoader.getInfoFromFile(infoPath);
        } catch (NoSuchFileException e) {
            infoPath = p.getParent().toString() + "/info.dat";
            info = BeatmapLoader.getInfoFromFile(infoPath);
        }
        currentBeatmap = BeatmapLoader.getDifficultyFromFile(path, info);
        currentInfo = info;
        var cs = currentBeatmap.getSetDifficulty().getColorScheme();
        SaberSyncedColor.leftColor = cs.getNoteLeftColor();
        SaberSyncedColor.rightColor = cs.getNoteRightColor();
        BaseProviderHandler.setupStaticProviders();
    }

    public static void reset() {
        currentInfo = null;
        currentBeatmap = null;
        isPlaying = false;
        elapsedNanoTime = 0;
        lastNanoTime = 0;
        SaberSyncedColor.leftColor = ColorScheme.getDefaultEnvironment().getNoteLeftColor();
        SaberSyncedColor.rightColor = ColorScheme.getDefaultEnvironment().getNoteRightColor();
    }

}
