package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ChainNoteLink;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
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
        cutResult = GameLogicHandler.CutResult.noHit(this);
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

        BakedModel arrowModel = mc.getBakedModelManager().getModel(chainDotModelID);

        if (!isBaseDissolved()) {
            var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
            var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
            BeatCraftRenderer.recordNoteRenderCall((tri, cam) -> {
                MeshLoader.CHAIN_LINK_RENDER_MESH.color = data.getColor().toARGB();
                MeshLoader.CHAIN_LINK_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
            });
            MirrorHandler.recordMirrorNoteDraw((tri, cam) -> {
                MeshLoader.CHAIN_LINK_RENDER_MESH.color = data.getColor().toARGB();
                MeshLoader.CHAIN_LINK_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
            });
        }


        if (!isArrowDissolved()) {
            var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
            var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
            BeatCraftRenderer.recordArrowRenderCall((tri, cam) -> {
                MeshLoader.CHAIN_DOT_RENDER_MESH.color = 0xFFFFFFFF;
                MeshLoader.CHAIN_DOT_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
            });
            MirrorHandler.recordMirrorArrowDraw((tri, cam) -> {
                MeshLoader.CHAIN_DOT_RENDER_MESH.color = 0xFFFFFFFF;
                MeshLoader.CHAIN_DOT_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
            });
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

    @Override
    public Quaternionf score$getLaneRotation() {
        return getLaneRotation();
    }

    @Override
    public QuadMesh getMesh() {
        return MeshLoader.CHAIN_LINK_MESH;
    }
}
