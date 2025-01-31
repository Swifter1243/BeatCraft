package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.object.ScorableObject;
import com.beatcraft.data.types.Mesh;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
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

    private static float CORNER_POS = 14/32f;
    private static float WALL_THICKNESS = 1/32f;

    public static final Mesh CUBE_MESH = new Mesh(
        List.of(
            // bottom of center cube
  /*   0 */ new Vector3f(-CORNER_POS, -CORNER_POS, -CORNER_POS),
  /*   1 */ new Vector3f( CORNER_POS, -CORNER_POS, -CORNER_POS),
  /*   2 */ new Vector3f( CORNER_POS, -CORNER_POS,  CORNER_POS),
  /*   3 */ new Vector3f(-CORNER_POS, -CORNER_POS,  CORNER_POS),

            // top of center cube
  /*   4 */ new Vector3f(-CORNER_POS,  CORNER_POS, -CORNER_POS),
  /*   5 */ new Vector3f( CORNER_POS,  CORNER_POS, -CORNER_POS),
  /*   6 */ new Vector3f( CORNER_POS,  CORNER_POS,  CORNER_POS),
  /*   7 */ new Vector3f(-CORNER_POS,  CORNER_POS,  CORNER_POS),

            // bottom face
  /*   8 */ new Vector3f(-CORNER_POS, -CORNER_POS-WALL_THICKNESS, -CORNER_POS),
  /*   9 */ new Vector3f( CORNER_POS, -CORNER_POS-WALL_THICKNESS, -CORNER_POS),
  /*  10 */ new Vector3f( CORNER_POS, -CORNER_POS-WALL_THICKNESS,  CORNER_POS),
  /*  11 */ new Vector3f(-CORNER_POS, -CORNER_POS-WALL_THICKNESS,  CORNER_POS),

            // top face
  /*  12 */ new Vector3f(-CORNER_POS,  CORNER_POS+WALL_THICKNESS, -CORNER_POS),
  /*  13 */ new Vector3f( CORNER_POS,  CORNER_POS+WALL_THICKNESS, -CORNER_POS),
  /*  14 */ new Vector3f( CORNER_POS,  CORNER_POS+WALL_THICKNESS,  CORNER_POS),
  /*  15 */ new Vector3f(-CORNER_POS,  CORNER_POS+WALL_THICKNESS,  CORNER_POS),

            // left face
  /*  16 */ new Vector3f( CORNER_POS+WALL_THICKNESS,  CORNER_POS, -CORNER_POS),
  /*  17 */ new Vector3f( CORNER_POS+WALL_THICKNESS,  CORNER_POS,  CORNER_POS),
  /*  18 */ new Vector3f( CORNER_POS+WALL_THICKNESS, -CORNER_POS,  CORNER_POS),
  /*  19 */ new Vector3f( CORNER_POS+WALL_THICKNESS, -CORNER_POS, -CORNER_POS),

            // right face
  /*  20 */ new Vector3f(-CORNER_POS-WALL_THICKNESS,  CORNER_POS, -CORNER_POS),
  /*  21 */ new Vector3f(-CORNER_POS-WALL_THICKNESS,  CORNER_POS,  CORNER_POS),
  /*  22 */ new Vector3f(-CORNER_POS-WALL_THICKNESS, -CORNER_POS,  CORNER_POS),
  /*  23 */ new Vector3f(-CORNER_POS-WALL_THICKNESS, -CORNER_POS, -CORNER_POS),

            // back face
  /*  24 */ new Vector3f( CORNER_POS, -CORNER_POS,  CORNER_POS+WALL_THICKNESS),
  /*  25 */ new Vector3f(-CORNER_POS, -CORNER_POS,  CORNER_POS+WALL_THICKNESS),
  /*  26 */ new Vector3f(-CORNER_POS,  CORNER_POS,  CORNER_POS+WALL_THICKNESS),
  /*  27 */ new Vector3f( CORNER_POS,  CORNER_POS,  CORNER_POS+WALL_THICKNESS),

            // front face
  /*  28 */ new Vector3f( CORNER_POS, -CORNER_POS, -CORNER_POS-WALL_THICKNESS),
  /*  29 */ new Vector3f(-CORNER_POS, -CORNER_POS, -CORNER_POS-WALL_THICKNESS),
  /*  30 */ new Vector3f(-CORNER_POS,  CORNER_POS, -CORNER_POS-WALL_THICKNESS),
  /*  31 */ new Vector3f( CORNER_POS,  CORNER_POS, -CORNER_POS-WALL_THICKNESS)
        ),
        List.of(
            // Bottom cube
            new int[]{8, 9, 10, 11}, // Bottom face
            new int[]{0, 1, 2, 3}, // Top face
            new int[]{1, 2, 10, 9}, // Left face
            new int[]{0, 3, 11, 8}, // Right face
            new int[]{2, 3, 11, 10}, // Back face
            new int[]{0, 1, 9, 8}, // Front face

            // Top cube
            new int[]{12, 13, 14, 15}, // Top face
            new int[]{4, 5, 6, 7}, // Bottom face
            new int[]{13, 14, 6, 5}, // Left face
            new int[]{4, 7, 15, 12}, // Right face
            new int[]{6, 7, 15, 14}, // Back face
            new int[]{4, 5, 13, 12}, // Front face

            // Left cube
            new int[]{16, 17, 18, 19}, // Left face
            new int[]{16, 17, 6, 5}, // Top face
            new int[]{18, 19, 1, 2}, // Bottom face
            new int[]{17, 18, 2, 6}, // Back face
            new int[]{16, 19, 1, 5}, // Front face
            new int[]{5, 6, 2, 1}, // Right face

            // Right cube
            new int[]{20, 21, 22, 23}, // Right face
            new int[]{20, 21, 7, 4}, // Top face
            new int[]{22, 23, 0, 3}, // Bottom face
            new int[]{21, 22, 3, 7}, // Back face
            new int[]{20, 23, 0, 4}, // Front face
            new int[]{4, 7, 3, 0}, // Left face

            // Back cube
            new int[]{24, 25, 26, 27}, // Back face
            new int[]{24, 25, 3, 2}, // Top face
            new int[]{26, 27, 6, 7}, // Bottom face
            new int[]{25, 26, 7, 3}, // Left face
            new int[]{24, 27, 6, 2}, // Right face
            new int[]{2, 3, 7, 6}, // Front face

            // Front cube
            new int[]{28, 29, 30, 31}, // Front face
            new int[]{28, 29, 0, 1}, // Top face
            new int[]{30, 31, 5, 4}, // Bottom face
            new int[]{28, 31, 5, 1}, // Left face
            new int[]{29, 30, 4, 0}, // Right face
            new int[]{1, 0, 4, 5}  // Back face
            //*/
        )
    );

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
}
