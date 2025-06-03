package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.CompoundTransformState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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

    public static GlowingCuboidInstanceData create(
        boolean mirrorDraw,
        Hitbox dimensions, Quaternionf orientation, Quaternionf rotation, Quaternionf rotation2,
        CompoundTransformState transformState, Vector3f position, Quaternionf worldRotation,
        Vector3f offset, Vector3f cameraPos, Color color,
        CompoundTransformState.Swizzle translationSwizzle,
        CompoundTransformState.Polarity translationPolarity,
        CompoundTransformState.Swizzle rotationSwizzle,
        CompoundTransformState.Polarity rotationPolarity,
        TriFunction<Float, Float, Float, Quaternionf> memoryPooledSwizzledQuaternionFactory
    ) {
        if (sharedCache.isEmpty()) {

            var mat = new Matrix4f();
            applyTransform(
                mat, dimensions, mirrorDraw,
                orientation, rotation, rotation2,
                transformState, position, worldRotation,
                offset, cameraPos,
                translationSwizzle, translationPolarity,
                rotationSwizzle, rotationPolarity,
                memoryPooledSwizzledQuaternionFactory
            );

            return new GlowingCuboidInstanceData(mat, new Color(color));
        } else {
            var x = sharedCache.removeLast();
            applyTransform(
                x.transform, dimensions, mirrorDraw,
                orientation, rotation, rotation2,
                transformState, position, worldRotation,
                offset, cameraPos,
                translationSwizzle, translationPolarity,
                rotationSwizzle, rotationPolarity,
                memoryPooledSwizzledQuaternionFactory
            );
            x.color.set(color);
            return x;
        }
    }

    private static void mirrorQuat(boolean mirror, Quaternionf src, Quaternionf dest) {
        dest.set(
            mirror ? -src.x : src.x,
            src.y,
            mirror ? -src.z : src.z,
            src.w
        );
    }

    private static void applyTransform(
        Matrix4f mat, Hitbox dimensions, boolean mirrorDraw, Quaternionf ori,
        Quaternionf rot, Quaternionf rot2,
        CompoundTransformState transformState, Vector3f pos,
        Quaternionf worldRot, Vector3f offset, Vector3f cameraPos,
        CompoundTransformState.Swizzle translationSwizzle,
        CompoundTransformState.Polarity translationPolarity,
        CompoundTransformState.Swizzle rotationSwizzle,
        CompoundTransformState.Polarity rotationPolarity,
        TriFunction<Float, Float, Float, Quaternionf> memoryPooledSwizzledQuaternionFactory
    ) {

        mat.identity();
        mat.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        var translation = MemoryPool.newVector3f();
        transformState.getTranslation(translationSwizzle, translationPolarity, translation);

        mat.translate(
            translation.x,
            mirrorDraw ? -translation.y : translation.y,
            translation.z
        );
        MemoryPool.releaseSafe(translation);

        mat.translate(
            offset.x,
            mirrorDraw ? -offset.y : offset.y,
            offset.z
        );

        var mirrorWorld = MemoryPool.newQuaternionf();
        mirrorQuat(mirrorDraw, worldRot, mirrorWorld);
        mat.rotate(mirrorWorld);
        MemoryPool.releaseSafe(mirrorWorld);

        mat.translate(
            pos.x,
            mirrorDraw ? -pos.y : pos.y,
            pos.z
        );

        var q = MemoryPool.newQuaternionf();
        mirrorQuat(mirrorDraw, rot2, q);
        mat.rotate(q);

        mirrorQuat(mirrorDraw, rot, q);
        mat.rotate(q);

        var q2 = transformState.getOrientation(rotationSwizzle, rotationPolarity, memoryPooledSwizzledQuaternionFactory);
        mirrorQuat(mirrorDraw, q2, q);
        MemoryPool.releaseSafe(q2);
        mat.rotate(q);

        mirrorQuat(mirrorDraw, ori, q);
        mat.rotate(q);

        MemoryPool.releaseSafe(q);

        var hbCenter = MemoryPool.newVector3f();
        var hbExtents = MemoryPool.newVector3f();
        dimensions.getVisualCenter(hbCenter);
        dimensions.getVisualExtents(hbExtents);

        mat.translate(hbCenter);
        mat.scale(hbExtents);
        MemoryPool.releaseSafe(hbCenter, hbExtents);

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
        return new GlowingCuboidInstanceData(new Matrix4f(transform), new Color(color));
    }
}
