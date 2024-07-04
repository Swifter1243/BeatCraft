package com.beatcraft.render;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.BeatmapCalculations;
import com.beatcraft.beatmap.BeatmapPlayer;
import com.beatcraft.math.GenericMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class PhysicalBeatmapObject extends WorldRenderer {
    private static final float JUMP_FAR_Z = 500;
    private static final float JUMP_SECONDS = 2;
    private final Quaternionf spawnQuaternion = SpawnQuaternionPool.getRandomQuaternion();
    public float beat;
    public float njs;
    public float offset;
    BeatmapCalculations.Jumps jumps;
    public Vector3f position = new Vector3f();
    public Quaternionf rotation = new Quaternionf();
    public Vector3f scale = new Vector3f(1,1,1);

    PhysicalBeatmapObject(float beat, float njs, float offset) {
        this.beat = beat;
        this.njs = njs;
        this.offset = offset;
        this.jumps = BeatmapCalculations.getJumps(njs, offset, BeatmapPlayer.bpm);
    }

    public float getSpawnBeat() {
        return beat - jumps.halfDuration();
    }

    public float getDespawnBeat() {
        return beat + jumps.halfDuration();
    }

    public boolean shouldRender() {
        float margin = BeatmapCalculations.secondsToBeats(JUMP_SECONDS, BeatmapPlayer.bpm);
        boolean isAboveSpawnBeat = BeatmapPlayer.beat >= getSpawnBeat() - margin;
        boolean isBelowDespawnBeat = BeatmapPlayer.beat <= getDespawnBeat() + margin;
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

    protected void doSpawnAnimation(float time) {
        float lifetime = GenericMath.clamp01(GenericMath.inverseLerp(getDespawnBeat(), getSpawnBeat(), time));
        float spawnLifetime = GenericMath.clamp01(1 - ((lifetime - 0.5f) * 2));
        float jumpTime = Easing.easeOutQuad(spawnLifetime);

        float noteY = 1; // temp
        position.y = Math.lerp(1.1f, noteY + 1.1f, jumpTime);

        if (lifetime > 0.5) {
            doSpawnRotation(spawnLifetime);
        }
    }

    protected void doSpawnRotation(float spawnLifetime) {
        float rotationLifetime = GenericMath.clamp01(spawnLifetime / 0.3f);
        float rotationTime = Easing.easeOutQuad(rotationLifetime);
        rotation = spawnQuaternion.slerp(new Quaternionf(), rotationTime);
    }

    @Override
    protected void worldRender(MatrixStack matrices, VertexConsumer vertexConsumer) {
        if (!shouldRender()) return;

        updateTime(BeatmapPlayer.beat);
        matrices.translate(position.x, position.y, position.z);
        matrices.scale(scale.x * 0.6f, scale.y * 0.6f, scale.z * 0.6f);
        matrices.multiply(rotation);
        matrices.translate(-0.5, -0.5, -0.5);

        objectRender(matrices, vertexConsumer);
    }

    abstract protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer);
}
