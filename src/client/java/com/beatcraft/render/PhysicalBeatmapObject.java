package com.beatcraft.render;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.GameplayObject;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;
import org.joml.Math;

public abstract class PhysicalBeatmapObject<T extends GameplayObject> extends WorldRenderer {
    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 0.4f;
    protected static final float SIZE_SCALAR = 0.5f;
    private final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    protected Quaternionf baseRotation = new Quaternionf();
    protected T data;
    protected NoteMath.Jumps jumps;

    PhysicalBeatmapObject(T data) {
        this.data = data;
        this.jumps = NoteMath.getJumps(data.getNjs(), data.getOffset(), BeatmapPlayer.currentInfo.getBpm());
    }

    public float getSpawnBeat() {
        return getData().getBeat() - jumps.halfDuration();
    }

    public float getDespawnBeat() {
        return getData().getBeat() + jumps.halfDuration();
    }

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
        float x = (this.getData().getX() - 1.5f) * 0.6f * -1;
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

    protected Matrix4f getMatrixAtTime(float time) {
        Matrix4f m = new Matrix4f();

        if (data.getWorldRotation() != null) {
            m.rotate(data.getWorldRotation());
        }

        m.mul(getSpawnMatrix(time));

        if (data.getLocalRotation() != null) {
            m.rotate(data.getLocalRotation());
        }

        return m;
    }

    protected Matrix4f getSpawnMatrix(float time) {
        float lifetime = getLifetime(time);
        float spawnLifetime = getSpawnLifetime(lifetime);

        Matrix4f m = new Matrix4f();
        Vector3f v = getJumpsPosition(spawnLifetime, time);
        m.translate(v);

        if (lifetime < 0.5) {
            m.rotate(getJumpsRotation(spawnLifetime));
        }
        else {
            m.rotate(baseRotation);
        }

        return m;
    }

    protected Quaternionf getJumpsRotation(float spawnLifetime) {
        float rotationLifetime = MathUtil.clamp01(spawnLifetime / 0.3f);
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        return new Quaternionf().set(spawnQuaternion).slerp(baseRotation, rotationTime);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        if (!shouldRender()) return;

        float time = BeatmapPlayer.getCurrentBeat();
        matrices.multiplyPositionMatrix(getMatrixAtTime(time));
        // TODO: Handle normal matrix???
        matrices.scale(SIZE_SCALAR, SIZE_SCALAR, SIZE_SCALAR);
        matrices.translate(-0.5, -0.5, -0.5);

        objectRender(matrices, vertexConsumer);
    }

    abstract protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer);

    public T getData() {
        return data;
    }
}
