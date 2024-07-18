package com.beatcraft.animation;

import com.beatcraft.animation.event.Path;
import com.beatcraft.animation.track.AnimatedPath;
import com.beatcraft.utils.MathUtil;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PathState extends AnimationPropertyContainer<Path<Float>, Path<Vector3f>, Path<Vector4f>, Path<Quaternionf>> {
    public void seekFromPath(float beat, AnimatedPath path) {
        offsetPosition = path.offsetPosition.seek(beat);
        offsetWorldRotation = path.offsetWorldRotation.seek(beat);
        localRotation = path.localRotation.seek(beat);
        localPosition = path.localPosition.seek(beat);
        definitePosition = path.definitePosition.seek(beat);
        position = path.position.seek(beat);
        rotation = path.rotation.seek(beat);
        scale = path.scale.seek(beat);
        dissolve = path.dissolve.seek(beat);
        dissolveArrow = path.dissolveArrow.seek(beat);
        interactable = path.interactable.seek(beat);
        time = path.time.seek(beat);
        color = path.color.seek(beat);
    }

    public void updateFromPath(float beat, AnimatedPath path) {
        offsetPosition = path.offsetPosition.update(beat);
        offsetWorldRotation = path.offsetWorldRotation.update(beat);
        localRotation = path.localRotation.update(beat);
        localPosition = path.localPosition.update(beat);
        definitePosition = path.definitePosition.update(beat);
        position = path.position.update(beat);
        rotation = path.rotation.update(beat);
        scale = path.scale.update(beat);
        dissolve = path.dissolve.update(beat);
        dissolveArrow = path.dissolveArrow.update(beat);
        interactable = path.interactable.update(beat);
        time = path.time.update(beat);
        color = path.color.update(beat);
    }

    public AnimationState interpolate(float t) {
        AnimationState state = new AnimationState();

        state.offsetPosition = interpolatePath(offsetPosition, t, MathUtil::lerpVector3);
        state.offsetWorldRotation = interpolatePath(offsetWorldRotation, t, MathUtil::lerpQuaternion);
        state.localRotation = interpolatePath(localRotation, t, MathUtil::lerpQuaternion);
        state.localPosition = interpolatePath(localPosition, t, MathUtil::lerpVector3);
        state.definitePosition = interpolatePath(definitePosition, t, MathUtil::lerpVector3);
        state.position = interpolatePath(position, t, MathUtil::lerpVector3);
        state.rotation = interpolatePath(rotation, t, MathUtil::lerpQuaternion);
        state.scale = interpolatePath(scale, t, MathUtil::lerpVector3);
        state.dissolve = interpolatePath(dissolve, t, Math::lerp);
        state.dissolveArrow = interpolatePath(dissolveArrow, t, Math::lerp);
        state.interactable = interpolatePath(interactable, t, Math::lerp);
        state.time = interpolatePath(time, t, Math::lerp);
        state.color = interpolatePath(color, t, MathUtil::lerpVector4);

        return state;
    }

    private static <T> T interpolatePath(Path<T> path, float time, Path.Interpolation<T> interpolation) {
        if (path == null) {
            return null;
        } else {
            return path.interpolate(time, interpolation);
        }
    }
}
