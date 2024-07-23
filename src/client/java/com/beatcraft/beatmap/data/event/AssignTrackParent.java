package com.beatcraft.beatmap.data.event;

import com.beatcraft.animation.track.Track;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.event.IEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public class AssignTrackParent extends BeatmapObject implements IEvent {
    protected ArrayList<Track> childrenTracks;
    protected Track parentTrack;
    protected boolean worldPositionStays;

    @Override
    public AssignTrackParent loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        JsonObject data = json.getAsJsonObject("_data");

        JsonElement childrenTracksElement = data.get("_childrenTracks");
        childrenTracks = Track.getTracksAsList(childrenTracksElement, difficulty.getTrackLibrary());
        parentTrack = difficulty.getTrackLibrary().getOrCreateTrack(data.get("_parentTrack").getAsString());
        worldPositionStays = JsonHelper.getBoolean(data, "_worldPositionStays", false);

        return this;
    }

    @Override
    public AssignTrackParent loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        JsonObject data = json.getAsJsonObject("d");

        JsonElement childrenTracksElement = data.get("childrenTracks");
        childrenTracks = Track.getTracksAsList(childrenTracksElement, difficulty.getTrackLibrary());
        parentTrack = difficulty.getTrackLibrary().getOrCreateTrack(data.get("parentTrack").getAsString());
        worldPositionStays = JsonHelper.getBoolean(data, "worldPositionStays", false);

        return this;
    }

    public ArrayList<Track> getChildrenTracks() {
        return childrenTracks;
    }

    public Track getParentTrack() {
        return parentTrack;
    }

    public boolean doesWorldPositionStay() {
        return worldPositionStays;
    }

    @Override
    public float getEventBeat() {
        return beat;
    }

    @Override
    public float getEventDuration() {
        return 0;
    }
}
