package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
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

    public PhysicalObstacle(Obstacle data) {
        super(data);
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
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek().getPositionMatrix().getTranslation(MemoryPool.newVector3f());
        var rotation = matrices.peek().getPositionMatrix().getUnnormalizedRotation(MemoryPool.newQuaternionf());
        var scale = matrices.peek().getPositionMatrix().getScale(MemoryPool.newVector3f());


        updateBounds(scale);


        var camPos = MemoryPool.newVector3f(mc.gameRenderer.getCamera().getPos());
        localPos.add(camPos);
        MemoryPool.release(camPos);
        GameLogicHandler.checkObstacle(this, localPos, rotation);

        render(MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation));
        renderMirrored(MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation));



        ObstacleGlowRenderer.render(MemoryPool.newVector3f(localPos), MemoryPool.newQuaternionf(rotation), bounds, data.getColor());
        ObstacleGlowRenderer.renderMirrored(localPos, rotation, bounds, data.getColor());

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
        BeatCraftRenderer.recordObstacleRenderCall(
            (b, c, i) -> _render(b, c, i, pos, orientation, false)
        );
    }
    private void _render(Vector3f pos, Quaternionf orientation) {
        if (BeatmapPlayer.currentBeatmap == null) return;
        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);



    private void renderMirrored(Vector3f pos, Quaternionf orientation) {
        var flippedPos = pos.mul(1, -1, 1);
        var flippedRot = MemoryPool.newQuaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);
        MemoryPool.release(orientation);
        MirrorHandler.recordMirroredObstacleRenderCall((b, c, i) -> _render(b, c, i, flippedPos, flippedRot, true));
    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, int _color, Vector3f pos, Quaternionf orientation, boolean mirrored) {
        List<Vector3f[]> faces = BeatCraftRenderer.getCubeFaces(bounds.min, bounds.max);
        var color = this.data.getColor();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        ObstacleGlowRenderer.grabScreen();

        var scene = ObstacleGlowRenderer.framebuffer;//MinecraftClient.getInstance().getFramebuffer();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.setShader(() -> ObstacleGlowRenderer.distortionShader);
        RenderSystem.setShaderTexture(0, scene.getColorAttachment());
        var c1 = MemoryPool.newVector3f();
        var c2 = MemoryPool.newVector3f();
        var c3 = MemoryPool.newVector3f();
        var c4 = MemoryPool.newVector3f();

        for (Vector3f[] face : faces) {
            c1.set(face[0]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c2.set(face[1]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c3.set(face[2]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);
            c4.set(face[3]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(pos).sub(cameraPos);

            buffer.vertex(c1.x, c1.y, c1.z).color(color).texture(0, 0);
            buffer.vertex(c2.x, c2.y, c2.z).color(color).texture(0, 1);
            buffer.vertex(c3.x, c3.y, c3.z).color(color).texture(1, 1);
            buffer.vertex(c4.x, c4.y, c4.z).color(color).texture(1, 0);

        }
        MemoryPool.release(c1, c2, c3, c4);
        MemoryPool.release(pos);
        MemoryPool.release(orientation);
    }

    @Override
    protected Vector2f getJumpsXY(float lifetime) {
        float reverseSpawnTime = 1 - org.joml.Math.abs(lifetime - 0.5f) * 2;
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

        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        bounds.max.z = data.getLength(length);
    }

    @Override
    public float getJumpOutPosition() {
        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());
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
