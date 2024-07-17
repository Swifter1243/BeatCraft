package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationState;
import com.google.gson.JsonElement;

import java.util.ArrayList;

public class ObjectTrackContainer {
    private final ArrayList<Track> tracks;

    public ObjectTrackContainer(JsonElement trackElement, TrackLibrary trackLibrary) {
        tracks = Track.getTracksAsList(trackElement, trackLibrary);
    }
    public ObjectTrackContainer() {
        tracks = new ArrayList<>();
    }

    private AnimationState getAnimationPropertyState() {
        if (tracks.size() == 0) {
            return new AnimationState();
        } else if (tracks.size() == 1) {
            return tracks.get(0).getAnimationProperties().getCurrentState();
        } else {
            AnimationState state = tracks.get(0).getAnimationProperties().getCurrentState();
            for (int i = 1; i < tracks.size(); i++) {
                state = AnimationState.combine(state, tracks.get(1).getAnimationProperties().getCurrentState());
            }
            return state;
        }
    }

    public AnimationState getAnimationState() {
        return getAnimationPropertyState();
    }
}
