package com.beatcraft.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public abstract class WorldRenderer {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        VertexConsumerProvider provider = mc.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getSolid());

        matrices.push();
        Vec3d cameraPos = camera.getPos();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        worldRender(matrices, vertexConsumer);
        matrices.pop();
    }

    // I'll probably add more parameters to this as I need them
    protected abstract void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer);
}
