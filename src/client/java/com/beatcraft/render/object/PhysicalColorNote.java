package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.Quad;
import com.beatcraft.render.mesh.QuadMesh;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

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

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek();


        if (!isBaseDissolved()) {
            var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
            var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
            BeatcraftRenderer.recordNoteRenderCall((tri, cam) -> {
                MeshLoader.COLOR_NOTE_RENDER_MESH.color = data.getColor().toARGB();
                MeshLoader.COLOR_NOTE_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
            });
            MirrorHandler.recordMirrorNoteDraw((tri, cam) -> {
                MeshLoader.COLOR_NOTE_RENDER_MESH.color = data.getColor().toARGB();
                MeshLoader.COLOR_NOTE_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
            });
        }

        if (!isArrowDissolved()) {
            var renderPos = localPos.getPositionMatrix().getTranslation(new Vector3f()).add(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());
            var renderRotation = localPos.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
            if (getData().getCutDirection() == CutDirection.DOT) {
                BeatcraftRenderer.recordArrowRenderCall((tri, cam) -> {
                    MeshLoader.NOTE_DOT_RENDER_MESH.color = 0xFFFFFFFF;
                    MeshLoader.NOTE_DOT_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
                });
                MirrorHandler.recordMirrorArrowDraw((tri, cam) -> {
                    MeshLoader.NOTE_DOT_RENDER_MESH.color = 0xFFFFFFFF;
                    MeshLoader.NOTE_DOT_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
                });
            } else {
                BeatcraftRenderer.recordArrowRenderCall((tri, cam) -> {
                    MeshLoader.NOTE_ARROW_RENDER_MESH.color = 0xFFFFFFFF;
                    MeshLoader.NOTE_ARROW_RENDER_MESH.drawToBuffer(tri, renderPos, renderRotation, cam);
                });
                MirrorHandler.recordMirrorArrowDraw((tri, cam) -> {
                    MeshLoader.NOTE_ARROW_RENDER_MESH.color = 0xFFFFFFFF;
                    MeshLoader.NOTE_ARROW_RENDER_MESH.drawToBufferMirrored(tri, renderPos, renderRotation, cam);
                });
            }

            //mc.getBlockRenderManager().getModelRenderer().render(localPos, vertexConsumer, null, arrowModel, getData().getColor().getRed(), getData().getColor().getGreen(), getData().getColor().getBlue(), 255, overlay);
        }
    }

    @Override
    public float getCollisionDistance() {
        return 0.688f;
    }

    @Override
    public Hitbox getGoodCutBounds() {
        if (getData().getCutDirection() == CutDirection.DOT) {
            // this hitbox may just need to be the normal one
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
    public QuadMesh getMesh() {
        return MeshLoader.COLOR_NOTE_MESH;
    }
}
