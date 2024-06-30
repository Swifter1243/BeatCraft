package com.beatcraft.entity;

import com.beatcraft.model.ColorNoteEntityModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ColorNoteRenderer extends EntityRenderer<ColorNoteEntity> {
    ColorNoteEntityModel model;

    public ColorNoteRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        model = new ColorNoteEntityModel(ctx.getPart(ColorNoteEntityModel.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(ColorNoteEntity entity) {
        return new Identifier("textures/block/white_wool.png"); // Temporary!!
    }

    @Override
    public void render(ColorNoteEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        RenderLayer renderLayer = model.getLayer(getTexture(entity));
        VertexConsumer vertices = vertexConsumers.getBuffer(renderLayer);
        model.render(matrices, vertices, light, 0, 1, 0, 0, 1);
    }
}
