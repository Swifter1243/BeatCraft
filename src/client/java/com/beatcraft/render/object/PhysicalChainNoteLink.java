package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ChainNoteLink;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.logic.GameLogicHandler;
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
import org.joml.Vector3f;

public class PhysicalChainNoteLink extends PhysicalGameplayObject<ChainNoteLink> implements PhysicalScorableObject {

    public static final ModelIdentifier chainLinkModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "color_note_chain_link"), "inventory");
    public static final ModelIdentifier chainDotModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "chain_note_dot"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    private static final Hitbox GOOD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.4f, -0.1f, -0.75835f),
        new Vector3f(0.4f, 0.1f, 0.24995f)
    );

    private static final Hitbox BAD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.175f, -0.05f, -0.175f),
        new Vector3f(0.175f, 0.05f, 0.175f)
    );

    public static final Hitbox ACCURATE_HITBOX = new Hitbox(
        new Vector3f(-0.25f, -0.046875f, -0.25f),
        new Vector3f(0.25f, 0.046875f, 0.25f)
    );

    public PhysicalChainNoteLink(ChainNoteLink data) {
        super(data);

        baseDegrees = NoteMath.degreesFromCut(data.getCutDirection());
        baseDegrees = (baseDegrees + data.getAngleOffset()) % 360;
    }

    @Override
    protected boolean doNoteLook() {
        return !data.isNoteLookDisable();
    }

    @Override
    protected boolean doNoteGravity() {
        return !data.isNoteGravityDisabled();
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        BakedModel baseModel = mc.getBakedModelManager().getModel(chainLinkModelID);
        BakedModel arrowModel = mc.getBakedModelManager().getModel(chainDotModelID);

        if (!isBaseDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, baseModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }

        if (!isArrowDissolved()) {
            mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, arrowModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }
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
        return GOOD_CUT_BOUNDS;

    }

    @Override
    public Hitbox getBadCutBounds() {
        return BAD_CUT_BOUNDS;
    }

    @Override
    public Hitbox getAccurateHitbox() {
        return ACCURATE_HITBOX;
    }

    @Override
    public ScorableObject score$getData() {
        return getData();
    }

    @Override
    public void score$setContactColor(NoteType type) {
        setContactColor(type);
    }

    @Override
    public void score$setCutResult(GameLogicHandler.CutResult cut) {
        setCutResult(cut);
    }

    @Override
    public void score$cutNote() {
        cutNote();
    }

    @Override
    public GameLogicHandler.CutResult score$getCutResult() {
        return getCutResult();
    }

    // TODO: find correct values for these 5 functions
    @Override
    public int score$getMaxCutPositionScore() {
        return 10;
    }

    @Override
    public int score$getMaxFollowThroughScore() {
        return 5;
    }

    @Override
    public int score$getMaxFollowThroughAngle() {
        return 5;
    }

    @Override
    public int score$getMaxSwingInScore() {
        return 70;
    }

    @Override
    public int score$getMaxSwingInAngle() {
        return 100;
    }

}
