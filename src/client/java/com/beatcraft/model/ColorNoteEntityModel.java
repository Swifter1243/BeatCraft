package com.beatcraft.model;

import com.beatcraft.BeatCraft;
import com.beatcraft.entity.ColorNoteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ColorNoteEntityModel extends EntityModel<ColorNoteEntity> {
    private final ModelPart base;
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(BeatCraft.MOD_ID, "base"), "main");

    public ColorNoteEntityModel(ModelPart modelPart) {
        this.base = modelPart.getChild("base");
    }

    public static TexturedModelData getTextureModelData() {

        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0F, 0F, 0F));
        addBody(base);
        return TexturedModelData.of(modelData, 64, 64);
    }

    public static void addBody(ModelPartData base) {
        base.addChild("body", ModelPartBuilder.create().cuboid(-6F, -6F, -6F, 12F, 12F, 12F), ModelTransform.pivot(0,0,0));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void setAngles(ColorNoteEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}
