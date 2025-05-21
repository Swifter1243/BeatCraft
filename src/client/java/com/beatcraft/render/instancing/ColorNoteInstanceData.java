package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

import static com.beatcraft.render.instancing.InstancedMesh.*;

public class ColorNoteInstanceData implements InstancedMesh.InstanceData {

    private final Matrix4f transform;
    private final Color color;
    private final float dissolve;
    private final int index;

    public ColorNoteInstanceData(Matrix4f transform, Color color, float dissolve, int index) {
        this.transform = new Matrix4f(transform);
        this.color = color;
        this.dissolve = dissolve;
        this.index = index;
    }

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

        var c = color;
        buffer.put(c.getRed()).put(c.getGreen()).put(c.getBlue()).put(c.getAlpha());

        buffer.put(dissolve);
        buffer.put((float) index);

    }

    @Override
    public int getFrameSize() {
        return 16 + 4 + 2;
    }

    private static final int TRANSFORM_LOCATION = 3;
    private static final int COLOR_LOCATION = 7;
    private static final int DISSOLVE_INDEX_LOCATION = 8;

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

        GL20.glVertexAttribPointer(DISSOLVE_INDEX_LOCATION, 2, GL11.GL_FLOAT, false,
            stride, MATRIX4F_SIZE_BYTES + COLOR_SIZE_BYTES);
        GL20.glEnableVertexAttribArray(DISSOLVE_INDEX_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(DISSOLVE_INDEX_LOCATION, 1);


    }
}
