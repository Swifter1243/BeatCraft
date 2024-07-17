package com.beatcraft.beatmap.data;

import com.beatcraft.animation.Animation;
import com.beatcraft.animation.Easing;
import com.beatcraft.animation.track.Track;
import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.function.Function;

public class AnimateTrack extends BeatmapObject {
    private ArrayList<Track> tracks;
    private float duration;
    private Function<Float, Float> easing;
    private Integer repeat;
    private final Animation animation = new Animation();

    @Override
    public AnimateTrack loadV2(JsonObject json, Difficulty difficulty) {
        super.loadV2(json, difficulty);

        JsonObject data = json.getAsJsonObject("_data");

        duration = JsonHelper.getFloat(data,"_duration", 0);

        if (data.has("_easing")) {
            easing = Easing.getEasing(data.get("_easing").getAsString());
        }

        JsonElement trackElement = data.get("_track");
        tracks = Track.getTracksAsList(trackElement, difficulty.getTrackLibrary());

        animation.loadV2(data, difficulty);

        return this;
    }

    @Override
    public AnimateTrack loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        JsonObject data = json.getAsJsonObject("d");

        duration = JsonHelper.getFloat(data,"duration", 0);

        if (data.has("repeat")) {
            repeat = data.get("repeat").getAsInt();
        }
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

    public float getRepeatedDuration() {
        return repeat != null ? duration * repeat : duration;
    }

    public Function<Float, Float> getEasing() {
        return easing;
    }

    public Integer getRepeat() {
        return repeat;
    }

    public Animation getAnimation() {
        return animation;
    }


    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public Animation.EventContainer toEventContainer() {
        return animation.toEventContainer(this);
    }
}
