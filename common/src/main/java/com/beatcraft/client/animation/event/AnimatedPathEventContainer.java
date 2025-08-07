package com.beatcraft.client.animation.event;


import com.beatcraft.client.animation.Animation;
import com.beatcraft.client.animation.AnimationPropertyContainer;
import com.beatcraft.client.animation.pointdefinition.PointDefinition;
import com.beatcraft.client.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.common.data.types.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AnimatedPathEventContainer extends AnimationPropertyContainer<AnimatedPathEvent<Float>, AnimatedPathEvent<Vector3f>, AnimatedPathEvent<Vector4f>, AnimatedPathEvent<Quaternionf>, AnimatedPathEvent<Color>> {
    private static <T> AnimatedPathEvent<T> propertyToEvent(PointDefinition<T> property, AssignPathAnimation assignPathAnimation) {
        if (property == null) {
            return null;
        } else {
            return property.toAnimatedPathEvent(assignPathAnimation);
        }
    }

    public AnimatedPathEventContainer(Animation animation, AssignPathAnimation assignPathAnimation) {
        offsetPosition = propertyToEvent(animation.getOffsetPosition(), assignPathAnimation);
        offsetWorldRotation = propertyToEvent(animation.getOffsetWorldRotation(), assignPathAnimation);
        localRotation = propertyToEvent(animation.getLocalRotation(), assignPathAnimation);
        localPosition = propertyToEvent(animation.getLocalPosition(), assignPathAnimation);
        definitePosition = propertyToEvent(animation.getDefinitePosition(), assignPathAnimation);
        position = propertyToEvent(animation.getPosition(), assignPathAnimation);
        rotation = propertyToEvent(animation.getRotation(), assignPathAnimation);
        scale = propertyToEvent(animation.getScale(), assignPathAnimation);
        dissolve = propertyToEvent(animation.getDissolve(), assignPathAnimation);
        dissolveArrow = propertyToEvent(animation.getDissolveArrow(), assignPathAnimation);
        interactable = propertyToEvent(animation.getInteractable(), assignPathAnimation);
        time = propertyToEvent(animation.getTime(), assignPathAnimation);
        color = propertyToEvent(animation.getColor(), assignPathAnimation);
    }
}
