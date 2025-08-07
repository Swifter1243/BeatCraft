package com.beatcraft.client.animation.track;

import com.beatcraft.client.animation.AnimationState;
import com.google.gson.JsonElement;
import org.joml.Matrix4f;

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

    public AnimationState getAnimatedPathState(float time) {
        Optional<AnimationState> state = tracks.stream().map(track -> track.getAnimatedPath().getCurrentState().interpolate(time)).reduce(AnimationState::combine);
        return state.orElseGet(AnimationState::new);
    }

    public Matrix4f tryGetParentMatrix() {
        Optional<Track> parentTrack = tracks.stream().filter(Track::isParented).findFirst();
        return parentTrack.map(Track::tryGetParentMatrix).orElse(null);
    }
}