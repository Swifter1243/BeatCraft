package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.event.AnimatedPathEventContainer;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.google.gson.JsonElement;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Track {
    private final AnimatedProperties animatedProperties = new AnimatedProperties();
    private final AnimatedPath animatedPath = new AnimatedPath();
    private Track parent;

    public AnimatedProperties getAnimatedProperties() {
        return animatedProperties;
    }

    public AnimatedPath getAnimatedPath() {
        return animatedPath;
    }

    public static ArrayList<Track> getTracksAsList(JsonElement trackElement, TrackLibrary trackLibrary) {
        ArrayList<Track> tracks = new ArrayList<>();

        if (trackElement.isJsonArray()) {
            trackElement.getAsJsonArray().forEach(x -> addTrackToList(x, trackLibrary, tracks));
        } else {
            addTrackToList(trackElement, trackLibrary, tracks);
        }

        return tracks;
    }

    private static void addTrackToList(JsonElement nameElement, TrackLibrary trackLibrary, ArrayList<Track> tracks) {
        String name = nameElement.getAsString();
        Track track = trackLibrary.getOrCreateTrack(name);
        tracks.add(track);
    }

    public void loadAnimatedPropertyEvents(AnimatedPropertyEventContainer eventContainer) {
        animatedProperties.loadAnimatedPropertyEvents(eventContainer);
    }

    public void loadAnimatedPathEvents(AnimatedPathEventContainer eventContainer) {
        animatedPath.loadAnimatedPropertyEvents(eventContainer);
    }

    public void setParent(Track parent) {
        this.parent = parent;
    }

    public Track getParent() {
        return parent;
    }

    public boolean isParented() {
        return parent != null;
    }

    public void unparent() {
        this.parent = null;
    }

    public Matrix4f tryGetParentMatrix() {
        if (isParented()) {
            AnimationState animationState = parent.getAnimatedProperties().getCurrentState();
            Matrix4f parentMatrix = new Matrix4f();

            if (parent.isParented()) {
                Matrix4f parentParentMatrix = tryGetParentMatrix();
                if (parentParentMatrix != null) {
                    parentMatrix.mul(parentParentMatrix);
                }
            }

            Vector3f position = animationState.getPosition();
            if (position != null) {
                parentMatrix.translate(position);
            }

            Quaternionf rotation = animationState.getRotation();
            if (rotation != null) {
                parentMatrix.rotate(rotation);
            }

            Quaternionf localRotation = animationState.getLocalRotation();
            if (localRotation != null) {
                parentMatrix.rotate(localRotation);
            }

            Vector3f scale = animationState.getScale();
            if (scale != null) {
                parentMatrix.scale(scale);
            }

            return parentMatrix;
        } else {
            return null;
        }
    }
}
