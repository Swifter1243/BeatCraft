package com.beatcraft.beatmap.data.event;

import com.beatcraft.animation.track.Track;
import com.beatcraft.animation.track.TrackLibrary;
import com.beatcraft.event.VoidEventHandler;

import java.util.ArrayList;

public class AssignTrackParentHandler extends VoidEventHandler<AssignTrackParent> {
    private final TrackLibrary trackLibrary;

    @Override
    public void reset() {
        super.reset();
        if (trackLibrary != null) {
            trackLibrary.getTracks().forEach(Track::unparent);
        }
    }

    public AssignTrackParentHandler(ArrayList<AssignTrackParent> events, TrackLibrary trackLibrary) {
        super(events);
        this.trackLibrary = trackLibrary;
    }

    @Override
    public void onEventInterrupted(AssignTrackParent event, float normalTime) {
        // Can't happen
    }

    @Override
    public void onInsideEvent(AssignTrackParent event, float normalTime) {
        // Can't happen
    }

    @Override
    public void onEventPassed(AssignTrackParent event) {
        Track parent = event.getParentTrack();
        event.childrenTracks.forEach(child -> child.setParent(parent));
    }
}
