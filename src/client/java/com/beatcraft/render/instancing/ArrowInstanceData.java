package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

import static com.beatcraft.render.instancing.InstancedMesh.*;
import static com.beatcraft.render.instancing.InstancedMesh.VEC4_SIZE_BYTES;
import static com.beatcraft.render.instancing.InstancedMesh.MATRIX4F_SIZE_BYTES;

public class ArrowInstanceData implements InstancedMesh.InstanceData {
    private final Matrix4f transform;
    private final Color color;

    public ArrowInstanceData(Matrix4f transform, Color color) {
        this.transform = new Matrix4f(transform);
        this.color = color;
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    @Override
    public int getFrameSize() {
        return 16 + 4;
    }

    @Override
    public int[] getLocations() {
        return new int[]{
            TRANSFORM_LOCATION,
            COLOR_LOCATION
        };
    }

    @Override
    public void putData(FloatBuffer buffer) {
        buffer.put(transform.m00()).put(transform.m01()).put(transform.m02()).put(transform.m03());
        buffer.put(transform.m10()).put(transform.m11()).put(transform.m12()).put(transform.m13());
        buffer.put(transform.m20()).put(transform.m21()).put(transform.m22()).put(transform.m23());
        buffer.put(transform.m30()).put(transform.m31()).put(transform.m32()).put(transform.m33());

        var c = color;
        buffer.put(c.getRed()).put(c.getGreen()).put(c.getBlue()).put(c.getAlpha());

    }

    private static final int TRANSFORM_LOCATION = 3;
    private static final int COLOR_LOCATION = 7;

    @Override
    public void init() {

        for (int i = 0; i < 4; i++) {
            int location = TRANSFORM_LOCATION + i;
            GL20.glVertexAttribPointer(location, 4, GL11.GL_FLOAT, false,
                MATRIX4F_SIZE_BYTES + VEC4_SIZE_BYTES, i * 4 * FLOAT_SIZE_BYTES);
            GL20.glEnableVertexAttribArray(location);
            ARBInstancedArrays.glVertexAttribDivisorARB(location, 1);
        }

        GL20.glVertexAttribPointer(COLOR_LOCATION, 4, GL11.GL_FLOAT, false,
            MATRIX4F_SIZE_BYTES + VEC4_SIZE_BYTES, MATRIX4F_SIZE_BYTES);
        GL20.glEnableVertexAttribArray(COLOR_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(COLOR_LOCATION, 1);
    }

}
