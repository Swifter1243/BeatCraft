package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationState;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Optional;

public class ObjectTrackContainer {
    private final ArrayList<Track> tracks = new ArrayList<>();

    public void loadTracks(JsonElement trackElement, TrackLibrary trackLibrary) {
        tracks.addAll(Track.getTracksAsList(trackElement, trackLibrary));
    }


    public AnimationState getAnimatedPropertyState() {
        Optional<AnimationState> state = tracks.stream().map(track -> track.getAnimatedProperties().getCurrentState()).reduce(AnimationState::combine);
        return state.orElseGet(AnimationState::new);
    }
}
