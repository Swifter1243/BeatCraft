package com.beatcraft.render.object;

import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.ColorNote;
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

public class PhysicalColorNote extends PhysicalGameplayObject<ColorNote> {
    public static final ModelIdentifier colorNoteBlockModelID = new ModelIdentifier("beatcraft", "color_note", "inventory");
    public static final ModelIdentifier noteArrowModelID = new ModelIdentifier("beatcraft", "note_arrow", "inventory");
    public static final ModelIdentifier noteDotModelID = new ModelIdentifier("beatcraft", "note_dot", "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    public PhysicalColorNote(ColorNote data) {
        super(data);

        baseDegrees = NoteMath.degreesFromCut(data.getCutDirection());
        baseDegrees = (baseDegrees + data.getAngleOffset()) % 360;
    }

    public void checkWindowSnap(PhysicalColorNote other) {
        boolean sameCuts = data.getCutDirection() == other.data.getCutDirection();
        boolean thisIsDot = data.getCutDirection() == CutDirection.DOT;
        boolean otherIsDot = other.data.getCutDirection() == CutDirection.DOT;

        boolean bothAreDifferentArrows = !sameCuts && !thisIsDot && !otherIsDot;
        if (bothAreDifferentArrows) {
            return;
        }

        Vector2f thisPos = get2DPosition();
        Vector2f otherPos = other.get2DPosition();
        Vector2f toOther = otherPos.sub(thisPos);
        float windowDegrees = MathUtil.getVectorAngleDegrees(toOther) + 90; // identity note rotation (down) is -90 in typical angle space

        boolean bothAreDots = thisIsDot && otherIsDot;
        if (bothAreDots) {
            baseDegrees = windowDegrees;
            other.baseDegrees = windowDegrees;
            return;
        }

        float degrees = thisIsDot ? other.baseDegrees : baseDegrees;
        float between = MathUtil.degreesBetween(degrees, windowDegrees);

        if (between <= 40) {
            baseDegrees = windowDegrees;
            other.baseDegrees = windowDegrees;
        }
    }

    @Override
    protected boolean doNoteLook() {
        return !data.isNoteLookDisabled();
    }

    @Override
    protected boolean doNoteGravity() {
        return !data.isNoteGravityDisabled();
    }

    public void finalizeBaseRotation() {
        float radians = Math.toRadians(baseDegrees);
        this.baseRotation = new Quaternionf().rotateZ(radians);
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        BakedModel baseModel = mc.getBakedModelManager().getModel(colorNoteBlockModelID);
        BakedModel arrowModel;
        if (getData().getCutDirection() == CutDirection.DOT) {
            arrowModel = mc.getBakedModelManager().getModel(noteDotModelID);
        } else {
            arrowModel = mc.getBakedModelManager().getModel(noteArrowModelID);
        }

        if (!isBaseDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, baseModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }

        if (!isArrowDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, arrowModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }
    }
}
