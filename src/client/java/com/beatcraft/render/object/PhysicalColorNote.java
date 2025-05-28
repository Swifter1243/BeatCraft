package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.data.types.Color;
import com.beatcraft.debug.BeatCraftDebug;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.instancing.ArrowInstanceData;
import com.beatcraft.render.instancing.ColorNoteInstanceData;
import com.beatcraft.render.instancing.InstancedMesh;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.QuadMesh;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.*;
import org.joml.Math;

public class PhysicalColorNote extends PhysicalGameplayObject<ColorNote> implements PhysicalScorableObject {
    public static final ModelIdentifier colorNoteBlockModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID, "color_note"), "inventory");
    public static final ModelIdentifier noteArrowModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID,  "note_arrow"), "inventory");
    public static final ModelIdentifier noteDotModelID = new ModelIdentifier(Identifier.of(BeatCraft.MOD_ID,  "note_dot"), "inventory");
    private static final int overlay = OverlayTexture.getUv(0, false);
    private float baseDegrees;

    public static final Hitbox NORMAL_GOOD_CUT_BOUNDS = new Hitbox(
                new Vector3f(-0.4f, -0.25f, -0.75f),
                new Vector3f(0.4f, 0.25f, 0.25f)
            );

    private static final Hitbox DOT_GOOD_CUT_BOUNDS = new Hitbox(
                new Vector3f(-0.4f, -0.4f, -0.75f),
                new Vector3f(0.4f, 0.4f, 0.25f)
            );

    public static final Hitbox BAD_CUT_BOUNDS = new Hitbox(
        new Vector3f(-0.175f, -0.175f, -0.175f),
        new Vector3f(0.175f, 0.175f, 0.175f)
    );

    public static final Hitbox ACCURATE_HITBOX = new Hitbox(
        new Vector3f(-0.25f, -0.25f, -0.25f),
        new Vector3f(0.25f, 0.25f, 0.25f)
    );

    public PhysicalColorNote(ColorNote data) {
        super(data);
        cutResult = GameLogicHandler.CutResult.noHit(this);
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
            MeshLoader.COLOR_NOTE_INSTANCED_MESH.draw(new ColorNoteInstanceData(localPos.getPositionMatrix(), data.getColor(), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_COLOR_NOTE_INSTANCED_MESH.draw(new ColorNoteInstanceData(flipped, data.getColor(), dissolve, data.getMapIndex()));

        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(GameLogicHandler.globalDissolve, getArrowDissolve());
            if (getData().getCutDirection() == CutDirection.DOT) {
                MeshLoader.NOTE_DOT_INSTANCED_MESH.draw(new ArrowInstanceData(localPos.getPositionMatrix(), WHITE, dissolve, data.getMapIndex()));
                MeshLoader.MIRROR_NOTE_DOT_INSTANCED_MESH.draw(new ArrowInstanceData(flipped, WHITE, dissolve, data.getMapIndex()));
                MeshLoader.NOTE_DOT_INSTANCED_MESH.copyDrawToBloom();
                //if (dissolve == 0) {
                //    BeatCraftRenderer.bloomfog.recordArrowBloomCall((b, v, q) -> {
                //        MeshLoader.NOTE_DOT_RENDER_MESH.color = data.getColor().toARGB();
                //        MeshLoader.NOTE_DOT_RENDER_MESH.drawToBuffer(b, worldToCameraSpace(renderPos, v, q), MemoryPool.newQuaternionf(q).mul(renderRotation), v);
                //    });
                //}
            } else {
                MeshLoader.NOTE_ARROW_INSTANCED_MESH.draw(new ArrowInstanceData(localPos.getPositionMatrix(), WHITE, dissolve, data.getMapIndex()));
                MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH.draw(new ArrowInstanceData(flipped, WHITE, dissolve, data.getMapIndex()));
                MeshLoader.NOTE_ARROW_INSTANCED_MESH.copyDrawToBloom();
                //if (dissolve == 0) {
                //    BeatCraftRenderer.bloomfog.recordArrowBloomCall((b, v, q) -> {
                //        MeshLoader.NOTE_ARROW_RENDER_MESH.color = data.getColor().toARGB();
                //        MeshLoader.NOTE_ARROW_RENDER_MESH.drawToBuffer(b, worldToCameraSpace(renderPos, v, q), MemoryPool.newQuaternionf(q).mul(renderRotation), v);
                //    });
                //}
            }
        }
    }

    @Override
    public float getCollisionDistance() {
        return 0.688f;
    }

    @Override
    public Hitbox getGoodCutBounds() {
        if (getData().getCutDirection() == CutDirection.DOT) {
            return DOT_GOOD_CUT_BOUNDS;
        } else {
            return NORMAL_GOOD_CUT_BOUNDS;
        }
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
    public ColorNote getData() {
        return super.getData();
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
        return MeshLoader.COLOR_NOTE_INSTANCED_MESH;
    }
}
