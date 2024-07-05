package com.beatcraft.render;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.math.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Quaternionf;

public class PhysicalColorNote extends PhysicalBeatmapObject<ColorNote> {
    public static final ModelIdentifier colorNoteArrowModelID = new ModelIdentifier("beatcraft", "color_note_arrow", "inventory");
    public static final ModelIdentifier colorNoteDotModelID = new ModelIdentifier("beatcraft", "color_note_dot", "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);

    public PhysicalColorNote(ColorNote data) {
        super(data);

        Quaternionf baseRotation = NoteMath.rotationFromCut(data.cutDirection);
        baseRotation.rotateZ(Math.toRadians(data.angleOffset));
        this.baseRotation = baseRotation;
    }


    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();

        BakedModel model;
        if (data.cutDirection == CutDirection.DOT) {
            model = mc.getBakedModelManager().getModel(colorNoteDotModelID);
        } else {
            model = mc.getBakedModelManager().getModel(colorNoteArrowModelID);
        }
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, data.color.red, data.color.green, data.color.blue, 255, overlay);
    }
}
