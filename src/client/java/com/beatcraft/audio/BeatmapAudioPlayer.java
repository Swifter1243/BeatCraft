package com.beatcraft.audio;

import com.beatcraft.BeatmapPlayer;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class BeatmapAudioPlayer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static BeatmapAudio beatmapAudio = new BeatmapAudio();
    public static CompletableFuture<Void> loadRequest = null;

    public static void playAudioFromFile(String path) {
        unload();

        loadRequest = CompletableFuture.runAsync(() -> {
            try {
                beatmapAudio.loadAudioFromFile(path);
                beatmapAudio.seek(0); // seek auto-compensates for the player's latency setting
                beatmapAudio.play();
            } catch (IOException e) {
                throw new RuntimeException("Something FUCKED happened.", e);
            }
        });
    }

    private static void cancelLoad() {
        if (loadRequest != null) {
            loadRequest.cancel(true);
        }
    }

    public static void onFrame() {
        if (!beatmapAudio.isLoaded()) {
            return;
        }

        if (mc.isPaused() || !BeatmapPlayer.isPlaying()) {
            beatmapAudio.pause();
        } else {
            if (!beatmapAudio.isPlaying()) {
                syncTimeWithBeatmap();
                beatmapAudio.play();
            }
        }
    }

    public static void goToBeat(float beat) {
        //float time = MathUtil.beatsToSeconds(beat, BeatmapPlayer.currentInfo.getBpm());
        float time = BeatmapPlayer.currentInfo.getTime(beat, 1f);
        beatmapAudio.seek(time);
    }

    public static void goToSecond(float second) {
        beatmapAudio.seek(second);
    }

    public static void syncTimeWithBeatmap() {
        goToSecond(BeatmapPlayer.getCurrentSeconds());
    }

    public static boolean isReady() {
        // load request isn't active
        if (loadRequest == null) return false;

        // load request is done and worked
        return loadRequest.isDone() && !loadRequest.isCompletedExceptionally();
    }

    public static void unload() {
        cancelLoad();
        beatmapAudio.closeBuffer();
    }
}
