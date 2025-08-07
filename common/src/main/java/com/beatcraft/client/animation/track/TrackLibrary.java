package com.beatcraft.client.animation.track;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class TrackLibrary {
    private final HashMap<String, Track> trackMap = new HashMap<>();

    public Track getOrCreateTrack(String name) {
        if (trackMap.containsKey(name)) {
            return trackMap.get(name);
        } else {
            Track track = new Track();
            trackMap.put(name, track);
            return track;
        }
    }
    public HashMap<String, Track> getTrackMap() {
        return trackMap;
    }

    public Collection<Track> getTracks() {
        return trackMap.values();
    }

    public Set<String> getTrackNames() {
        return trackMap.keySet();
    }

    public void seek(float beat) {
        getTracks().forEach(track -> {
            track.getAnimatedProperties().seek(beat);
            track.getAnimatedPath().seek(beat);
        });
    }

    public void update(float beat) {
        getTracks().forEach(track -> {
            track.getAnimatedProperties().update(beat);
            track.getAnimatedPath().update(beat);
        });
    }
}