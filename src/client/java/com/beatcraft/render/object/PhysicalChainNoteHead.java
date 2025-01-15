package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.ChainNoteHead;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Math;
import org.joml.Quaternionf;

public class PhysicalChainNoteHead extends PhysicalGameplayObject<ChainNoteHead> {

    public static final ModelIdentifier chainHeadModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "color_note_chain_head"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    public PhysicalChainNoteHead(ChainNoteHead data) {
        super(data);

        baseDegrees = NoteMath.degreesFromCut(data.getCutDirection());
        baseDegrees = (baseDegrees + data.getAngleOffset()) % 360;
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        BakedModel baseModel = mc.getBakedModelManager().getModel(chainHeadModelID);
        BakedModel arrowModel = mc.getBakedModelManager().getModel(PhysicalColorNote.noteArrowModelID);


        if (!isBaseDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, baseModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }

        if (!isArrowDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, arrowModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
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
    public float getCollisionDistance() {
        return 0.688f;
    }

    @Override
    public Hitbox getGoodCutBounds() {
        return PhysicalColorNote.NORMAL_GOOD_CUT_BOUNDS;
    }

    @Override
    public Hitbox getBadCutBounds() {
        return PhysicalColorNote.BAD_CUT_BOUNDS;
    }
}
