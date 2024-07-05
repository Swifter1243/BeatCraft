package com.beatcraft.beatmap.data;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class Info {
    public float bpm;
    public HashMap<String, StyleSet> styleSets = new HashMap<>();

    public static Info from(JsonObject json) {
        Info info = new Info();

        info.bpm = json.get("_beatsPerMinute").getAsFloat();

        return info;
    }

    public static class StyleSet {
        public HashMap<String, SetDifficulty> difficulties = new HashMap<>();
    }

    public static class SetDifficulty {
        public float njs;
        public float offset;
        // also color palette shit goes here too, but I Do Not Care for now

        public static SetDifficulty from(JsonObject json) {
            SetDifficulty setDifficulty = new SetDifficulty();

            setDifficulty.njs = json.get("_noteJumpMovementSpeed").getAsFloat();
            setDifficulty.offset = json.get("_noteJumpStartBeatOffset").getAsFloat();

            return setDifficulty;
        }
    }
}
