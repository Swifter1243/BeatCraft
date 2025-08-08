package com.beatcraft.client.render.instancing;

import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.gl.GlUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.beatcraft.client.render.instancing.InstancedMesh.FLOAT_SIZE_BYTES;
import static com.beatcraft.client.render.instancing.InstancedMesh.MATRIX4F_SIZE_BYTES;

public class SmokeInstanceData implements InstancedMesh.InstanceData {

    private final Matrix4f transform;
    private float delta;

    private static final int TRANSFORM_LOCATION = 3;
    private static final int DELTA_LOCATION = 7;

    private static final ArrayList<SmokeInstanceData> sharedCache = new ArrayList<>();

    private SmokeInstanceData(Quaternionf orientation, Vector3f cameraPos, float delta) {
        rot = orientation;
        pos = cameraPos;
        var v = MemoryPool.newVector3f();
        transform = new Matrix4f()
            //.rotate(cameraRot.conjugate(new Quaternionf()))
            .translate(cameraPos.negate(v))
            .rotate(orientation)
        ;
        MemoryPool.releaseSafe(v);
        this.delta = delta;
    }

    private Quaternionf rot;
    private Vector3f pos;

    public static SmokeInstanceData create(Quaternionf orientation, Vector3f cameraPos, float delta) {
        if (sharedCache.isEmpty()) {
            return new SmokeInstanceData(new Quaternionf(orientation), new Vector3f(cameraPos), delta);
        } else {
            var x = sharedCache.removeLast();
            var v = MemoryPool.newVector3f();
            x.transform.identity().translate(cameraPos.negate(v)).rotate(orientation);
            MemoryPool.releaseSafe(v);
            x.delta = delta;
            x.rot = orientation;
            x.pos = cameraPos;
            return x;
        }
    }

    @Override
    public InstancedMesh.InstanceData copy() {
        return SmokeInstanceData.create(rot, pos, delta);
    }

    @Override
    public void free() {
        sharedCache.add(this);
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

        buffer.put(delta).put(0).put(0).put(0);
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
            GL31.glVertexAttribPointer(location, 4, GL31.GL_FLOAT, false,
                stride, i * 4 * FLOAT_SIZE_BYTES);
            GL31.glEnableVertexAttribArray(location);
            ARBInstancedArrays.glVertexAttribDivisorARB(location, 1);
        }

        GL31.glVertexAttribPointer(DELTA_LOCATION, 1, GL31.GL_FLOAT, false,
            stride, MATRIX4F_SIZE_BYTES);
        GL31.glEnableVertexAttribArray(DELTA_LOCATION);
        ARBInstancedArrays.glVertexAttribDivisorARB(DELTA_LOCATION, 1);

    }

    @Override
    public int[] getLocations() {
        return new int[]{
            TRANSFORM_LOCATION, TRANSFORM_LOCATION + 1, TRANSFORM_LOCATION + 2, TRANSFORM_LOCATION + 3,
            DELTA_LOCATION
        };
    }

    public void setup(int program) {
        int depthBuffer = Bloomfog.sceneDepthBuffer;

        GlUtil.setTex(program, "u_depth", 1, depthBuffer);
        GlUtil.setTex(program, "u_bloomfog", 2, BeatcraftRenderer.bloomfog.getBloomfogColorAttachment());

        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

    }

    public void cleanup() {
    }

}