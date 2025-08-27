package com.beatcraft.client.beatmap.data;

public record Jumps(float njs, float halfDuration, float jumpDistance) {
    public static Jumps getJumps(float njs, float offset, float bpm) {

        // v * t = d

        var hjd = 4f;
        var num = 60 / bpm;

        while (njs * num * hjd > 17.999f) {
            hjd /= 2;
        }

        hjd += offset;

        if (hjd < 0.25f) {
            hjd = 0.25f;
        }
        var jd = hjd * num * njs * 2;


        return new Jumps(njs, hjd, jd); // this needs to be calculated properly at some point...

    }
}
