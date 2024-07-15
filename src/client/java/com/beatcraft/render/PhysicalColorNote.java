package com.beatcraft.render;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2f;

public class PhysicalColorNote extends PhysicalBeatmapObject<ColorNote> {
    public static final ModelIdentifier colorNoteArrowModelID = new ModelIdentifier("beatcraft", "color_note_arrow", "inventory");
    public static final ModelIdentifier colorNoteDotModelID = new ModelIdentifier("beatcraft", "color_note_dot", "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    public PhysicalColorNote(ColorNote data) {
        super(data);

        baseDegrees = NoteMath.degreesFromCut(data.getCutDirection());
        baseDegrees = (baseDegrees + data.getAngleOffset()) % 360;
    }

    public void checkWindow(PhysicalColorNote other) {
        Vector2f thisPos = get2DPosition();
        Vector2f otherPos = other.get2DPosition();
        Vector2f toOther = otherPos.sub(thisPos);

        float windowDegrees = MathUtil.getVectorAngleDegrees(toOther) + 90; // identity note rotation (down) is -90 in typical angle space
        float between = MathUtil.degreesBetween(baseDegrees, windowDegrees);

        if (between < 45) {
            baseDegrees = windowDegrees;
            other.baseDegrees = windowDegrees;
        }
    }

    public void finalizeBaseRotation() {
        float radians = Math.toRadians(baseDegrees);
        this.baseRotation = new Quaternionf().rotateZ(radians);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        var localPos = matrices.peek();

        BakedModel model;
        if (getData().getCutDirection() == CutDirection.DOT) {
            model = mc.getBakedModelManager().getModel(colorNoteDotModelID);
        } else {
            model = mc.getBakedModelManager().getModel(colorNoteArrowModelID);
        }
        mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, model, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
    }
}
