package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.data.types.ISplinePath;
import com.beatcraft.logic.Hitbox;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DebugRenderer {

    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();

    public static final ParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1, 0, 0), 0.5f);
    public static final ParticleEffect ORANGE_DUST = new DustParticleEffect(new Vector3f(1, 0.5f, 0), 0.5f);
    public static final ParticleEffect YELLOW_DUST = new DustParticleEffect(new Vector3f(1, 1, 0), 0.5f);
    public static final ParticleEffect GREEN_DUST = new DustParticleEffect(new Vector3f(0, 1, 0), 0.5f);
    public static final ParticleEffect BLUE_DUST = new DustParticleEffect(new Vector3f(0, 0, 1), 0.5f);
    public static final ParticleEffect MAGENTA_DUST = new DustParticleEffect(new Vector3f(1, 0, 1), 0.5f);


    public static boolean doDebugRendering = false;
    public static boolean debugSaberRendering = false;
    public static boolean renderHitboxes = false;
    public static boolean renderArcDebugLines = false;

    private static List<Vector3f[]> getCuboidEdges(Vector3f minPos, Vector3f maxPos) {
        List<Vector3f[]> edges = new ArrayList<>();

        Vector3f[] corners = new Vector3f[] {
            new Vector3f(minPos.x, minPos.y, minPos.z),
            new Vector3f(maxPos.x, minPos.y, minPos.z),
            new Vector3f(maxPos.x, maxPos.y, minPos.z),
            new Vector3f(minPos.x, maxPos.y, minPos.z),
            new Vector3f(minPos.x, minPos.y, maxPos.z),
            new Vector3f(maxPos.x, minPos.y, maxPos.z),
            new Vector3f(maxPos.x, maxPos.y, maxPos.z),
            new Vector3f(minPos.x, maxPos.y, maxPos.z)
        };

        int[][] edgeIndices = new int[][] {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] pair : edgeIndices) {
            edges.add(new Vector3f[] { corners[pair[0]], corners[pair[1]] });
        }

        return edges;
    }

    public static void renderParticle(Vector3f point, ParticleEffect particle) {

        MinecraftClient.getInstance().world.addParticle(
            particle,
            point.x, point.y, point.z, 0, 0, 0
        );
    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest) {
        renderCalls.add(() -> _renderHitbox(hitbox, position, orientation, color, doDepthTest));

    }

    public static void renderPath(ISplinePath path, Vector3f offset, int segments, int color) {
        renderCalls.add(() -> _renderPath(path, offset, segments, color));
    }

    public static void _renderPath(ISplinePath path, Vector3f offset, int segments, int color) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();


        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(offset).sub(cam);
            Vector3f p2 = path.evaluate(f2).add(offset).sub(cam);

            //renderParticle(p.add(cam, new Vector3f()), ParticleTypes.BUBBLE);

            Vector3f normal = p2.sub(p, new Vector3f()).normalize();

            buffer.vertex(p.x, p.y, p.z).color(color).normal(normal.x, normal.y, normal.z);
            buffer.vertex(p2.x, p2.y, p2.z).color(color).normal(normal.x, normal.y, normal.z);

        }

        List<Vector3f> controlPoints = path.getControlPoints();

        if (controlPoints.size() >= 2) {
            for (int i = 0; i < controlPoints.size()-1; i++) {
                Vector3f p = controlPoints.get(i).add(offset, new Vector3f()).sub(cam);
                Vector3f p2 = controlPoints.get(i+1).add(offset, new Vector3f()).sub(cam);

                Vector3f normal = p2.sub(p, new Vector3f()).normalize();

                buffer.vertex(p.x, p.y, p.z).color(0xFF0000).normal(normal.x, normal.y, normal.z);
                buffer.vertex(p2.x, p2.y, p2.z).color(0xFF0000).normal(normal.x, normal.y, normal.z);

            }
        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();

    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color) {
        renderHitbox(hitbox, position, orientation, color, false);
    }

    private static void _renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest) {

        var edges = getCuboidEdges(hitbox.min, hitbox.max);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        for (Vector3f[] edge : edges) {
            var c1 = edge[0].rotate(orientation, new Vector3f());
            var c2 = edge[1].rotate(orientation, new Vector3f());
            c1.add(position);
            c2.add(position);

            var normal = c2.sub(c1, new Vector3f()).normalize();

            buffer.vertex((float) (c1.x - cam.x), (float) (c1.y - cam.y), (float) (c1.z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);
            buffer.vertex((float) (c2.x - cam.x), (float) (c2.y - cam.y), (float) (c2.z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);

        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.depthMask(doDepthTest);
        if (doDepthTest) RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }



    public static void render() {

        //DebugRenderer.renderPath(BeatCraftClient.TEST, new Vector3f(), 50, 0xFF0000);

        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }

        renderCalls.clear();

    }

}
