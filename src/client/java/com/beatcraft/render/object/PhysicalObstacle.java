package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.mixin_utils.BufferBuilderAccessible;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
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
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {
        var localPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        var camPos = mc.gameRenderer.getCamera().getPos();
        localPos.x = (-data.getX()) * 0.6f + 0.9f;
        localPos.y = (data.getY() * 0.6f + 0.25f);
        localPos.add(0, 0, (float) camPos.z);
        updateBounds();
        GameLogicHandler.checkObstacle(this, localPos, new Quaternionf());

        render(localPos, new Quaternionf());

        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getObstacleColor().toARGB();

        DebugRenderer.renderHitbox(bounds, localPos, new Quaternionf(), color, true, 6);
        DebugRenderer.renderHitbox(bounds, localPos, new Quaternionf(), 0xFFFFFF, true);
    }

    private void render(Vector3f pos, Quaternionf orientation) {
        BeatcraftRenderer.recordRenderCall(
            () -> _render(pos, orientation)
        );
    }
    private void _render(Vector3f pos, Quaternionf orientation) {
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
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        buff.sortQuads(((BufferBuilderAccessible) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

    }

    private void updateBounds() {
        bounds.min.x = -((data.getWidth() * 0.6f) - 0.3f);
        bounds.max.y = (data.getHeight() * 0.6f);

        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        bounds.max.z = data.getDuration() * length;
    }

    @Override
    public float getJumpOutPosition() {
        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());
        return -(length * data.getDuration());
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
