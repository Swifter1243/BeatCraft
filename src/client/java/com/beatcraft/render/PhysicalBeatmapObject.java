package com.beatcraft.render;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.GameplayObject;
import com.beatcraft.math.GenericMath;
import com.beatcraft.math.NoteMath;
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
    protected Vector3f position = new Vector3f();
    protected Quaternionf rotation = new Quaternionf();
    protected Vector3f scale = new Vector3f(1,1,1);

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
        float margin = GenericMath.secondsToBeats(JUMP_SECONDS, BeatmapPlayer.currentInfo.getBpm());
        boolean isAboveSpawnBeat = BeatmapPlayer.getCurrentBeat() >= getSpawnBeat() - margin;
        boolean isBelowDespawnBeat = BeatmapPlayer.getCurrentBeat() <= getDespawnBeat() + margin;
        return isAboveSpawnBeat && isBelowDespawnBeat;
    }

    public void updateTime(float time) {
        doSpawnAnimation(time);
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
        float lifetime = GenericMath.inverseLerp(getSpawnBeat(), getDespawnBeat(), time);
        return GenericMath.clamp01(lifetime);
    }

    protected float getSpawnLifetime(float lifetime) {
        return GenericMath.clamp01(1 - ((0.5f - lifetime) * 2));
    }

    protected void doSpawnAnimation(float time) {
        float lifetime = getLifetime(time);
        float spawnLifetime = getSpawnLifetime(lifetime);

        position = getJumpsPosition(spawnLifetime, time);

        if (lifetime < 0.5) {
            rotation = getJumpsRotation(spawnLifetime);
        }
        else {
            rotation = baseRotation;
        }
    }

    protected Quaternionf getJumpsRotation(float spawnLifetime) {
        float rotationLifetime = GenericMath.clamp01(spawnLifetime / 0.3f);
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        return new Quaternionf().set(spawnQuaternion).slerp(baseRotation, rotationTime);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        if (!shouldRender()) return;

        updateTime(BeatmapPlayer.getCurrentBeat());
        matrices.translate(position.x, position.y, position.z);
        matrices.scale(scale.x * SIZE_SCALAR, scale.y * SIZE_SCALAR, scale.z * SIZE_SCALAR);
        matrices.multiply(rotation);
        matrices.translate(-0.5, -0.5, -0.5);

        objectRender(matrices, vertexConsumer);
    }

    abstract protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer);

    public T getData() {
        return data;
    }
}
