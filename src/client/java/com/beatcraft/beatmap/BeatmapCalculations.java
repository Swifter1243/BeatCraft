package com.beatcraft.beatmap;

public class BeatmapCalculations {
    public static Jumps getJumps(float njs, float offset, float bpm) {
        float startHJD = 4;
        float maxHJD = 18;

        float num = 60 / bpm;
        float num2 = startHJD;
        while (njs * num * num2 > maxHJD) num2 /= 2;
        num2 += offset;
        if (num2 < 1) num2 = 1;

        float jumpDur = num * num2 * 2;
        float jumpDist = njs * jumpDur;
        jumpDist /= 0.6;

        return new Jumps(num2, jumpDist);
    }

    public record Jumps(float halfDuration, float jumpDistance) {}

    public static float secondsToBeats(float seconds, float bpm) {
        return seconds * (bpm / 60);
    }

    public static float beatsToSeconds(float beats, float bpm) {
        return beats * (60 / bpm);
    }
}
