package com.beatcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public abstract class WorldRenderer {
    protected static final Minecraft mc = Minecraft.getInstance();

    public void render(PoseStack matrices, Camera camera) {
        if (!shouldRender()) return;

        MultiBufferSource provider = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = provider.getBuffer(RenderType.solid());

        matrices.pushPose();
        Vec3 cameraPos = camera.getPosition();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        worldRender(matrices, vertexConsumer);
        matrices.popPose();
    }

    // I'll probably add more parameters to this as I need them
    protected abstract void worldRender(PoseStack matrices, VertexConsumer vertexConsumer);
    protected abstract boolean shouldRender();
}
