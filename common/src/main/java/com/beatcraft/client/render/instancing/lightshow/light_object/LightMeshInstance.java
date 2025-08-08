package com.beatcraft.client.render.instancing.lightshow.light_object;

import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.lightshow.lights.LightState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LightMeshInstance {

    private final LightMesh mesh;

    private static final int colorChannelCount = 8;

    public Matrix4f transform;

    private final LightState[] colorChannels = new LightState[colorChannelCount];

    public LightMeshInstance(LightMesh mesh) {
        for (int i = 0; i < colorChannelCount; i++) {
            colorChannels[i] = new LightState(new Color(), 0);
        }
        transform = new Matrix4f().identity();
        this.mesh = mesh;
    }

    public void setColor(int channel, LightState color) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0-7");
        }
        colorChannels[channel].set(color);
    }

    public LightState getLightState(int channel) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0-7");
        }
        return colorChannels[channel];
    }

    public void translate(Vector3f vec) {
        transform.translate(vec);
    }

    public void rotate(Quaternionf quat) {
        transform.rotate(quat);
    }

    public void scale(Vector3f scale) {
        transform.scale(scale);
    }

    public void scale(float x, float y, float z) {
        transform.scale(x, y, z);
    }

    public void setTransform(Matrix4f mat) {
        transform.set(mat);
    }

    public void draw() {
        mesh.draw(transform, colorChannels);
    }

}
