package com.beatcraft.render.instancing.lightshow.light_object;

import com.beatcraft.data.types.Color;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LightMeshObject {

    private final LightMesh mesh;

    private static final int colorChannelCount = 8;

    public Matrix4f transform;

    private final Color[] colorChannels = new Color[colorChannelCount];

    public LightMeshObject(LightMesh mesh) {
        for (int i = 0; i < colorChannelCount; i++) {
            colorChannels[i] = new Color(0);
        }
        transform = new Matrix4f().identity();
        this.mesh = mesh;
    }

    public void setColor(int channel, Color color) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0-7");
        }
        colorChannels[channel].set(color);
    }

    public Color getColor(int channel) {
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

    public void render() {

    }

}
