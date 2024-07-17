package com.beatcraft.render;

import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.GameplayObject;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;
import org.joml.Math;

public abstract class PhysicalGameplayObject<T extends GameplayObject> extends WorldRenderer {
    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 0.4f;
    protected static final float SIZE_SCALAR = 0.5f;
    private final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    protected Quaternionf baseRotation = new Quaternionf();
    private Quaternionf laneRotation;
    protected T data;
    protected NoteMath.Jumps jumps;

    PhysicalGameplayObject(T data) {
        this.data = data;
        this.jumps = NoteMath.getJumps(data.getNjs(), data.getOffset(), BeatmapPlayer.currentInfo.getBpm());
    }

    public float getSpawnBeat() {
        return getData().getBeat() - jumps.halfDuration();
    }

    public float getDespawnBeat() {
        return getData().getBeat() + jumps.halfDuration();
    }

    @Override
    public boolean shouldRender() {
        float margin = MathUtil.secondsToBeats(JUMP_SECONDS, BeatmapPlayer.currentInfo.getBpm());
        boolean isAboveSpawnBeat = BeatmapPlayer.getCurrentBeat() >= getSpawnBeat() - margin;
        boolean isBelowDespawnBeat = BeatmapPlayer.getCurrentBeat() <= getDespawnBeat() + margin;
        return isAboveSpawnBeat && isBelowDespawnBeat;
    }

    protected Vector3f getJumpsPosition(float spawnLifetime, float time) {
        Vector2f xy = getJumpsXY(spawnLifetime);
        return new Vector3f(xy.x, xy.y, getJumpsZ(time));
    }

    protected Vector2f getJumpsXY(float spawnLifetime) {
        float jumpTime = Easing.easeOutQuad(spawnLifetime);
        Vector2f grid = get2DPosition();
        grid.y = Math.lerp(1.1f, grid.y + 1.1f, jumpTime);
        return grid;
    }

    protected float getJumpsZ(float time) {
        float spawnPosition = jumps.jumpDistance() / 2;
        float despawnPosition = -spawnPosition;

        float spawnBeat = getSpawnBeat();
        float despawnBeat = getDespawnBeat();

        // jumps
        if (time < spawnBeat) {
            // jump in
            float percent = (spawnBeat - time) / 2;
            return Math.lerp(spawnPosition, JUMP_FAR_Z, percent);
        } else if (time > despawnBeat) {
            // jump out
            float percent = (time - despawnBeat) / 2;
            return Math.lerp(despawnPosition, -JUMP_FAR_Z, percent);
        } else {
            // in between
            float percent = (time - spawnBeat) / (despawnBeat - spawnBeat);
            return Math.lerp(spawnPosition, despawnPosition, percent);
        }
    }

    protected Vector2f get2DPosition() {
        float x = (this.getData().getX() - 1.5f) * 0.6f;
        float y = (this.getData().getY()) * 0.6f;
        return new Vector2f(x, y);
    }

    protected float getLifetime(float time) {
        float lifetime = MathUtil.inverseLerp(getSpawnBeat(), getDespawnBeat(), time);
        return MathUtil.clamp01(lifetime);
    }

    protected float getSpawnLifetime(float lifetime) {
        return MathUtil.clamp01(lifetime * 2);
    }

    // Converts world space to the object's local space
    protected Matrix4f getMatrix(float time, AnimationState animationState) {
        Matrix4f m = new Matrix4f();

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

    protected void applySpawnMatrix(float time, Matrix4f m, AnimationState animationState) {
        float lifetime = getLifetime(time);
        float spawnLifetime = getSpawnLifetime(lifetime);

        Matrix4f jumpMatrix = new Matrix4f();

        Vector3f v = getJumpsPosition(spawnLifetime, time);
        jumpMatrix.translate(v);

        if (doNoteLook()) {
            Vector3f headPosition = new Vector3f(0, 1, 0);
            headPosition = MathUtil.matrixTransformPoint3D(new Matrix4f(m).invert(), headPosition);
            headPosition = MathUtil.matrixTransformPoint3D(jumpMatrix, headPosition.mul(-1));
            Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
            Matrix4f lookRotation = new Matrix4f().rotateTowards(headPosition, up);

            m.mul(jumpMatrix).mul(lookRotation);
        } else {
            m.mul(jumpMatrix);
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
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        return new Quaternionf().set(spawnQuaternion).slerp(baseRotation, rotationTime);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        float beat = BeatmapPlayer.getCurrentBeat();
        AnimationState animationState = data.getTrackContainer().getAnimationState();
        Matrix4f matrix = getMatrix(beat, animationState);

        Matrix3f normalMatrix = new Matrix3f();
        matrix.get3x3(normalMatrix);
        matrices.multiplyPositionMatrix(matrix);
        matrices.peek().getNormalMatrix().mul(normalMatrix);

        matrices.scale(SIZE_SCALAR, SIZE_SCALAR, SIZE_SCALAR);
        matrices.translate(-0.5, -0.5, -0.5);

        objectRender(matrices, vertexConsumer, animationState);
    }

    abstract protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState);

    public T getData() {
        return data;
    }

    public Quaternionf getLaneRotation() {
        return laneRotation;
    }

    public void setLaneRotation(Quaternionf laneRotation) {
        this.laneRotation = laneRotation;
    }
}
