package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
import com.beatcraft.render.mesh.MeshLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BeatcraftRenderer {

    private static final ArrayList<Consumer<VertexConsumerProvider>> earlyRenderCalls = new ArrayList<>();
    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();
    private static final ArrayList<TriConsumer<BufferBuilder, BufferBuilder, Vector3f>> noteRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();

    public static void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        BeatmapPlayer.onRender(matrices, camera, tickDelta);
    }

    // lambdas are passed, in order, the triangle buffer and the quad buffer
    public static void recordNoteRenderCall(TriConsumer<BufferBuilder, BufferBuilder, Vector3f> call) {
        noteRenderCalls.add(call);
    }

    public static void recordRenderCall(Runnable call) {
        renderCalls.add(call);
    }

    public static void recordLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserRenderCalls.add(call);
    }

    public static void recordEarlyRenderCall(Consumer<VertexConsumerProvider> call) {
        earlyRenderCalls.add(call);
    }

    public static void earlyRender(VertexConsumerProvider vcp) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        int oldTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, MeshLoader.NOTE_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        for (var renderCall : noteRenderCalls) {
            try {
                renderCall.accept(triBuffer, null, cameraPos);
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        var triBuff = triBuffer.endNullable();
        if (triBuff != null) {
            BufferRenderer.drawWithGlobalProgram(triBuff);
        }

        BufferBuilder quadBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (var renderCall : noteRenderCalls) {
            try {
                renderCall.accept(null, quadBuffer, cameraPos);
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }

        var quadBuff = quadBuffer.endNullable();
        if (quadBuff != null) {
            BufferRenderer.drawWithGlobalProgram(quadBuff);
        }

        RenderSystem.setShaderTexture(0, oldTexture);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        noteRenderCalls.clear();


        for (var call : earlyRenderCalls) {
            call.accept(vcp);
        }
        earlyRenderCalls.clear();

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : laserRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        laserRenderCalls.clear();

        var buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    public static void render() {

        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        renderCalls.clear();

    }

    public static List<Vector3f[]> getCubeEdges(Vector3f minPos, Vector3f maxPos) {
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
            edges.add(new Vector3f[]{
                corners[pair[0]],
                corners[pair[1]]
            });
        }

        return edges;
    }


    public static List<Vector3f[]> getCubeFaces(Vector3f minPos, Vector3f maxPos) {
        List<Vector3f[]> faces = new ArrayList<>();

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


        int[][] faceIndices = new int[][] {
            {0, 1, 2, 3}, // F
            {4, 5, 6, 7}, // B
            {0, 3, 7, 4}, // L
            {1, 5, 6, 2}, // R
            {3, 2, 6, 7}, // T
            {0, 4, 5, 1}  // D
        };

        for (int[] pair : faceIndices) {
            faces.add(new Vector3f[]{
                corners[pair[0]],
                corners[pair[1]],
                corners[pair[2]],
                corners[pair[3]]
            });
        }

        return faces;
    }


}
