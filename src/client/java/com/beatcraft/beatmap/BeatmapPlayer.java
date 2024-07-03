package com.beatcraft.beatmap;

import net.minecraft.client.MinecraftClient;

public class BeatmapPlayer {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isPlaying = false;

    public static float bpm = 60;
    public static float beat = 0;

    public static void play() {
        play(beat);
    }
    public static void play(float beat) {
        BeatmapPlayer.beat = beat;
        isPlaying = true;
    }

    public static void pause() {
        isPlaying = false;
    }

    public static void restart() {
        play(0);
    }

    public static void onFrame() {
        if (isPlaying) {
            progressSong();
        }
    }

    public static void progressSong() {
        float deltaTime = 1.0f / (float)mc.getCurrentFps();
        float secondsToBeat = bpm / 60;
        beat += deltaTime * secondsToBeat;
    }
}
