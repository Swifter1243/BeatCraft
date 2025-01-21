package com.beatcraft.render.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.AnimationState;
import com.beatcraft.beatmap.data.object.Arc;
import com.beatcraft.data.types.BezierPath;
import com.beatcraft.data.types.ISplinePath;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.utils.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PhysicalArc extends PhysicalGameplayObject<Arc> {

    BezierPath basePath;

    public PhysicalArc(Arc data) {
        super(data);
        buildBasePath(new Vector3f(1, 1, 1));
    }

    public void buildBasePath(Vector3f modifier) {

        ArrayList<Vector3f> points = new ArrayList<>();

        Vector3f start = new Vector3f(
            (-data.getX()) * 0.6f + 0.9f,
            data.getY() * 0.6f + 0.8f,
            0
        );

        Vector3f startC = Arc.cutDirectionToControlPoint(data.getHeadCutDirection()).mul(data.getHeadMagnitude()).add(start);

        Vector3f end = new Vector3f(
            (-data.getTailX()) * 0.6f + 0.9f,
            data.getTailY() * 0.6f + 0.8f,
            -(data.getTailBeat() - data.getBeat())
        );

        Vector3f endC = Arc.cutDirectionToControlPoint(data.getTailCutDirection()).mul(data.getTailMagnitude()).mul(-1, -1, -1).add(end);

        Vector3f midpoint = MathUtil.lerpVector3(start, end, 0.5f);


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

    }

    protected void updateCurve() {
        float beatSpacing = data.getNjs() * (60f / BeatmapPlayer.currentBeatmap.getInfo().getBpm());

        buildBasePath(new Vector3f(1, 1, -beatSpacing));
    }

    @Override
    protected void objectRender(MatrixStack matrices, VertexConsumer vertexConsumer, AnimationState animationState) {

        updateCurve();

        var localPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        var camPos = mc.gameRenderer.getCamera().getPos().toVector3f();
        localPos.x = 0;
        localPos.y = 0;

        //render(basePath, localPos.add(0, 0, camPos.z + 0.25f), data.getColor().toARGB());
        DebugRenderer.renderPath(basePath, localPos.add(0, 0, camPos.z + 0.25f), 50, data.getColor().toARGB());

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

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);


        Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        int segments = 50;

        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(origin).sub(cam);
            Vector3f p2 = path.evaluate(f2).add(origin).sub(cam);
            Vector3f t = path.getTangent(f);
            Vector3f t2 = path.getTangent(f2);

            var h1 = MathUtil.generateCircle(t, 0.1f, 6);
            var h2 = MathUtil.generateCircle(t, 0.1f, 6);

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


            //buffer.vertex(q1[0].x, q1[0].y, q1[0].z).texture()


        }

    }

}
