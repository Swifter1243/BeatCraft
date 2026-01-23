package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.object.data.Obstacle;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.effect.ObstacleGlowRenderer;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class PhysicalObstacle extends PhysicalGameplayObject<Obstacle> {

    private final Hitbox bounds = new Hitbox(
        new Vector3f(-0.3f, 0, 0),
        new Vector3f(0.3f, 0, 0)
    );

    public PhysicalObstacle(BeatmapController map, Obstacle data) {
        super(map, data);
    }

    @Override
    protected Quaternionf getJumpsRotation(float spawnLifetime) {
        return new Quaternionf();
    }


    private static final Vector3f MODEL_OFFSET = new Vector3f();
    @Override
    protected Vector3f getModelOffset() {
        return MODEL_OFFSET;
    }

    @Override
    protected void objectRender(PoseStack matrices, Camera camera, AnimationState animationState, float alpha) {
        var localPos = matrices.last().pose().getTranslation(MemoryPool.newVector3f());
        var rotation = matrices.last().pose().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var scale = matrices.last().pose().getScale(MemoryPool.newVector3f());

        updateBounds(scale);

        var camPos = MemoryPool.newVector3f(mc.gameRenderer.getMainCamera().getPosition());
        // localPos.add(camPos);
        MemoryPool.release(camPos);
        mapController.checkObstacle(this, localPos, rotation);

        render(MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation));
        renderMirrored(MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation));

        ObstacleGlowRenderer.render(mapController, MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation), bounds, new Color(data.getColor()).withAlpha(alpha).toARGB());
        ObstacleGlowRenderer.renderMirrored(mapController, localPos, rotation, bounds, new Color(data.getColor()).withAlpha(alpha).toARGB());

    }

    @Override
    protected boolean doNoteLook() {
        return false;
    }

    @Override
    protected boolean doNoteGravity() {
        return false;
    }

    private void render(Vector3f pos, Quaternionf orientation) {
        mapController.recordObstacleRenderCall(
            (b, c, i) -> _render(b, c, i, pos, orientation, false)
        );
    }

    private void renderMirrored(Vector3f pos, Quaternionf orientation) {
        float mirrorY = mapController.worldPosition.y;

        var flippedPos = pos.mul(1, -1, 1);
        flippedPos.y += mirrorY * 2f;

        var flippedRot = MemoryPool.newQuaternionf(
            -orientation.x,
            orientation.y,
            -orientation.z,
            orientation.w
        );

        MemoryPool.release(orientation);

        mapController.recordMirroredObstacleRenderCall(
            (b, c, i) -> _render(b, c, i, flippedPos, flippedRot, true)
        );
    }


    private void _render(BufferBuilder buffer, Vector3f cameraPos, int _color, Vector3f pos, Quaternionf orientation, boolean mirrored) {
        List<Vector3f[]> faces = BeatcraftRenderer.getCubeFaces(bounds.min, bounds.max);
        var color = this.data.getColor();

        //BeatCraft.LOGGER.info("wall color: {}", new Color(color));

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);


        var c1 = MemoryPool.newVector3f();
        var c2 = MemoryPool.newVector3f();
        var c3 = MemoryPool.newVector3f();
        var c4 = MemoryPool.newVector3f();

        for (Vector3f[] face : faces) {
            c1.set(face[0]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c2.set(face[1]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c3.set(face[2]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c4.set(face[3]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);

            buffer.addVertex(c1.x, c1.y, c1.z).setColor(color).setUv(0, 0);
            buffer.addVertex(c2.x, c2.y, c2.z).setColor(color).setUv(0, 1);
            buffer.addVertex(c3.x, c3.y, c3.z).setColor(color).setUv(1, 1);
            buffer.addVertex(c4.x, c4.y, c4.z).setColor(color).setUv(1, 0);

        }
        MemoryPool.release(c1, c2, c3, c4);
        MemoryPool.release(pos);
        MemoryPool.release(orientation);
    }

    @Override
    protected Vector2f getJumpsXY(float lifetime) {
        float reverseSpawnTime = 1 - Math.abs(lifetime - 0.5f) * 2;
        float jumpTime = Easing.easeOutQuad(reverseSpawnTime);
        Vector2f grid = get2DPosition();
        grid.y = Math.lerp(doNoteGravity() ? -0.3f: grid.y, grid.y, jumpTime);
        return grid;
    }

    @Override
    protected Vector2f get2DPosition() {
        return new Vector2f(
            data.getX() * 0.6f - 0.9f,
            data.getY() * 0.6f - 0.6f
        );
    }

    private void updateBounds(Vector3f scale) {
        bounds.min.x = -(((data.getWidth()) * scale.x * 1.2f) - 0.3f);
        bounds.max.y = (data.getHeight() * scale.y * 1.2f);

        float length = this.data.getNjs() * (60f / mapController.getBpm(data.getBeat()));

        bounds.max.z = data.getLength(length);
    }

    @Override
    public float getJumpOutPosition() {
        float length = this.data.getNjs() * (60f / mapController.getBpm(data.getBeat()));
        return -(data.getLength(length));
    }

    @Override
    public float getJumpOutBeat() {
        return data.getBeat() + data.getDuration();
    }

    @Override
    public float getDespawnBeat() {
        return super.getDespawnBeat() + data.getDuration();
    }

    public Hitbox getBounds() {
        return bounds;
    }
}
