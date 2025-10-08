package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.beatmap.object.data.ColorNote;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.object.data.ScorableObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import com.beatcraft.common.data.types.Color;
//import com.beatcraft.common.debug.BeatCraftDebug;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.instancing.ArrowInstanceData;
import com.beatcraft.client.render.instancing.ColorNoteInstanceData;
import com.beatcraft.client.render.instancing.InstancedMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.*;
import org.joml.Math;

public class PhysicalColorNote extends PhysicalGameplayObject<ColorNote> implements PhysicalScorableObject {
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

    public PhysicalColorNote(BeatmapController map, ColorNote data) {
        super(map, data);
        scoreState.setUnchecked();
        baseDegrees = data.getCutDirection().baseAngleDegrees;
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

    private static final Color WHITE = new Color(0xFFFFFFFF);
    @Override
    protected void objectRender(PoseStack matrices, Camera camera, AnimationState animationState, float alpha) {
        var localPos = matrices.last();

        var renderPos = localPos.pose().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.pose().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var renderScale = localPos.pose().getScale(MemoryPool.newVector3f());
        var c = camera.getPosition().toVector3f();

        var flipped = new Matrix4f().scale(1, -1, 1);
        flipped.translate(0, c.y * 2f, 0);
        flipped.translate(renderPos);
        flipped.rotate(renderRotation);
        flipped.scale(renderScale);

        MemoryPool.release(renderPos, renderScale);
        MemoryPool.release(renderRotation);

        var localDissolve = getBaseDissolve();
        if (mapController.isModifierActive("Ghost Notes")) {
            if (mapController.firstBeat < this.data.getBeat()) {
                localDissolve = 1;
            } else {
                var s = this.getSpawnBeat();
                var e = this.getDisappearBeat();
                var t = mapController.currentBeat;
                localDissolve = Math.clamp(MathUtil.inverseLerp(s, e, t), 0, 1);
            }
        }

        var localArrowDissolve = getArrowDissolve();
        if (mapController.isModifierActive("Disappearing Arrows")) {
            var s = this.getSpawnBeat();
            var e = this.getDisappearBeat();
            var t = mapController.currentBeat;
            localArrowDissolve = Math.clamp(MathUtil.inverseLerp(s, e, t), 0, 1);
        }

        if (!isBaseDissolved()) {
            var dissolve = Math.max(mapController.logic.globalDissolve, localDissolve);
            MeshLoader.COLOR_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(localPos.pose(), data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_COLOR_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(flipped, data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));

        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(mapController.logic.globalArrowDissolve, localArrowDissolve);
            if (getData().getCutDirection() == CutDirection.DOT) {
                MeshLoader.NOTE_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(localPos.pose(), WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
                MeshLoader.MIRROR_NOTE_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(flipped, WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
                MeshLoader.NOTE_DOT_INSTANCED_MESH.copyDrawToBloom(data.getColor().copy().withAlpha(alpha));

            } else {
                MeshLoader.NOTE_ARROW_INSTANCED_MESH.draw(ArrowInstanceData.create(localPos.pose(), WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
                MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH.draw(ArrowInstanceData.create(flipped, WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
                MeshLoader.NOTE_ARROW_INSTANCED_MESH.copyDrawToBloom(data.getColor().copy().withAlpha(alpha));

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
    public void score$setScoreState(ScoreState state) {
        setScoreState(state);
    }

    @Override
    public void score$cutNote() {
        cutNote();
    }

    @Override
    public ScoreState score$getScoreState() {
        return getScoreState();
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
    public CutDirection score$getCutDirection() {
        return data.getCutDirection();
    }

    @Override
    public void score$spawnDebris(Vector3f point, Vector3f normal) {
        spawnDebris(point, normal);
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