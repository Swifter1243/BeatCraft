package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.object.Arc;
import com.beatcraft.data.types.BezierPath;
import com.beatcraft.data.types.ISplinePath;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PhysicalArc extends PhysicalGameplayObject<Arc> {

    BezierPath basePath;
    private int segments = 50;

    public PhysicalArc(Arc data) {
        super(data);
        buildBasePath(new Vector3f(1, 1, 1));
    }

    public void buildBasePath(Vector3f modifier) {

        ArrayList<Vector3f> points = new ArrayList<>();

        float startX = (-data.getX()) * 0.6f + 0.9f;
        float startY = data.getY() * 0.6f + 0.8f;

        Vector3f start = new Vector3f(
            0,
            0,
            0
        );

        Vector3f startC = Arc.cutDirectionToControlPoint(data.getHeadCutDirection()).mul(data.getHeadMagnitude()).mul(2).add(start);

        Vector3f end = new Vector3f(
            ((-data.getTailX()) * 0.6f + 0.9f) - startX,
            (data.getTailY() * 0.6f + 0.8f) - startY,
            -(data.getTailBeat() - data.getBeat())
        );

        Vector3f endC = Arc.cutDirectionToControlPoint(data.getTailCutDirection()).mul(data.getTailMagnitude()).mul(-2, -2, -2).add(end);

        Vector3f midpoint = MathUtil.lerpVector3(startC, endC, 0.5f);

        boolean inline = data.getX() == data.getTailX() &&
            data.getY() == data.getTailY() &&
            (
                data.getHeadCutDirection() == data.getTailCutDirection() ||
                    data.getHeadCutDirection() == data.getTailCutDirection().opposite()
            );

        float radians = 0;
        if (inline) {
            if (data.getMidAnchorMode() == Arc.MidAnchorMode.CLOCKWISE) {
                radians = (float) (-Math.PI / 2d);
            } else if (data.getMidAnchorMode() == Arc.MidAnchorMode.COUNTER_CLOCKWISE) {
                radians = (float) (Math.PI / 2d);
            }
        }

        float deg = -NoteMath.degreesFromCut(data.getHeadCutDirection());

        Vector3f midpointRotation = new Vector3f(0, -1, 0).rotateZ((deg * MathHelper.RADIANS_PER_DEGREE) + radians);

        midpoint.add(midpointRotation.mul(1.2f));

        start.mul(modifier);
        startC.mul(modifier);
        midpoint.mul(modifier);
        endC.mul(modifier);
        end.mul(modifier);

        points.add(start);
        points.add(startC);
        points.add(midpoint);
        points.add(endC);
        points.add(end);

        basePath = new BezierPath(points);
        segments = (int) (start.distance(end) * 5);

    }

    protected void updateCurve() {
        float beatSpacing = data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        buildBasePath(new Vector3f(1, 1, -beatSpacing));
    }

    @Override
    protected boolean doNoteLook() {
        return false;
    }

    @Override
    protected boolean doNoteGravity() {
        return false;
    }

    @Override
    protected Quaternionf getJumpsRotation(float spawnLifetime) {
        return new Quaternionf();
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {

        updateCurve();

        var localPos = matrices.peek().getPositionMatrix().getTranslation(MemoryPool.newVector3f());
        var camPos = MemoryPool.newVector3f(mc.gameRenderer.getCamera().getPos());

        localPos.add(0.2f, 0.3f, 0.25f);

        render(basePath, localPos, data.getColor().toARGB());

        if (DebugRenderer.doDebugRendering && DebugRenderer.renderArcDebugLines) {
            var offset = MemoryPool.newVector3f(localPos).add(camPos);
            DebugRenderer.renderPath(basePath, offset, segments, data.getColor().toARGB());
            MemoryPool.release(offset);
        }
        MemoryPool.release(localPos, camPos);
    }

    @Override
    protected Vector2f getJumpsXY(float lifetime) {
        float reverseSpawnTime = 1 - org.joml.Math.abs(lifetime - 0.5f) * 2;
        float jumpTime = Easing.easeOutQuad(reverseSpawnTime);
        Vector2f grid = get2DPosition();
        grid.y = org.joml.Math.lerp(doNoteGravity() ? -0.3f: grid.y, grid.y, jumpTime);
        return grid;
    }

    @Override
    public float getJumpOutPosition() {
        float length = this.data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());
        return -(length * (data.getTailBeat() - data.getBeat()));
    }

    @Override
    public float getJumpOutBeat() {
        return data.getTailBeat();
    }

    @Override
    public float getDespawnBeat() {
        return data.getTailBeat() + data.getJumps().halfDuration();
    }

    public void render(ISplinePath path, Vector3f origin, int color) {

        var o = MemoryPool.newVector3f(origin);
        var o2 = MemoryPool.newVector3f(origin);
        var q = MemoryPool.newQuaternionf();

        BeatCraftRenderer.recordArcRenderCall((b, c) -> _render(b, path, o, color, q));
        BeatCraftRenderer.bloomfog.recordBloomCall((b, c, r) -> _render(b, path, o2, color, MemoryPool.newQuaternionf(r)));
    }

    public void _render(BufferBuilder buffer, ISplinePath path, Vector3f origin, int color, Quaternionf cameraRotation) {

        int segments = 35;

        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(origin).rotate(cameraRotation);
            Vector3f p2 = path.evaluate(f2).add(origin).rotate(cameraRotation);
            Vector3f t = path.getTangent(f).rotate(cameraRotation);
            Vector3f t2 = path.getTangent(f2).rotate(cameraRotation);


            var h1 = MathUtil.generateCircle(t, 0.075f, 6, p);
            var h2 = MathUtil.generateCircle(t2, 0.075f, 6, p2);

            Vector3f[] q1 = new Vector3f[]{
                h1[0], h1[3],
                h2[3], h2[0]
            };

            Vector3f[] q2 = new Vector3f[]{
                h1[1], h1[4],
                h2[4], h2[1]
            };

            Vector3f[] q3 = new Vector3f[]{
                h1[2], h1[5],
                h2[5], h2[2]
            };

            float dist = p.length();

            //MemoryPool.release(p, p2, t, t2);

            int fade = (int) (Math.clamp((12f - dist) / 9f, 0f, 1f) * 127f) << 24;

            if (fade == 0) {
                continue;
            }

            int col = (color + fade);

            buffer.vertex(q1[0]).color(col);
            buffer.vertex(q1[1]).color(col);
            buffer.vertex(q1[2]).color(col);

            buffer.vertex(q1[0]).color(col);
            buffer.vertex(q1[2]).color(col);
            buffer.vertex(q1[3]).color(col);

            buffer.vertex(q2[0]).color(col);
            buffer.vertex(q2[1]).color(col);
            buffer.vertex(q2[2]).color(col);

            buffer.vertex(q2[0]).color(col);
            buffer.vertex(q2[2]).color(col);
            buffer.vertex(q2[3]).color(col);

            buffer.vertex(q3[0]).color(col);
            buffer.vertex(q3[1]).color(col);
            buffer.vertex(q3[2]).color(col);

            buffer.vertex(q3[0]).color(col);
            buffer.vertex(q3[2]).color(col);
            buffer.vertex(q3[3]).color(col);

            MemoryPool.releaseSafe(h1);
            MemoryPool.releaseSafe(h2);

        }

        MemoryPool.releaseSafe(origin);
        MemoryPool.releaseSafe(cameraRotation);

    }

    @Override
    public void seek(float beat) {
        super.seek(beat);
    }
}
