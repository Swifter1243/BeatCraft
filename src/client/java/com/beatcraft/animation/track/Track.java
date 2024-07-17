package com.beatcraft.animation.track;

import com.beatcraft.animation.Animation;
import com.beatcraft.animation.AnimationPropertyContainer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.event.AnimatedPropertyEvent;
import com.beatcraft.event.AnimatedPropertyEventHandler;
import com.google.gson.JsonElement;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class Track {
    private final AnimationProperties animationProperties = new AnimationProperties();

    public AnimationProperties getAnimationProperties() {
        return animationProperties;
    }

    public static class AnimationProperties extends AnimationPropertyContainer<AnimatedPropertyEventHandler<Float>, AnimatedPropertyEventHandler<Vector3f>, AnimatedPropertyEventHandler<Vector4f>, AnimatedPropertyEventHandler<Quaternionf>> {
        private final AnimationState currentState = new AnimationState();

        AnimationProperties() {
            offsetPosition = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            offsetWorldRotation = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            localRotation = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            localPosition = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            definitePosition = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            position = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            rotation = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            scale = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            dissolve = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            dissolveArrow = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            interactable = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            time = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
            color = new AnimatedPropertyEventHandler<>(new ArrayList<>(), null);
        }

        public void seek(float beat) {
            currentState.applySeek(beat, this);
        }

        public void update(float beat) {
            currentState.applyUpdate(beat, this);
        }

        public void loadEventContainer(Animation.EventContainer eventContainer) {
            loadEvent(getOffsetPosition(), eventContainer.getOffsetPosition());
            loadEvent(getOffsetWorldRotation(), eventContainer.getOffsetWorldRotation());
            loadEvent(getLocalRotation(), eventContainer.getLocalRotation());
            loadEvent(getLocalPosition(), eventContainer.getLocalPosition());
            loadEvent(getDefinitePosition(), eventContainer.getDefinitePosition());
            loadEvent(getPosition(), eventContainer.getPosition());
            loadEvent(getRotation(), eventContainer.getRotation());
            loadEvent(getScale(), eventContainer.getScale());
            loadEvent(getDissolve(), eventContainer.getDissolve());
            loadEvent(getDissolveArrow(), eventContainer.getDissolveArrow());
            loadEvent(getInteractable(), eventContainer.getInteractable());
            loadEvent(getTime(), eventContainer.getTime());
            loadEvent(getColor(), eventContainer.getColor());
        }

        private <T> void loadEvent(AnimatedPropertyEventHandler<T> handler, AnimatedPropertyEvent<T> event) {
            if (event != null) {
                handler.getEvents().add(event);
            }
        }

        public AnimationState getCurrentState() {
            return currentState;
        }
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

    public void loadEventContainer(Animation.EventContainer eventContainer) {
        animationProperties.loadEventContainer(eventContainer);
    }
}
