package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
import com.beatcraft.render.mesh.MeshLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BeatCraftRenderer {

    public static Bloomfog bloomfog;

    private static final ArrayList<Consumer<VertexConsumerProvider>> earlyRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> bloomfogPosColCalls = new ArrayList<>();
    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();
    private static final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> noteRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arrowRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserPreRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> lightRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arcRenderCalls = new ArrayList<>();

    public static ShaderProgram noteShader;
    public static ShaderProgram arrowShader;
    public static ShaderProgram heartHealthShader;

    public static void init() {
        bloomfog = Bloomfog.create();

        try {
            noteShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "note_shader", VertexFormats.POSITION_TEXTURE_COLOR);
            arrowShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "arrow_shader", VertexFormats.POSITION_TEXTURE_COLOR);
            heartHealthShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "health_hearts", VertexFormats.POSITION_TEXTURE_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void updateBloomfogSize(int width, int height) {
        if (bloomfog != null && bloomfog.framebuffer != null) bloomfog.resize(width, height, true);
    }

    public static void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        BeatmapPlayer.onRender(matrices, camera, tickDelta);
    }

    public static void recordNoteRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        noteRenderCalls.add(call);
    }

    public static void recordArrowRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        arrowRenderCalls.add(call);
    }

    public static void recordObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        obstacleRenderCalls.add(call);
    }

    public static void recordRenderCall(Runnable call) {
        renderCalls.add(call);
    }

    public static void recordArcRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        arcRenderCalls.add(call);
    }

    public static void recordLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserRenderCalls.add(call);
    }

    public static void recordLaserPreRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserPreRenderCalls.add(call);
    }

    public static void recordLightRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        lightRenderCalls.add(call);
    }

    public static void recordEarlyRenderCall(Consumer<VertexConsumerProvider> call) {
        earlyRenderCalls.add(call);
    }

    public static void recordBloomfogPosColCall(BiConsumer<BufferBuilder, Vector3f> call) {
        bloomfogPosColCalls.add(call);
    }

    private static void renderEarly(VertexConsumerProvider vcp) {

        // other stuff
        for (var call : earlyRenderCalls) {
            call.accept(vcp);
        }
        earlyRenderCalls.clear();
    }

    private static void renderObstacles(Tessellator tessellator, Vector3f cameraPos) {
        if (BeatmapPlayer.currentBeatmap == null) {
            obstacleRenderCalls.clear();
            return;
        }

        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (var call : obstacleRenderCalls) {
            call.accept(buffer, cameraPos, color);
        }
        obstacleRenderCalls.clear();

        var buff = buffer.endNullable();

        if (buff != null) {
            ObstacleGlowRenderer.grabScreen();


            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);

            var scene = ObstacleGlowRenderer.framebuffer;//MinecraftClient.getInstance().getFramebuffer();

            RenderSystem.setShader(() -> ObstacleGlowRenderer.distortionShader);
            RenderSystem.setShaderTexture(0, scene.getColorAttachment());
            ObstacleGlowRenderer.distortionShader.getUniformOrDefault("Time").set(System.nanoTime() / 1_000_000_000f);

            buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

            BufferRenderer.drawWithGlobalProgram(buff);

            RenderSystem.disableDepthTest();
            RenderSystem.disableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();

        }

    }

    private static void renderBloomfogPosCol(Tessellator tessellator, Vector3f cameraPos) {

        var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : bloomfogPosColCalls) {
            call.accept(buffer, cameraPos);
        }
        bloomfogPosColCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(() -> Bloomfog.bloomfogPositionColor);
            bloomfog.loadTex();
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.enableCull();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }



    }

    private static void renderLightDepth(Tessellator tessellator, Vector3f cameraPos) {

        Bloomfog.lightDepth.setClearColor(0, 0, 0, 1);
        Bloomfog.lightDepth.clear(true);

        bloomfog.overrideBuffer = true;
        bloomfog.overrideFramebuffer = Bloomfog.lightDepth;

        Bloomfog.lightDepth.beginWrite(true);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);


        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        var buff = buffer.endNullable();
        if (buff != null) {
            BufferRenderer.drawWithGlobalProgram(buff);
        }

        Bloomfog.lightDepth.endWrite();

        bloomfog.overrideFramebuffer = null;
        bloomfog.overrideBuffer = false;

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

    }

    private static void renderEnvironmentLights(Tessellator tessellator, Vector3f cameraPos) {
        // environment lights

        Matrix4f worldTransform = new Matrix4f();
        worldTransform.translate(cameraPos);
        worldTransform.rotate(MirrorHandler.invCameraRotation.conjugate(new Quaternionf()));

        renderLightDepth(tessellator, cameraPos);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(() -> Bloomfog.lightsPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        lightRenderCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            //buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
            Bloomfog.lightsPositionColorShader.addSampler("Sampler0", Bloomfog.lightDepth.getDepthAttachment());
            RenderSystem.setShaderTexture(0, Bloomfog.lightDepth.getDepthAttachment());
            Bloomfog.lightsPositionColorShader.getUniformOrDefault("WorldTransform").set(worldTransform);
            BufferRenderer.drawWithGlobalProgram(buff);
        }
    }

    private static void renderNotes(Tessellator tessellator, Vector3f cameraPos) {
        // notes and debris
        BufferBuilder triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        int oldTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShader(() -> noteShader);
        RenderSystem.setShaderTexture(0, MeshLoader.NOTE_TEXTURE);
        for (var renderCall : noteRenderCalls) {
            try {
                renderCall.accept(triBuffer, cameraPos);
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        var triBuff = triBuffer.endNullable();
        if (triBuff != null) {
            BufferRenderer.drawWithGlobalProgram(triBuff);
        }

        triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.setShader(() -> arrowShader);
        RenderSystem.setShaderTexture(0, MeshLoader.ARROW_TEXTURE);
        for (var renderCall : arrowRenderCalls) {
            try {
                renderCall.accept(triBuffer, cameraPos);
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        triBuff = triBuffer.endNullable();
        if (triBuff != null) {
            BufferRenderer.drawWithGlobalProgram(triBuff);
        }

        RenderSystem.setShaderTexture(0, oldTexture);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        noteRenderCalls.clear();
        arrowRenderCalls.clear();
    }

    private static void renderFloorLightsPhase1(Tessellator tessellator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : laserPreRenderCalls) {
            call.accept(buffer, cameraPos);
        }
        laserPreRenderCalls.clear();

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;


        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static void renderFloorLights(Tessellator tessellator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : laserRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        laserRenderCalls.clear();

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;


        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static void renderArcs(Tessellator tessellator, Vector3f cameraPos) {

        var buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        for (var call : arcRenderCalls) {
            call.accept(buffer, cameraPos);
        }
        arcRenderCalls.clear();
        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            BufferRenderer.drawWithGlobalProgram(buff);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }

    }

    public static void earlyRender(VertexConsumerProvider vcp) {

        Tessellator tessellator = Tessellator.getInstance();
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        renderEarly(vcp);
        renderBloomfogPosCol(tessellator, cameraPos);

        renderEnvironmentLights(tessellator, cameraPos);

        renderNotes(tessellator, cameraPos);

        renderFloorLightsPhase1(tessellator, cameraPos);

        renderObstacles(tessellator, cameraPos);

        renderFloorLights(tessellator, cameraPos);

    }

    public static void render() {

        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        renderArcs(Tessellator.getInstance(), cameraPos);

        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        renderCalls.clear();

        renderFootPosIndicator();

    }

    private static void renderFootPosIndicator() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        var col = BeatmapPlayer.currentBeatmap == null ? 0x7FFFFFFF : 0x38FFFFFF;

        float height = 0.01f;

        for (int i = 0; i < 2; i++) {
            var x = (i-0.5f) * 0.25f;

            buffer.vertex(MemoryPool.newVector3f(x - 0.1f, height, -0.1f).sub(camPos)).color(col);
            buffer.vertex(MemoryPool.newVector3f(x - 0.1f, height, 0.1f).sub(camPos)).color(col);
            buffer.vertex(MemoryPool.newVector3f(x + 0.1f, height, 0.1f).sub(camPos)).color(col);
            buffer.vertex(MemoryPool.newVector3f(x + 0.1f, height, -0.1f).sub(camPos)).color(col);

        }
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public static List<Vector3f[]> getCubeEdges(Vector3f minPos, Vector3f maxPos) {
        List<Vector3f[]> edges = new ArrayList<>();

        Vector3f[] corners = new Vector3f[] {
            MemoryPool.newVector3f(minPos.x, minPos.y, minPos.z),
            MemoryPool.newVector3f(maxPos.x, minPos.y, minPos.z),
            MemoryPool.newVector3f(maxPos.x, maxPos.y, minPos.z),
            MemoryPool.newVector3f(minPos.x, maxPos.y, minPos.z),
            MemoryPool.newVector3f(minPos.x, minPos.y, maxPos.z),
            MemoryPool.newVector3f(maxPos.x, minPos.y, maxPos.z),
            MemoryPool.newVector3f(maxPos.x, maxPos.y, maxPos.z),
            MemoryPool.newVector3f(minPos.x, maxPos.y, maxPos.z)
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
