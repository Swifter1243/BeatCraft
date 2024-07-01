package com.beatcraft.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;

public class TestRenderer extends WorldRenderer {
    private static final BakedModel model;
    private static final int overlay = OverlayTexture.getUv(0, false);

    static {
        var modelID = new ModelIdentifier("minecraft", "stone", "inventory");
        model = mc.getBakedModelManager().getModel(modelID);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();
        matrices.multiply(new Quaternionf().rotateX(20));
        matrices.multiply(new Quaternionf().rotateZ(20));
        matrices.scale(2, 2, 2);
        matrices.translate(-0.5, -0.5, -0.5);
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, (BlockState)null, model, 1, 1, 1, 255, overlay);
    }
}
