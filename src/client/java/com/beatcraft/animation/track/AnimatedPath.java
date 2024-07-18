package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationPropertyContainer;
import com.beatcraft.animation.PathState;
import com.beatcraft.animation.event.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class AnimatedPath extends AnimationPropertyContainer<AnimatedPathEventHandler<Float>, AnimatedPathEventHandler<Vector3f>, AnimatedPathEventHandler<Vector4f>, AnimatedPathEventHandler<Quaternionf>> {
    private final PathState currentState = new PathState();

    public AnimatedPath() {
        offsetPosition = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        offsetWorldRotation = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        localRotation = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        localPosition = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        definitePosition = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        position = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        rotation = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        scale = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        dissolve = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        dissolveArrow = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        interactable = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        time = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
        color = new AnimatedPathEventHandler<>(new ArrayList<>(), null);
    }

    public void seek(float beat) {
        currentState.seekFromPath(beat, this);
    }

    public void update(float beat) {
        currentState.updateFromPath(beat, this);
    }

    public void loadAnimatedPropertyEvents(AnimatedPathEventContainer eventContainer) {
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

    private <T> void loadEvent(AnimatedPathEventHandler<T> handler, AnimatedPathEvent<T> event) {
        if (event != null) {
            handler.getEvents().add(event);
        }
    }

    public PathState getCurrentState() {
        return currentState;
    }
}
