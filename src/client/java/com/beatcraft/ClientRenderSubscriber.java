package com.beatcraft;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ClientRenderSubscriber {
    public static float totalSeconds = 0f;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void TestRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        totalSeconds += tickDelta;

        VertexConsumerProvider provider = mc.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertices = provider.getBuffer(RenderLayer.getSolid());
        int overlay = OverlayTexture.getUv(0, false);

        matrices.push();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
        matrices.multiply(new Quaternionf().fromAxisAngleDeg(0,1,1, totalSeconds * 0.2f));

        float s = (float) Math.sin(totalSeconds * 0.1) * 0.1f + 1f;
        matrices.scale(s, s, s);

        matrices.translate(-0.5, -0.5, -0.5);

        var modelID = new ModelIdentifier("minecraft", "stone", "inventory");
        var blockModel = mc.getBakedModelManager().getModel(modelID);

        for (int i = 0; i < 100; i++) {
            var localPos = matrices.peek();

            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertices, (BlockState)null, blockModel, 1, 1, 1, 255, overlay);

            matrices.translate(1, 0, 0);
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(0,0,1, i * 0.1f * s));
        }


        matrices.pop();
    }
}
