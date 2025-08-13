package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.animation.AnimationState;
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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

    public PhysicalChainNoteLink(BeatmapPlayer map, ChainNoteLink data) {
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

    public Vector3f worldToCameraSpace(Vector3f renderPos, Vector3f cameraPos, Quaternionf cameraRot) {
        var a = renderPos.sub(cameraPos, new Vector3f());
        a.rotate(cameraRot);
        a.add(cameraPos);
        return a;
    }

    private static final Color WHITE = new Color(0xFFFFFFFF);
    @Override
    protected void objectRender(PoseStack matrices, AnimationState animationState, float alpha) {
        var localPos = matrices.last();

        var renderPos = localPos.pose().getTranslation(MemoryPool.newVector3f());
        var renderRotation = localPos.pose().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var c = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        var flipped = new Matrix4f().scale(1, -1, 1);
        flipped.translate(0, c.y * 2f, 0);
        flipped.translate(renderPos);
        flipped.rotate(renderRotation);
        flipped.scale(0.5f);

        renderPos.add(c);

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
            var dissolve = Math.max(mapController.globalDissolve, localDissolve);
            MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(localPos.pose(), data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH.draw(ColorNoteInstanceData.create(flipped, data.getColor().copy().withAlpha(alpha), dissolve, data.getMapIndex()));
        }

        if (!isArrowDissolved()) {
            var dissolve = Math.max(mapController.globalArrowDissolve, localArrowDissolve);
            MeshLoader.CHAIN_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(localPos.pose(), WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
            MeshLoader.MIRROR_CHAIN_DOT_INSTANCED_MESH.draw(ArrowInstanceData.create(flipped, WHITE.copy().withAlpha(alpha), dissolve, data.getMapIndex()));
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
    public Quaternionf score$getLaneRotation() {
        return getLaneRotation();
    }

    @Override
    public InstancedMesh<ColorNoteInstanceData> getMesh() {
        return MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH;
    }
}
