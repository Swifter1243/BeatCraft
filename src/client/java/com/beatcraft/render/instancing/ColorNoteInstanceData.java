package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.beatcraft.render.instancing.InstancedMesh.*;

public class ColorNoteInstanceData implements InstancedMesh.InstanceData {

    private final Matrix4f transform;
    private final Color color;
    private float dissolve;
    private int index;
    private final Vector4f slicePosition;

    private static final ArrayList<ColorNoteInstanceData> sharedCache = new ArrayList<>();

    private ColorNoteInstanceData(Matrix4f transform, Color color, float dissolve, int index, Vector4f slicePosition) {
        this.transform = new Matrix4f(transform);
        this.color = color;
        this.dissolve = dissolve;
        this.index = index;
        this.slicePosition = slicePosition;
    }

    public static ColorNoteInstanceData create(Matrix4f transform, Color color, float dissolve, int index, Vector4f slicePosition) {
        if (sharedCache.isEmpty()) {
            return new ColorNoteInstanceData(new Matrix4f(transform), new Color(color), dissolve, index, new Vector4f(slicePosition));
        } else {
            var x = sharedCache.removeLast();
            x.transform.set(transform);
            x.color.set(color);
            x.dissolve = dissolve;
            x.index = index;
            x.slicePosition.set(slicePosition);
            return x;
        }
    }

    @Override
    public ColorNoteInstanceData copy() {
        return ColorNoteInstanceData.create(transform, color, dissolve, index, slicePosition);
    }

    @Override
    public void free() {
        sharedCache.add(this);
    }

    private static final Vector4f ZERO = new Vector4f(0);
    public static ColorNoteInstanceData create(Matrix4f transform, Color color, float dissolve, int index) {
        return create(transform, color, dissolve, index, ZERO);
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

        buffer.put(color.getRed()).put(color.getGreen()).put(color.getBlue()).put(color.getAlpha());

        buffer.put(dissolve);
        buffer.put((float) index);
        buffer.put(0).put(0);

        buffer.put(slicePosition.x).put(slicePosition.y).put(slicePosition.z).put(slicePosition.w);

    }

    @Override
    public int getFrameSize() {
        return 16 + 4 + 4 + 4;
    }

    private static final int TRANSFORM_LOCATION = 3;
    private static final int COLOR_LOCATION = 7;
    private static final int DISSOLVE_INDEX_LOCATION = 8;
    private static final int SLICE_LOCATION = 9;

    @Override
    public int[] getLocations() {
        return new int[]{
            TRANSFORM_LOCATION, TRANSFORM_LOCATION + 1, TRANSFORM_LOCATION + 2, TRANSFORM_LOCATION + 3,
            COLOR_LOCATION,
            DISSOLVE_INDEX_LOCATION,
            SLICE_LOCATION
        };
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

        GL20.glVertexAttribPointer(DISSOLVE_INDEX_LOCATION, 2, GL11.GL_FLOAT, false,
            stride, MATRIX4F_SIZE_BYTES + VEC4_SIZE_BYTES);
        GL20.glEnableVertexAttribArray(DISSOLVE_INDEX_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(DISSOLVE_INDEX_LOCATION, 1);

        GL20.glVertexAttribPointer(SLICE_LOCATION, 4, GL11.GL_FLOAT, false,
            stride, MATRIX4F_SIZE_BYTES + VEC4_SIZE_BYTES * 2);
        GL20.glEnableVertexAttribArray(SLICE_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(SLICE_LOCATION, 1);

    }

    public void setup(int program) {}
    public void cleanup() {}

}
