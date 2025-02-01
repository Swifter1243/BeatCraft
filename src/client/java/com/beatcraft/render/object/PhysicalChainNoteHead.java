package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ChainNoteHead;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.QuadMesh;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicalChainNoteHead extends PhysicalGameplayObject<ChainNoteHead> implements PhysicalScorableObject {

    public static final ModelIdentifier chainHeadModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "color_note_chain_head"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    public static final Hitbox ACCURATE_HITBOX = new Hitbox(
        new Vector3f(-0.25f, 0f, -0.25f),
        new Vector3f(0.25f, 0.25f, 0.25f)
    );

    public PhysicalChainNoteHead(ChainNoteHead data) {
        super(data);
        cutResult = GameLogicHandler.CutResult.noHit(this);
        baseDegrees = NoteMath.degreesFromCut(data.getCutDirection());
        baseDegrees = (baseDegrees + data.getAngleOffset()) % 360;
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        BakedModel arrowModel = mc.getBakedModelManager().getModel(PhysicalColorNote.noteArrowModelID);


        if (!isBaseDissolved()) {
            BeatcraftRenderer.recordNoteRenderCall(() -> {
                MeshLoader.CHAIN_HEAD_MESH.color = data.getColor().toARGB();
                MeshLoader.CHAIN_HEAD_MESH.render(localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f()), localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf()), true);
            });
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

    @Override
    public int score$getMaxCutPositionScore() {
        return 15;
    }

    @Override
    public int score$getMaxFollowThroughScore() {
        return 30;
    }

    @Override
    public int score$getMaxFollowThroughAngle() {
        return 60;
    }

    @Override
    public int score$getMaxSwingInScore() {
        return 70;
    }

    @Override
    public int score$getMaxSwingInAngle() {
        return 100;
    }

    @Override
    public QuadMesh getMesh() {
        return MeshLoader.CHAIN_HEAD_MESH;
    }
}
