package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.audio.AudioController;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import com.beatcraft.client.beatmap.object.data.SpawnQuaternionPool;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.WorldRenderer;
import com.beatcraft.client.render.instancing.ColorNoteInstanceData;
import com.beatcraft.client.render.instancing.InstancedMesh;
import com.beatcraft.client.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.client.render.particle.Debris;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;

public abstract class PhysicalGameplayObject<T extends GameplayObject> extends WorldRenderer {

    public final BeatmapPlayer mapController;

    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 0.4f;
    protected static final float SIZE_SCALAR = 0.5f;
    protected static final Vector3f WORLD_OFFSET = new Vector3f(0, 0.8f, 1f);
    protected final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    protected Quaternionf baseRotation = new Quaternionf();
    private Quaternionf laneRotation = new Quaternionf();
    private Quaternionf lookRotation = new Quaternionf();
    private Vector3f worldPos = new Vector3f();
    private Quaternionf worldRot = new Quaternionf();
    private Matrix4f matrix = new Matrix4f();
    private AnimationState animationState = new AnimationState();
    protected T data;
    protected boolean despawned = false;
    protected ScoreState scoreState = ScoreState.unChecked();
    private NoteType contactColor = null;

    public PhysicalGameplayObject(BeatmapPlayer beatmap, T data) {
        this.mapController = beatmap;
        this.data = data;
    }

    private Vector3f getPlayerHeadPosition() {
        return new Vector3f(0, 1.62f, 0);
    }

    public float getSpawnBeat() {
        return getData().getBeat() - data.getJumps().halfDuration();
    }

    public float getJumpInBeat() {
        return getData().getBeat() - data.getJumps().halfDuration() * 0.5f;
    }

    public float getDisappearBeat() {
        return getData().getBeat() - data.getJumps().halfDuration() * 0.4f;
    }

    public float getJumpOutBeat() {
        return getData().getBeat() + data.getJumps().halfDuration() * 0.5f;
    }

    public float getDespawnBeat() {
        return getData().getBeat() + data.getJumps().halfDuration();
    }

    public float getSpawnPosition() {
        return data.getJumps().jumpDistance() / 2;
    }

    public float getJumpOutPosition() {
        return data.getJumps().jumpDistance() * -0.25f;
    }

    private void despawn() {
        despawned = true;
    }

    public boolean isDespawned() {
        return despawned;
    }

    public void seek(float beat) {
        despawned = false;
        if (this instanceof PhysicalScorableObject scorable) {
            scoreState = ScoreState.missed();
        }
        update(beat);
    }

    public void update(float beat) {
        if (!isInWorld()) {
            return;
        }

        AnimationState animatedPropertyState = data.getTrackContainer().getAnimatedPropertyState();

        beat = applyTimeRemapping(beat, animatedPropertyState);
        if (jumpEnded(beat)) {
            despawn();
            return;
        }

        if (pastBeat(beat)) {
            if (this instanceof PhysicalScorableObject scorable) {
                //scorable.score$getScoreState().setContactPosition(this.getWorldPos());
                //scorable.score$getScoreState().finalizeScore();
            }
        }

        float lifetime = getLifetime(beat);
        animationState = animatedPropertyState;
        animationState = AnimationState.combine(animationState, getObjectPathAnimationState(lifetime));
        animationState = AnimationState.combine(animationState, getTrackPathAnimationState(lifetime));

        matrix = getMatrix(beat, animationState);
    }

    public boolean hasAppeared() {
        float margin = MathUtil.secondsToBeats(JUMP_SECONDS, mapController.getBpm(data.getBeat()));
        //float margin = BeatmapPlayer.currentInfo.getBeat(JUMP_SECONDS, 1f);
        return mapController.currentBeat >= getSpawnBeat() - margin;
    }

    public boolean isInWorld() {
        return hasAppeared() && !isDespawned();
    }

    protected boolean isBaseDissolved() {
        Float dissolve = animationState.getDissolve();
        if (dissolve == null) {
            return false;
        } else {
            return dissolve == 0;
        }
    }

    public float getBaseDissolve() {
        Float dissolve = animationState.getDissolve();
        if (dissolve == null) {
            return 0;
        } else {
            return 1-dissolve;
        }
    }

    protected boolean isArrowDissolved() {
        Float dissolveArrow = animationState.getDissolveArrow();
        if (dissolveArrow == null) {
            return false;
        } else {
            return dissolveArrow == 0;
        }
    }

    public float getArrowDissolve() {
        Float dissolve = animationState.getDissolveArrow();
        if (dissolve == null) {
            return 0;
        } else {
            return 1-dissolve;
        }
    }

    @Override
    public boolean shouldRender() {
        return isInWorld();
    }

    protected Vector3f getJumpsPosition(float lifetime, float time) {
        Vector2f xy = getJumpsXY(lifetime);
        return new Vector3f(xy.x, xy.y, getJumpsZ(time));
    }

    protected Vector2f getJumpsXY(float lifetime) {
        float reverseSpawnTime = 1 - Math.abs(lifetime - 0.5f) * 2;
        float jumpTime = Easing.easeOutQuad(reverseSpawnTime);
        Vector2f grid = get2DPosition();
        grid.y = Math.lerp(doNoteGravity() ? -0.3f: grid.y - 0.3f, grid.y, jumpTime);
        return grid;
    }

    protected float getJumpsZ(float time) {
        float spawnPosition = getSpawnPosition();
        float jumpOutPosition = getJumpOutPosition();

        float spawnBeat = getSpawnBeat();
        float jumpOutBeat = getJumpOutBeat();

        // jumps
        if (time < spawnBeat) {
            // jump in
            float percent = (spawnBeat - time) / 2;
            return Math.lerp(spawnPosition, JUMP_FAR_Z, percent);
        } else if (time < jumpOutBeat) {
            // in between
            float percent = MathUtil.inverseLerp(spawnBeat, jumpOutBeat, time);
            return Math.lerp(spawnPosition, jumpOutPosition, percent);
        } else {
            // jump out
            float percent = MathUtil.inverseLerp(jumpOutBeat, getDespawnBeat(), time);
            percent *= percent; // bullshit parabola or something
            return Math.lerp(jumpOutPosition, -JUMP_FAR_Z, percent);
        }
    }

    protected Vector2f get2DPosition() {
        float x = (this.getData().getX() - 1.5f) * 0.6f;
        float y = (this.getData().getY()) * 0.6f;
        return new Vector2f(x, y);
    }

    protected float getLifetime(float beat) {
        float lifetime = MathUtil.inverseLerp(getSpawnBeat(), getDespawnBeat(), beat);
        return MathUtil.clamp01(lifetime);
    }

    protected float getSpawnLifetime(float lifetime) {
        return MathUtil.clamp01(lifetime * 2);
    }

    // Converts world space to the object's local space
    protected Matrix4f getMatrix(float time, AnimationState animationState) {
        Matrix4f m = new Matrix4f();

        Matrix4f parentMatrix = data.getTrackContainer().tryGetParentMatrix();
        if (parentMatrix != null) {
            m.mul(parentMatrix);
        }

        m.translate(getPlayerHeadPosition().x, 0, getPlayerHeadPosition().z);

        if (data.getWorldRotation() != null) {
            m.rotate(data.getWorldRotation());
        }

        if (animationState.getOffsetWorldRotation() != null) {
            m.rotate(animationState.getOffsetWorldRotation());
        }

        if (animationState.getOffsetPosition() != null) {
            m.translate(animationState.getOffsetPosition());
        }

        if (getLaneRotation() != null) {
            m.rotate(getLaneRotation());
        }

        applySpawnMatrix(time, m, animationState);

        MathUtil.reflectMatrixAcrossX(m); // Transform matrix from Beat Saber world space to Minecraft world space

        return m;
    }

    protected boolean doNoteLook() {
        return false;
    }
    protected boolean doNoteGravity() {
        return true;
    }

    protected void applySpawnMatrix(float time, Matrix4f m, AnimationState animationState) {
        float lifetime = getLifetime(time);
        float spawnLifetime = getSpawnLifetime(lifetime);

        Matrix4f jumpMatrix = new Matrix4f();

        Vector3f v = getJumpsPosition(lifetime, time);

        if (animationState.getDefinitePosition() != null) {
            v.mul(1, 1, 0);
            var v2 = animationState.getDefinitePosition();
            v.add(v2);
        }

        jumpMatrix.translate(v);

        m.translate(WORLD_OFFSET);

        if (doNoteLook()) {
            if (lifetime < 0.5) {
                Vector3f headPosition = getPlayerHeadPosition();
                headPosition = MathUtil.matrixTransformPoint3D(new Matrix4f(m).invert(), headPosition);
                headPosition = MathUtil.matrixTransformPoint3D(jumpMatrix, headPosition.mul(-1));
                Vector3f up = new Vector3f(0.0f, 0, 1);
                Quaternionf targetLookRotation = new Quaternionf().rotateTo(up, headPosition);
                lookRotation = new Quaternionf().slerp(targetLookRotation, spawnLifetime);
            }

            m.mul(jumpMatrix).rotate(lookRotation);
        } else {
            m.mul(jumpMatrix);
        }

        Vector3f animatedScale = animationState.getScale();
        if (animatedScale != null) {
            m.scale(animatedScale);
        }

        if (data.getLocalRotation() != null) {
            m.rotate(data.getLocalRotation());
        }

        if (animationState.getLocalRotation() != null) {
            m.rotate(animationState.getLocalRotation());
        }

        if (lifetime < 0.5) {
            m.rotate(getJumpsRotation(spawnLifetime));
        }
        else {
            m.rotate(baseRotation);
        }
    }

    protected Quaternionf getJumpsRotation(float spawnLifetime) {
        float rotationLifetime = MathUtil.clamp01(spawnLifetime / 0.3f);
        if (spawnLifetime == 0) {
            return baseRotation;
        }
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        return new Quaternionf().set(spawnQuaternion).slerp(baseRotation, rotationTime);
    }

    protected boolean jumpEnded(float beat) {
        return beat >= getDespawnBeat();
    }

    protected boolean pastBeat(float beat) {
        return beat > getData().getBeat()+0.25f;
    }

    private void applyMatrixToRender(Matrix4f matrix, PoseStack matrices) {
        Matrix3f normalMatrix = new Matrix3f();
        matrix.get3x3(normalMatrix);
        matrices.mulPose(matrix);
        matrices.last().normal().mul(normalMatrix);
    }

    private float applyTimeRemapping(float beat, AnimationState animatedPropertyState) {
        float spawnBeat = getSpawnBeat();
        float despawnBeat = getDespawnBeat();
        Float animationTime = animatedPropertyState.getTime();

        if (beat >= spawnBeat && animationTime != null) {
            return Math.lerp(spawnBeat, despawnBeat, animationTime);
        } else {
            return beat;
        }
    }

    private AnimationState getObjectPathAnimationState(float lifetime) {
        return data.getPathAnimation().toState(lifetime);
    }

    private AnimationState getTrackPathAnimationState(float lifetime) {
        return data.getTrackContainer().getAnimatedPathState(lifetime);
    }


    private static final Vector3f DEFAULT_MODEL_OFFSET = new Vector3f(-0.5f);
    protected Vector3f getModelOffset() {
        return DEFAULT_MODEL_OFFSET;
    }

    @Override
    protected void worldRender(PoseStack matrices, float alpha) {
        applyMatrixToRender(matrix, matrices);

        matrices.last().pose().getTranslation(worldPos)
            .add(mc.gameRenderer.getMainCamera().getPosition().toVector3f());
        matrices.last().pose().getUnnormalizedRotation(worldRot);

        matrices.scale(SIZE_SCALAR, SIZE_SCALAR, SIZE_SCALAR);
        var v = getModelOffset();
        matrices.translate(v.x, v.y, v.z);


        mapController.checkNote(this);

        objectRender(matrices, animationState, alpha);
    }

    abstract protected void objectRender(PoseStack matrices, AnimationState animationState, float alpha);

    public T getData() {
        return data;
    }

    public Quaternionf getLaneRotation() {
        return laneRotation;
    }

    public void setLaneRotation(Quaternionf laneRotation) {
        this.laneRotation = laneRotation;
    }

    public float getCollisionDistance() {
        return 0;
    }

    public Vector3f getWorldPos() {
        return worldPos;
    }

    public Quaternionf getWorldRot() {
        return worldRot;
    }

    public Hitbox getGoodCutBounds() {
        return new Hitbox(new Vector3f(), new Vector3f());
    }

    public Hitbox getBadCutBounds() {
        return new Hitbox(new Vector3f(), new Vector3f());
    }

    public Hitbox getAccurateHitbox() {
        return new Hitbox(new Vector3f(), new Vector3f());
    }

    public void cutNote() {
        this.despawn();
    }

    public ScoreState getScoreState() {
        return scoreState;
    }

    public void setScoreState(ScoreState state) {
        this.scoreState = state;
    }

    @Nullable
    public NoteType getContactColor() {
        return this.contactColor;
    }

    public void setContactColor(NoteType color) {
        contactColor = color;
    }

    public InstancedMesh<ColorNoteInstanceData> getMesh() {
        return null;
    }


    public void spawnDebris(Vector3f notePos, Quaternionf noteOrientation, Color color, Vector3f planeIncident, Vector3f planeNormal) {

        if (BeatcraftClient.playerConfig.preferences.reducedDebris) return;

        var m = getMesh();
        if (m == null) return;

        float d = planeNormal.normalize(new Vector3f()).dot(planeIncident);

        var slice = new Vector4f(planeNormal, d);
        var slice2 = new Vector4f(planeNormal.negate(), d);

        float velocity = -mapController.difficulty.getSetDifficulty().getNjs(mapController.currentBeat);

        Debris left = new Debris(
            mapController,
            new Vector3f(notePos),
            new Quaternionf(noteOrientation),
            new Vector3f(0f, 0, velocity).add(planeNormal.mul(2f, new Vector3f())).rotate(laneRotation.invert(new Quaternionf())),
            new Quaternionf().rotateY(-0.02f).rotateX(-0.03f),
            slice, color, m
        );

        Debris right = new Debris(
            mapController,
            new Vector3f(notePos),
            new Quaternionf(noteOrientation),
            new Vector3f(0f, 0, velocity).add(planeNormal.mul(-2f, new Vector3f())).rotate(laneRotation.invert(new Quaternionf())),
            new Quaternionf().rotateY(0.02f).rotateX(-0.03f),
            slice2, color, m
        );

        BeatcraftParticleRenderer.addParticle(left);
        BeatcraftParticleRenderer.addParticle(right);

    }

}
