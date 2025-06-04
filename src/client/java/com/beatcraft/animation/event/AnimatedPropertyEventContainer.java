package com.beatcraft.animation.event;

import com.beatcraft.animation.Animation;
import com.beatcraft.animation.AnimationPropertyContainer;
import com.beatcraft.animation.pointdefinition.PointDefinition;
import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.data.types.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AnimatedPropertyEventContainer extends AnimationPropertyContainer<AnimatedPropertyEvent<Float>, AnimatedPropertyEvent<Vector3f>, AnimatedPropertyEvent<Vector4f>, AnimatedPropertyEvent<Quaternionf>, AnimatedPropertyEvent<Color>> {
    private static <T> AnimatedPropertyEvent<T> propertyToEvent(PointDefinition<T> property, AnimateTrack animateTrack) {
        if (property == null) {
            return null;
        } else {
            return property.toAnimatedPropertyEvent(animateTrack);
        }
    }

    public AnimatedPropertyEventContainer(Animation animation, AnimateTrack animateTrack) {
        offsetPosition = propertyToEvent(animation.getOffsetPosition(), animateTrack);
        offsetWorldRotation = propertyToEvent(animation.getOffsetWorldRotation(), animateTrack);
        localRotation = propertyToEvent(animation.getLocalRotation(), animateTrack);
        localPosition = propertyToEvent(animation.getLocalPosition(), animateTrack);
        definitePosition = propertyToEvent(animation.getDefinitePosition(), animateTrack);
        position = propertyToEvent(animation.getPosition(), animateTrack);
        rotation = propertyToEvent(animation.getRotation(), animateTrack);
        scale = propertyToEvent(animation.getScale(), animateTrack);
        dissolve = propertyToEvent(animation.getDissolve(), animateTrack);
        dissolveArrow = propertyToEvent(animation.getDissolveArrow(), animateTrack);
        interactable = propertyToEvent(animation.getInteractable(), animateTrack);
        time = propertyToEvent(animation.getTime(), animateTrack);
        color = propertyToEvent(animation.getColor(), animateTrack);
    }
}
