package com.beatcraft.animation.track;

import com.beatcraft.animation.event.AnimatedPathEventContainer;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.google.gson.JsonElement;

import java.util.ArrayList;

public class Track {
    private final AnimatedProperties animatedProperties = new AnimatedProperties();
    private final AnimatedPath animatedPath = new AnimatedPath();

    public AnimatedProperties getAnimatedProperties() {
        return animatedProperties;
    }

    public AnimatedPath getAnimatedPath() {
        return animatedPath;
    }

    public static ArrayList<Track> getTracksAsList(JsonElement trackElement, TrackLibrary trackLibrary) {
        ArrayList<Track> tracks = new ArrayList<>();

        if (trackElement.isJsonArray()) {
            trackElement.getAsJsonArray().forEach(x -> addTrackToList(x, trackLibrary, tracks));
        } else {
            addTrackToList(trackElement, trackLibrary, tracks);
        }

        return tracks;
    }

    private static void addTrackToList(JsonElement nameElement, TrackLibrary trackLibrary, ArrayList<Track> tracks) {
        String name = nameElement.getAsString();
        Track track = trackLibrary.getOrCreateTrack(name);
        tracks.add(track);
    }

    public void loadAnimatedPropertyEvents(AnimatedPropertyEventContainer eventContainer) {
        animatedProperties.loadAnimatedPropertyEvents(eventContainer);
    }

    public void loadAnimatedPathEvents(AnimatedPathEventContainer eventContainer) {
        animatedPath.loadAnimatedPropertyEvents(eventContainer);
    }
}
