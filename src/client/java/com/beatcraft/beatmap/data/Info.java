package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class Info {
    public float bpm;
    public HashMap<String, StyleSet> styleSets = new HashMap<>();

    public Info load(JsonObject json) {
        bpm = json.get("_beatsPerMinute").getAsFloat();

        return this;
    }

    public static class StyleSet {
        public HashMap<String, SetDifficulty> difficulties = new HashMap<>();
    }

    public static class SetDifficulty {
        public float njs;
        public float offset;
        // also color palette shit goes here too, but I Do Not Care for now

        public SetDifficulty load(JsonObject json) {
            njs = json.get("_noteJumpMovementSpeed").getAsFloat();
            offset = json.get("_noteJumpStartBeatOffset").getAsFloat();

            return this;
        }
    }
}
