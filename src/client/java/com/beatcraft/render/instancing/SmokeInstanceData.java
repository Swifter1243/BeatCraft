package com.beatcraft.render.instancing;

import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.gl.GlUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.math3.analysis.function.Min;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;

import static com.beatcraft.render.instancing.InstancedMesh.FLOAT_SIZE_BYTES;
import static com.beatcraft.render.instancing.InstancedMesh.MATRIX4F_SIZE_BYTES;

public class SmokeInstanceData implements InstancedMesh.InstanceData {

    private final Matrix4f transform;
    private final float delta;

    private static final int TRANSFORM_LOCATION = 3;
    private static final int DELTA_LOCATION = 7;

    public SmokeInstanceData(Quaternionf orientation, Vector3f cameraPos, float delta) {
        var cameraRot = MinecraftClient.getInstance().gameRenderer.getCamera().getRotation().conjugate(new Quaternionf());
        transform = new Matrix4f()
            //.rotate(cameraRot.conjugate(new Quaternionf()))
            .translate(cameraPos.negate(new Vector3f()))
            .rotate(orientation)
        ;
        this.delta = delta;
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
            TRANSFORM_LOCATION,
            DELTA_LOCATION
        };
    }

    public void setup(int program) {
        int depthBuffer = Bloomfog.sceneDepthBuffer;

        GlUtil.setTex(program, "u_depth", 1, depthBuffer);
        GlUtil.setTex(program, "u_bloomfog", 2, BeatCraftRenderer.bloomfog.getBloomfogColorAttachment());

        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

    }

    public void cleanup() {
    }

}
