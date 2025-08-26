package com.beatcraft.client.beatmap.data;

public record Jumps(float njs, float halfDuration, float jumpDistance) {
    public static Jumps getJumps(float njs, float offset, float bpm) {

        // v * t = d

        var jumpDistance = 16;

        return new Jumps(njs, (njs/jumpDistance), jumpDistance + offset); // this needs to be calculated properly at some point...

    }
}
