package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ChainNoteLink;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.data.types.Color;
import com.beatcraft.debug.BeatCraftDebug;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.instancing.ArrowInstanceData;
import com.beatcraft.render.instancing.ColorNoteInstanceData;
import com.beatcraft.render.instancing.InstancedMesh;
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
import org.joml.Matrix4f;
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

    public Vector3f worldToCameraSpace(Vector3f renderPos, Vector3f cameraPos, Quaternionf cameraRot) {
        var a = renderPos.sub(cameraPos, new Vector3f());
        a.rotate(cameraRot);
        a.add(cameraPos);
        return a;
    }

    private static final Color WHITE = new Color(0xFFFFFFFF);
    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();

        var renderPos = localPos.getPositionMatrix().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var c = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        var flipped = new Matrix4f().scale(1, -1, 1);
        flipped.translate(0, c.y * 2f, 0);
        flipped.translate(renderPos);
        flipped.rotate(renderRotation);
        flipped.scale(0.5f);

        renderPos.add(c);

        if (!isBaseDissolved()) {
            var dissolve = Math.max(GameLogicHandler.globalDissolve, getBaseDissolve());
            MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.draw(new ColorNoteInstanceData(localPos.getPositionMatrix(), data.getColor(), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH.draw(new ColorNoteInstanceData(flipped, data.getColor(), dissolve, data.getMapIndex()));
        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(GameLogicHandler.globalDissolve, getArrowDissolve());
            MeshLoader.CHAIN_DOT_INSTANCED_MESH.draw(new ArrowInstanceData(localPos.getPositionMatrix(), WHITE, dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_CHAIN_DOT_INSTANCED_MESH.draw(new ArrowInstanceData(flipped, WHITE, dissolve, data.getMapIndex()));
            MeshLoader.NOTE_DOT_INSTANCED_MESH.copyDrawToBloom();
            //if (dissolve == 0) {
            //    BeatCraftRenderer.bloomfog.recordArrowBloomCall((b, v, q) -> {
            //        MeshLoader.CHAIN_DOT_RENDER_MESH.color = data.getColor().toARGB();
            //        MeshLoader.CHAIN_DOT_RENDER_MESH.drawToBuffer(b, worldToCameraSpace(renderPos, v, q), MemoryPool.newQuaternionf(q).mul(renderRotation), v);
            //    });
            //}
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
    public InstancedMesh<ColorNoteInstanceData> getMesh() {
        return MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH;
    }
}
