package com.beatcraft.lightshow.lights;

import com.beatcraft.data.types.Color;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class LightObject {

    protected Vector3f position = new Vector3f();
    protected Quaternionf orientation = new Quaternionf();

    protected Vector3f offset = new Vector3f();
    protected Quaternionf rotation = new Quaternionf();

    protected Quaternionf worldRotation = new Quaternionf();

    protected LightState lightState = new LightState(new Color(0, 0, 0, 0), 0);

    protected CompoundTransformState transformState = new CompoundTransformState();

    protected Quaternionf mirrorQuaternion(boolean mirror, Quaternionf quat) {
        return mirror ? new Quaternionf(-quat.x, quat.y, -quat.z, quat.w) : quat;
    }

    protected Vector3f processVertex(Vector3f basePos, Vector3f cameraPos, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, boolean mirrorDraw) {
        return basePos.mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f())
            .rotate(mirrorQuaternion(mirrorDraw, orientation))
            .rotate(mirrorQuaternion(mirrorDraw, rotation))
            .add(position.mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()))
            .rotate(mirrorQuaternion(mirrorDraw, worldRotation))
            .add(offset.mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()))
            .sub(cameraPos);
    }

    public abstract LightObject cloneOffset(Vector3f offset);

    public abstract void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog);

    // Mainly used for light intensity, but also will work for spectrum visualizer elements
    public abstract void setBrightness(float value);

    public abstract void setColor(int color);

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setOrientation(Quaternionf orientation) {
        this.orientation = orientation;
    }

    public void setOffset(Vector3f offset) {
        this.offset = offset;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void setWorldRotation(Quaternionf rotation) {
        worldRotation = rotation;
    }

    public Quaternionf getWorldRotation() {
        return worldRotation;
    }

    public void addRotation(Quaternionf rotation) {
        this.rotation.mul(rotation);
    }

    public void setLightState(LightState state) {
        lightState.setColor(new Color(state.getColor()));
        lightState.setBrightness(state.getBrightness());
    }

    public void setTransformState(TransformState state) {

    }

    public LightState getLightState() {
        return lightState;
    }

    /// returns the absolute world-space position
    public Vector3f getPos() {
        return new Vector3f(position).rotate(rotation).add(offset);
    }

}
