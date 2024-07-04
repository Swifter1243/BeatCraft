package com.beatcraft.render;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.NoteColor;
import com.beatcraft.math.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Math;
import org.joml.Quaternionf;

import java.util.List;

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

        float red = Math.lerp(data.noteColor == NoteColor.RED ? 1 : 0, 1, 0.5f);
        float green = 0.5f;
        float blue = Math.lerp(data.noteColor == NoteColor.BLUE ? 1 : 0, 1, 0.5f);

        Random random = Random.create();

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuads(localPos, vertexConsumer, red, green, blue, model.getQuads(null, direction, random), 255, overlay);
        }

        // mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, m, 0, 0, 0, 127, overlay);
    }

    private static void renderQuads(
            MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, List<BakedQuad> quads, int light, int overlay
    ) {
        for (BakedQuad bakedQuad : quads) {
            float f = MathHelper.clamp(red, 0.0F, 1.0F);
            float g = MathHelper.clamp(green, 0.0F, 1.0F);
            float h = MathHelper.clamp(blue, 0.0F, 1.0F);

            vertexConsumer.quad(entry, bakedQuad, f, g, h, light, overlay);
        }
    }
}
