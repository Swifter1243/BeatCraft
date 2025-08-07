package com.beatcraft.client.beatmap.data;

public record Jumps(float njs, float hjd, float jd) {
    public static Jumps getJumps(float njs, float offset, float bpm) {

        var diff = 60f / bpm;

        var jumpDistance = diff * njs;

        return new Jumps(njs, jumpDistance/2f, jumpDistance); // this needs to be calculated properly at some point...

    }
}
