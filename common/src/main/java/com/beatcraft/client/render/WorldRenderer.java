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

    public void render(PoseStack matrices, Camera camera, float alpha) {
        if (!shouldRender()) return;

        matrices.pushPose();
        Vec3 cameraPos = camera.getPosition();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        worldRender(matrices, alpha);
        matrices.popPose();
    }

    // I'll probably add more parameters to this as I need them
    protected abstract void worldRender(PoseStack matrices, float alpha);
    protected abstract boolean shouldRender();
}
