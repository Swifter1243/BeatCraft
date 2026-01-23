package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.beatmap.object.data.ChainNoteLink;
import com.beatcraft.client.beatmap.object.data.ScorableObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.instancing.ArrowInstanceData;
import com.beatcraft.client.render.instancing.ColorNoteInstanceData;
import com.beatcraft.client.render.instancing.InstancedMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicalChainNoteLink extends PhysicalGameplayObject<ChainNoteLink> implements PhysicalScorableObject {
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

    public PhysicalChainNoteLink(BeatmapController map, ChainNoteLink data) {
        super(map, data);
        scoreState = ScoreState.unChecked();
        baseDegrees = data.getCutDirection().baseAngleDegrees;
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


    private static final Color WHITE = new Color(0xFFFFFFFF);
    @Override
    protected void objectRender(PoseStack matrices, Camera camera, AnimationState animationState, float alpha) {
        var localPos = matrices.last();

        var renderPos = localPos.pose().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.pose().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var renderScale = localPos.pose().getScale(MemoryPool.newVector3f());
        var c = camera.getPosition().toVector3f();

        float mirrorY = mapController.worldPosition.y;

        var flipped = new Matrix4f()
            .translate(0, mirrorY, 0)   // move mirror plane to y = 0
            .scale(1, -1, 1)            // flip over Y
            .translate(0, -mirrorY, 0)  // move back
            .translate(renderPos)
            .rotate(renderRotation)
            .scale(renderScale);

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
            MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(localPos.pose(), data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            mapController.mirrorHandler.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(flipped, data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(mapController.logic.globalArrowDissolve, localArrowDissolve);
            MeshLoader.CHAIN_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(localPos.pose(), WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            mapController.mirrorHandler.MIRROR_CHAIN_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(flipped, WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            MeshLoader.CHAIN_DOT_INSTANCED_MESH.copyDrawToBloom(data.getColor().copy().withAlpha(alpha));

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
        return MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH;
    }
}
