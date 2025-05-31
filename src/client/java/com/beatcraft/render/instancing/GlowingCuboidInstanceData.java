package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.beatcraft.render.instancing.InstancedMesh.FLOAT_SIZE_BYTES;
import static com.beatcraft.render.instancing.InstancedMesh.MATRIX4F_SIZE_BYTES;

public class GlowingCuboidInstanceData implements InstancedMesh.InstanceData {

    private static final ArrayList<GlowingCuboidInstanceData> sharedCache = new ArrayList<>();

    private Matrix4f transform;
    private Color color;

    private GlowingCuboidInstanceData(Matrix4f transform, Color color) {
        this.transform = new Matrix4f(transform);
        this.color = new Color(color);
    }

    private int TRANSFORM_LOCATION = 3;
    private int COLOR_LOCATION = 7;

    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    @Override
    public void putData(FloatBuffer buffer) {

        buffer.put(transform.m00()).put(transform.m01()).put(transform.m02()).put(transform.m03());
        buffer.put(transform.m10()).put(transform.m11()).put(transform.m12()).put(transform.m13());
        buffer.put(transform.m20()).put(transform.m21()).put(transform.m22()).put(transform.m23());
        buffer.put(transform.m30()).put(transform.m31()).put(transform.m32()).put(transform.m33());

        buffer.put(color.getRed()).put(color.getGreen()).put(color.getBlue()).put(color.getAlpha());

    }

    @Override
    public int getFrameSize() {
        return 16 + 4;
    }

    @Override
    public void init() {
        int stride = getFrameSize() * FLOAT_SIZE_BYTES;

        for (int i = 0; i < 4; i++) {
            int location = TRANSFORM_LOCATION + i;
            GL20.glVertexAttribPointer(location, 4, GL11.GL_FLOAT, false,
                stride, i * 4 * FLOAT_SIZE_BYTES);
            GL20.glEnableVertexAttribArray(location);
            ARBInstancedArrays.glVertexAttribDivisorARB(location, 1);
        }

        GL20.glVertexAttribPointer(COLOR_LOCATION, 4, GL11.GL_FLOAT, false,
            stride, MATRIX4F_SIZE_BYTES);
        GL20.glEnableVertexAttribArray(COLOR_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(COLOR_LOCATION, 1);
    }

    @Override
    public int[] getLocations() {
        return new int[]{
            TRANSFORM_LOCATION,
            COLOR_LOCATION
        };
    }

    @Override
    public void setup(int program) {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void free() {
        sharedCache.add(this);
    }

    @Override
    public InstancedMesh.InstanceData copy() {
        return new GlowingCuboidInstanceData(transform, color);
    }
}
