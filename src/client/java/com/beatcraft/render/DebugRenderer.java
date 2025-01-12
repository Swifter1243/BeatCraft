package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.logic.Hitbox;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DebugRenderer {

    private static final ArrayList<Callable<Void>> renderCalls = new ArrayList<>();

    public static final ParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1, 0, 0), 0.5f);
    public static final ParticleEffect ORANGE_DUST = new DustParticleEffect(new Vector3f(1, 0.5f, 0), 0.5f);
    public static final ParticleEffect YELLOW_DUST = new DustParticleEffect(new Vector3f(1, 1, 0), 0.5f);
    public static final ParticleEffect GREEN_DUST = new DustParticleEffect(new Vector3f(0, 1, 0), 0.5f);
    public static final ParticleEffect BLUE_DUST = new DustParticleEffect(new Vector3f(0, 0, 1), 0.5f);
    public static final ParticleEffect MAGENTA_DUST = new DustParticleEffect(new Vector3f(1, 0, 1), 0.5f);


    public static boolean doDebugRendering = false;
    public static boolean debugSaberRendering = false;
    public static boolean renderHitboxes = false;

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

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color) {
        renderCalls.add(() -> {

            _renderHitbox(hitbox, position, orientation, color);

            return null;
        });
    }

    private static void _renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color) {

        var edges = getCuboidEdges(hitbox.min(), hitbox.max());

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
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);


        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }



    public static void render() {

        for (Callable<Void> renderCall : renderCalls) {
            try {
                renderCall.call();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }

        renderCalls.clear();

    }

}
