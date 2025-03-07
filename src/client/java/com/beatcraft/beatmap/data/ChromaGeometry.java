package com.beatcraft.beatmap.data;

import com.beatcraft.animation.track.Track;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class ChromaGeometry extends BeatmapObject{
    public enum GeometryType {
        Sphere,
        Capsule,
        Cylinder,
        Cube,
        Plane,
        Quad,
        Triangle

    }
    protected GeometryType type;
    protected ChromaMaterial material;
    protected JsonArray position;
    protected JsonArray rotation;
    protected JsonArray scale;
    protected Track track;

    @Override
    public ChromaGeometry loadV3(JsonObject json, Difficulty difficulty) {
        super.loadV3(json, difficulty);

        // geometry specific shi
        JsonObject geo = json.getAsJsonObject("geometry");
        type = GeometryType.valueOf(geo.get("type").getAsString());
        material = new ChromaMaterial().load(geo.get("material").getAsJsonObject());

        // chroma shit
        position = json.getAsJsonArray("position");
        rotation = json.getAsJsonArray("rotation");
        scale = json.getAsJsonArray("scale");
        track = difficulty.getTrackLibrary().getOrCreateTrack(json.get("track").getAsString());
        return this;
    }
    public GeometryType getType() {
        return type;
    }
    public ChromaMaterial getMaterial() {
        return material;
    }
    public JsonArray getPosition() {
        return position;
    }
    public JsonArray getRotation() {
        return rotation;
    }
    public JsonArray getScale() {
        return scale;
    }
    public Track getTrack() {
        return track;
    }
}
