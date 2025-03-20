package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
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
        renderMirrored(localPos, rotation);

        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getObstacleColor().toARGB();

        ObstacleGlowRenderer.render(localPos, rotation, bounds, color);
        ObstacleGlowRenderer.renderMirrored(localPos, rotation, bounds, color);

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
        BeatcraftRenderer.recordObstacleRenderCall(
            (b, c, i) -> _render(b, c, i, pos, orientation, false)
        );
    }

    private void renderMirrored(Vector3f pos, Quaternionf orientation) {
        var flippedPos = pos.mul(1, -1, 1, new Vector3f());
        var flippedRot = new Quaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);

        MirrorHandler.recordMirroredObstacleRenderCall((b, c, i) -> _render(b, c, i, flippedPos, flippedRot, true));
    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, int color, Vector3f pos, Quaternionf orientation, boolean mirrored) {
        List<Vector3f[]> faces = BeatcraftRenderer.getCubeFaces(bounds.min, bounds.max);
        for (Vector3f[] face : faces) {
            var c1 = face[0].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(pos).sub(cameraPos);
            var c2 = face[1].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(pos).sub(cameraPos);
            var c3 = face[2].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(pos).sub(cameraPos);
            var c4 = face[3].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(pos).sub(cameraPos);

            buffer.vertex(c1.x, c1.y, c1.z).color(color);
            buffer.vertex(c2.x, c2.y, c2.z).color(color);
            buffer.vertex(c3.x, c3.y, c3.z).color(color);
            buffer.vertex(c4.x, c4.y, c4.z).color(color);
        }
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
