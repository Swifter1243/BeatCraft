package com.beatcraft.animation.track;

import com.beatcraft.animation.AnimationPropertyContainer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.event.AnimatedPropertyEvent;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.animation.event.AnimatedPropertyEventHandler;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class AnimatedProperties extends AnimationPropertyContainer<AnimatedPropertyEventHandler<Float>, AnimatedPropertyEventHandler<Vector3f>, AnimatedPropertyEventHandler<Vector4f>, AnimatedPropertyEventHandler<Quaternionf>> {
    private final AnimationState currentState = new AnimationState();

    public AnimatedProperties() {
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
        currentState.seekFromProperties(beat, this);
    }

    public void update(float beat) {
        currentState.updateFromProperties(beat, this);
    }

    public void loadAnimatedPropertyEvents(AnimatedPropertyEventContainer eventContainer) {
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
