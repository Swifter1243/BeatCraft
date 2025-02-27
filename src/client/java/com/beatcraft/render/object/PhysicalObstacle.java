package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
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

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        var rotation = matrices.peek().getPositionMatrix().getUnnormalizedRotation(new Quaternionf());
        updateBounds();


        var camPos = mc.gameRenderer.getCamera().getPos().toVector3f();
        localPos.add(camPos);
        GameLogicHandler.checkObstacle(this, localPos, rotation);

        render(localPos, rotation);

        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getObstacleColor().toARGB();

        ObstacleGlowRenderer.render(localPos, rotation, bounds, color);

        //DebugRenderer.renderHitbox(bounds, localPos, rotation, color, true, 6);
        //DebugRenderer.renderHitbox(bounds, localPos, rotation, 0xFFFFFF, true);
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
        BeatcraftRenderer.recordEarlyRenderCall(
            vcp -> _render(pos, orientation)
        );
    }
    private void _render(Vector3f pos, Quaternionf orientation) {
        if (BeatmapPlayer.currentBeatmap == null) return;
        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        List<Vector3f[]> faces = BeatcraftRenderer.getCubeFaces(bounds.min, bounds.max);

        for (Vector3f[] face : faces) {
            var c1 = face[0].rotate(orientation, new Vector3f()).add(pos).sub(cam);
            var c2 = face[1].rotate(orientation, new Vector3f()).add(pos).sub(cam);
            var c3 = face[2].rotate(orientation, new Vector3f()).add(pos).sub(cam);
            var c4 = face[3].rotate(orientation, new Vector3f()).add(pos).sub(cam);

            buffer.vertex(c1.x, c1.y, c1.z).color(color);
            buffer.vertex(c2.x, c2.y, c2.z).color(color);
            buffer.vertex(c3.x, c3.y, c3.z).color(color);
            buffer.vertex(c4.x, c4.y, c4.z).color(color);

        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

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
            data.getX() * 0.6f - 1.1f,
            data.getY() * 0.6f - 0.45f
        );
    }

    private void updateBounds() {
        bounds.min.x = -((data.getWidth() * 0.6f) - 0.3f);
        bounds.max.y = (data.getHeight() * 0.6f);

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
