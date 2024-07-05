package com.beatcraft.render;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteColor;
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
    private static BakedModel colorNoteArrowModel;
    private static BakedModel colorNoteArrowDot;
    private static final int overlay = OverlayTexture.getUv(0, false);

    public static void loadModels() {
        if (colorNoteArrowModel != null) return;
        colorNoteArrowModel = mc.getBakedModelManager().getModel(colorNoteArrowModelID);
        colorNoteArrowDot = mc.getBakedModelManager().getModel(colorNoteDotModelID);
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

        float red = data.noteColor == NoteColor.RED ? 1 : 0;
        float green = 0;
        float blue = data.noteColor == NoteColor.BLUE ? 1 : 0;

        loadModels();

        if (data.cutDirection == CutDirection.DOT) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, colorNoteArrowDot, red, green, blue, 255, overlay);
        } else {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, colorNoteArrowModel, red, green, blue, 255, overlay);
        }
    }
}
