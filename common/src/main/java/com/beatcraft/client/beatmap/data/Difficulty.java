package com.beatcraft.client.beatmap.data;

import com.beatcraft.client.animation.track.TrackLibrary;
import com.google.gson.JsonArray;

import java.util.HashMap;

public abstract class Difficulty {

    private final TrackLibrary trackLibrary = new TrackLibrary();

    public final HashMap<String, JsonArray> pointDefinitions = new HashMap<>();


    public TrackLibrary getTrackLibrary() {
        return trackLibrary;
    }

}
