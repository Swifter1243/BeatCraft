package com.beatcraft.render.object;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.object.Arc;
import com.beatcraft.data.types.BezierPath;
import com.beatcraft.data.types.ISplinePath;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
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

        var localPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        var camPos = mc.gameRenderer.getCamera().getPos().toVector3f();
        //localPos.x = 0;
        //localPos.y = 0;
        //localPos.add(0, 0, camPos.z + 0.25f);
        localPos.add(0.2f, 0.3f, 0.25f);

        render(basePath, localPos, data.getColor().toARGB());

        if (DebugRenderer.doDebugRendering && DebugRenderer.renderArcDebugLines) {
            DebugRenderer.renderPath(basePath, localPos.add(camPos, new Vector3f()), segments, data.getColor().toARGB());
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

        BeatcraftRenderer.recordRenderCall(() -> _render(path, origin, color));

    }

    public void _render(ISplinePath path, Vector3f origin, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        //Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        int segments = 50;

        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(origin);
            Vector3f p2 = path.evaluate(f2).add(origin);
            Vector3f t = path.getTangent(f);
            Vector3f t2 = path.getTangent(f2);

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

            int fade = (int) (Math.clamp((12f - dist) / 9f, 0f, 1f) * 127f) << 24;

            if (fade == 0) {
                continue;
            }

            int col = (color + fade);

            buffer.vertex(q1[0].x, q1[0].y, q1[0].z).color(col);
            buffer.vertex(q1[1].x, q1[1].y, q1[1].z).color(col);
            buffer.vertex(q1[2].x, q1[2].y, q1[2].z).color(col);
            buffer.vertex(q1[3].x, q1[3].y, q1[3].z).color(col);

            buffer.vertex(q2[0].x, q2[0].y, q2[0].z).color(col);
            buffer.vertex(q2[1].x, q2[1].y, q2[1].z).color(col);
            buffer.vertex(q2[2].x, q2[2].y, q2[2].z).color(col);
            buffer.vertex(q2[3].x, q2[3].y, q2[3].z).color(col);

            buffer.vertex(q3[0].x, q3[0].y, q3[0].z).color(col);
            buffer.vertex(q3[1].x, q3[1].y, q3[1].z).color(col);
            buffer.vertex(q3[2].x, q3[2].y, q3[2].z).color(col);
            buffer.vertex(q3[3].x, q3[3].y, q3[3].z).color(col);

        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;


        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

    }

    @Override
    public void seek(float beat) {
        super.seek(beat);
    }
}
