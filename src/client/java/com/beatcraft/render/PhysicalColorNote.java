package com.beatcraft.render;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.math.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Quaternionf;

public class PhysicalColorNote extends PhysicalBeatmapObject<ColorNote> {
    private static final BakedModel model;
    private static final int overlay = OverlayTexture.getUv(0, false);

    static {
        var modelID = new ModelIdentifier("minecraft", "magenta_glazed_terracotta", "inventory");
        model = mc.getBakedModelManager().getModel(modelID);
    }

    public PhysicalColorNote(ColorNote data) {
        super(data);

        Quaternionf baseRotation = NoteMath.rotationFromCut(data.cutDirection);
        baseRotation.rotateZ(Math.toRadians(data.angleOffset));
        this.baseRotation = baseRotation;
    }


    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, 1, 1, 1, 255, overlay);
    }
}
