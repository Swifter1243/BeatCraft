package com.beatcraft.client.render.instancing;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.gl.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.beatcraft.client.render.instancing.InstancedMesh.FLOAT_SIZE_BYTES;

public class HeadsetInstanceData implements InstancedMesh.InstanceData {
    private final Matrix4f transform;

    private static final ArrayList<HeadsetInstanceData> sharedCache = new ArrayList<>();

    private HeadsetInstanceData(Matrix4f transform) {
        this.transform = transform;
    }

    public static HeadsetInstanceData create(Matrix4f transform) {
        if (sharedCache.isEmpty()) {
            return new HeadsetInstanceData(new Matrix4f(transform));
        } else {
            var x = sharedCache.removeLast();
            x.transform.set(transform);
            return x;
        }
    }

    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    private static final int TRANSFORM_LOCATION = 3;
    private static final int[] LOCATIONS = new int[]{ TRANSFORM_LOCATION };

    @Override
    public void putData(FloatBuffer buffer) {

        buffer.put(transform.m00()).put(transform.m01()).put(transform.m02()).put(transform.m03());
        buffer.put(transform.m10()).put(transform.m11()).put(transform.m12()).put(transform.m13());
        buffer.put(transform.m20()).put(transform.m21()).put(transform.m22()).put(transform.m23());
        buffer.put(transform.m30()).put(transform.m31()).put(transform.m32()).put(transform.m33());

    }

    @Override
    public int getFrameSize() {
        return 16;
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
    }

    @Override
    public int[] getLocations() {
        return LOCATIONS;
    }

    @Override
    public void free() {
        sharedCache.add(this);
    }

    private static final ResourceLocation BLOOM_MAP = Beatcraft.id("textures/item/headset_bloom_map.png");

    @Override
    public void setup(int program, DrawPass pass) {

        GlUtil.uniform1i("u_pass", pass.ordinal());

        if (pass == DrawPass.Bloom) {
            var manager = Minecraft.getInstance().getTextureManager();
            var aTex = manager.getTexture(BLOOM_MAP);
            RenderSystem.setShaderTexture(2, BLOOM_MAP);
            GlUtil.setTex(program, "u_depth", 1, Bloomfog.sceneDepthBuffer);
            GlUtil.setTex(program, "u_bloom_map", 2, aTex.getId());
        }

        RenderSystem.enableBlend();

    }

    @Override
    public void cleanup() {
        RenderSystem.disableCull();
    }

    @Override
    public InstancedMesh.InstanceData copy() {
        return create(this.transform);
    }
}
