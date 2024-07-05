package com.beatcraft.render;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.GameplayObject;
import com.beatcraft.math.GenericMath;
import com.beatcraft.math.NoteMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public abstract class PhysicalBeatmapObject<T extends GameplayObject> extends WorldRenderer {
    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 2;
    private final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    protected Quaternionf baseRotation = new Quaternionf();
    public T data;
    NoteMath.Jumps jumps;
    public Vector3f position = new Vector3f();
    public Quaternionf rotation = new Quaternionf();
    public Vector3f scale = new Vector3f(1,1,1);

    PhysicalBeatmapObject(T data) {
        this.data = data;
        this.jumps = NoteMath.getJumps(data.njs, data.offset, BeatmapPlayer.currentInfo.bpm);
    }

    public float getSpawnBeat() {
        return data.beat - jumps.halfDuration();
    }

    public float getDespawnBeat() {
        return data.beat + jumps.halfDuration();
    }

    public boolean shouldRender() {
        float margin = GenericMath.secondsToBeats(JUMP_SECONDS, BeatmapPlayer.currentInfo.bpm);
        boolean isAboveSpawnBeat = BeatmapPlayer.currentBeat >= getSpawnBeat() - margin;
        boolean isBelowDespawnBeat = BeatmapPlayer.currentBeat <= getDespawnBeat() + margin;
        return isAboveSpawnBeat && isBelowDespawnBeat;
    }

    public void updateTime(float time) {
        doJumpsPosition(time);
        doSpawnAnimation(time);
    }

    protected void doJumpsPosition(float time) {
        float spawnPosition = jumps.jumpDistance() / 2;
        float despawnPosition = -spawnPosition;

        float spawnBeat = getSpawnBeat();
        float despawnBeat = getDespawnBeat();

        // jumps
        if (time < spawnBeat) {
            // jump in
            float percent = (spawnBeat - time) / 2;
            position.z = Math.lerp(spawnPosition, JUMP_FAR_Z, percent);
        } else if (time > despawnBeat) {
            // jump out
            float percent = (time - despawnBeat) / 2;
            position.z = Math.lerp(despawnPosition, -JUMP_FAR_Z, percent);
        } else {
            // in between
            float percent = (time - spawnBeat) / (despawnBeat - spawnBeat);
            position.z = Math.lerp(spawnPosition, despawnPosition, percent);
        }
    }

    protected Vector2f get2DPosition() {
        return new Vector2f(
                (this.data.x - 1.5f) * 0.6f * -1,
                (this.data.y) * 0.6f
        );
    }

    protected void doSpawnAnimation(float time) {
        float lifetime = GenericMath.clamp01(GenericMath.inverseLerp(getDespawnBeat(), getSpawnBeat(), time));
        float spawnLifetime = GenericMath.clamp01(1 - ((lifetime - 0.5f) * 2));
        float jumpTime = Easing.easeOutQuad(spawnLifetime);

        Vector2f grid = get2DPosition();
        position.x = grid.x;
        position.y = Math.lerp(1.1f, grid.y + 1.1f, jumpTime);

        if (lifetime > 0.5) {
            doSpawnRotation(spawnLifetime);
        }
    }

    protected void doSpawnRotation(float spawnLifetime) {
        float rotationLifetime = GenericMath.clamp01(spawnLifetime / 0.3f);
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        rotation = new Quaternionf().set(spawnQuaternion).slerp(baseRotation, rotationTime);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        if (!shouldRender()) return;

        updateTime(BeatmapPlayer.currentBeat);
        matrices.translate(position.x, position.y, position.z);
        matrices.scale(scale.x * 0.6f, scale.y * 0.6f, scale.z * 0.6f);
        matrices.multiply(rotation);
        matrices.multiply(new Quaternionf().rotateZ(Math.toRadians(90))); // TODO: TEMPORARY MEASURE, REMOVE LATER.
        matrices.translate(-0.5, -0.5, -0.5);

        objectRender(matrices, vertexConsumer);
    }

    abstract protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer);
}
