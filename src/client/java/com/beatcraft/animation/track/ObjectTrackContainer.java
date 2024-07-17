package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationState;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Optional;

public class ObjectTrackContainer {
    private final ArrayList<Track> tracks;

    public ObjectTrackContainer(JsonElement trackElement, TrackLibrary trackLibrary) {
        tracks = Track.getTracksAsList(trackElement, trackLibrary);
    }
    public ObjectTrackContainer() {
        tracks = new ArrayList<>();
    }

    private AnimationState getAnimationPropertyState() {
        Optional<AnimationState> state = tracks.stream().map(track -> track.getAnimatedProperties().getCurrentState()).reduce(AnimationState::combine);
        return state.orElseGet(AnimationState::new);
    }

    public AnimationState getAnimationState() {
        return getAnimationPropertyState();
    }
}
