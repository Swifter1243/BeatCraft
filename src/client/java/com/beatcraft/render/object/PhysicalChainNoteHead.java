package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ChainNoteHead;
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
            MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(localPos.getPositionMatrix(), data.getColor(), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(flipped, data.getColor(), dissolve, data.getMapIndex()));
        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(GameLogicHandler.globalDissolve, getArrowDissolve());
            MeshLoader.NOTE_ARROW_INSTANCED_MESH.draw(ArrowInstanceData.create(localPos.getPositionMatrix(), WHITE, dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH.draw(ArrowInstanceData.create(flipped, WHITE, dissolve, data.getMapIndex()));
            MeshLoader.NOTE_ARROW_INSTANCED_MESH.copyDrawToBloom();

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
    public Quaternionf score$getLaneRotation() {
        return getLaneRotation();
    }

    @Override
    public InstancedMesh<ColorNoteInstanceData> getMesh() {
        return MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH;
    }
}
