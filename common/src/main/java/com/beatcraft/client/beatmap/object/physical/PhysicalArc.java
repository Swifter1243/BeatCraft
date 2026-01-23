package com.beatcraft.client.beatmap.object.physical;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.AnimationState;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.object.data.Arc;
import com.beatcraft.common.data.types.BezierPath;
import com.beatcraft.common.data.types.ISplinePath;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.DebugRenderer;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PhysicalArc extends PhysicalGameplayObject<Arc> {

    BezierPath basePath;
    private int segments = 50;

    public PhysicalArc(BeatmapController map, Arc data) {
        super(map, data);
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

        float radians = getRadians();

        float deg = -data.getHeadCutDirection().baseAngleDegrees;

        Vector3f midpointRotation = new Vector3f(0, -1, 0).rotateZ((deg * Mth.DEG_TO_RAD) + radians);

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

    private float getRadians() {
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
        return radians;
    }

    protected void updateCurve() {
        float beatSpacing = data.getNjs() * (60f / mapController.info.getBpm(data.getBeat()));

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
    protected void objectRender(PoseStack matrices, Camera camera, AnimationState animationState, float alpha) {

        updateCurve();

        var localPos = matrices.last().pose().transformPosition(new Vector3f(0.5f, 0.5f, 0.5f));
        var camPos = MemoryPool.newVector3f(mc.gameRenderer.getMainCamera().getPosition());

        localPos.sub(camPos);
        MemoryPool.release(localPos, camPos);

        render(matrices, basePath, localPos, data.getColor().toARGB());

        if (DebugRenderer.doDebugRendering && DebugRenderer.renderArcDebugLines) {
            DebugRenderer.renderPath(basePath, segments, data.getColor().copy().withAlpha(alpha).toARGB());
        }
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
        float length = this.data.getNjs() * (60f / mapController.info.getBpm(data.getBeat()));
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

    public void render(PoseStack matrices, ISplinePath path, Vector3f origin, int color) {

        var o = MemoryPool.newVector3f(origin);
        var o2 = MemoryPool.newVector3f(origin);
        var q = MemoryPool.newQuaternionf();
        var m = new Matrix4f(matrices.last().pose());

        mapController.recordArcRenderCall((b, c) -> _render(m, b, path, o, color, q));
        BeatcraftRenderer.bloomfog.recordBloomCall((b, c, r) -> _render(m, b, path, o2, color, MemoryPool.newQuaternionf(r)));
    }

    public void _render(Matrix4f transform, BufferBuilder buffer, ISplinePath path, Vector3f origin, int color, Quaternionf cameraRotation) {

        int segments = 35;

        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            var q = transform.getUnnormalizedRotation(new Quaternionf());
            Vector3f p = path.evaluate(f).rotate(q).add(origin).rotate(cameraRotation);
            Vector3f p2 = path.evaluate(f2).rotate(q).add(origin).rotate(cameraRotation);
            Vector3f t = path.getTangent(f).rotate(q).rotate(cameraRotation);
            Vector3f t2 = path.getTangent(f2).rotate(q).rotate(cameraRotation);


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

            buffer.addVertex(q1[0]).setColor(col);
            buffer.addVertex(q1[1]).setColor(col);
            buffer.addVertex(q1[2]).setColor(col);

            buffer.addVertex(q1[0]).setColor(col);
            buffer.addVertex(q1[2]).setColor(col);
            buffer.addVertex(q1[3]).setColor(col);

            buffer.addVertex(q2[0]).setColor(col);
            buffer.addVertex(q2[1]).setColor(col);
            buffer.addVertex(q2[2]).setColor(col);

            buffer.addVertex(q2[0]).setColor(col);
            buffer.addVertex(q2[2]).setColor(col);
            buffer.addVertex(q2[3]).setColor(col);

            buffer.addVertex(q3[0]).setColor(col);
            buffer.addVertex(q3[1]).setColor(col);
            buffer.addVertex(q3[2]).setColor(col);

            buffer.addVertex(q3[0]).setColor(col);
            buffer.addVertex(q3[2]).setColor(col);
            buffer.addVertex(q3[3]).setColor(col);

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
