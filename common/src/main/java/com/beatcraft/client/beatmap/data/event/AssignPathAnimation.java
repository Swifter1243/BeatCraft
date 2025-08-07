package com.beatcraft.client.beatmap.data.event;


import com.beatcraft.client.animation.Animation;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.animation.event.AnimatedPathEventContainer;
import com.beatcraft.client.animation.track.Track;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.function.Function;

public class AssignPathAnimation extends BeatmapObject {
    private ArrayList<Track> tracks;
    private float duration;
    private Function<Float, Float> easing;
    private final Animation animation = new Animation();

    @Override
    public AssignPathAnimation loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        JsonObject data = json.getAsJsonObject("_data");

        duration = GsonHelper.getAsFloat(data,"_duration", 0);

        if (data.has("_easing")) {
            easing = Easing.getEasing(data.get("_easing").getAsString());
        }

        JsonElement trackElement = data.get("_track");
        tracks = Track.getTracksAsList(trackElement, difficulty.getTrackLibrary());

        animation.loadV2(data, difficulty);

        return this;
    }

    @Override
    public AssignPathAnimation loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        JsonObject data = json.getAsJsonObject("d");

        duration = GsonHelper.getAsFloat(data,"duration", 0);

        if (data.has("easing")) {
            easing = Easing.getEasing(data.get("easing").getAsString());
        }

        JsonElement trackElement = data.get("track");
        tracks = Track.getTracksAsList(trackElement, difficulty.getTrackLibrary());

        animation.loadV3(data, difficulty);

        return this;
    }

    public float getDuration() {
        return duration;
    }

    public Function<Float, Float> getEasing() {
        return easing;
    }

    public Animation getAnimation() {
        return animation;
    }


    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public AnimatedPathEventContainer toAnimatedPathEvents() {
        return animation.toAnimatedPathEvents(this);
    }
}