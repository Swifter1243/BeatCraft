package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.effect.ObstacleGlowRenderer;
import com.beatcraft.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.particle.SmokeParticle;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.*;

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
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserPreRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> lightRenderCalls = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arcRenderCalls = new ArrayList<>();

    public static ShaderProgram noteShader;
    public static ShaderProgram arrowShader;
    public static ShaderProgram heartHealthShader;
    public static ShaderProgram BCPosTexColShader;

    public static void init() {
        bloomfog = Bloomfog.create();

        try {
            noteShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "note_shader", VertexFormats.POSITION_TEXTURE_COLOR);
            arrowShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "arrow_shader", VertexFormats.POSITION_TEXTURE_COLOR);
            heartHealthShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "health_hearts", VertexFormats.POSITION_TEXTURE_COLOR);
            BCPosTexColShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bc_tex_col", VertexFormats.POSITION_TEXTURE_COLOR);
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

        bloomfog.overrideBuffer = true;
        bloomfog.overrideFramebuffer = Bloomfog.lightDepth;

        Bloomfog.lightDepth.beginWrite(true);
        Bloomfog.lightDepth.setClearColor(0, 0, 0, 1);
        Bloomfog.lightDepth.clear(MinecraftClient.IS_SYSTEM_MAC);

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

    private static void renderBackgroundLights(Tessellator tessellator, Vector3f cameraPos, Matrix4f worldTransform) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(() -> Bloomfog.backlightsPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        var buff = buffer.endNullable();
        if (buff != null) {
            Bloomfog.backlightsPositionColorShader.addSampler("Sampler0", Bloomfog.lightDepth.getDepthAttachment());
            RenderSystem.setShaderTexture(0, Bloomfog.lightDepth.getDepthAttachment());
            Bloomfog.backlightsPositionColorShader.getUniformOrDefault("WorldTransform").set(worldTransform);
            Bloomfog.backlightsPositionColorShader.getUniformOrDefault("u_fog").set(Bloomfog.getFogHeights());
            BufferRenderer.drawWithGlobalProgram(buff);
        }
    }

    private static void renderEnvironmentLights(Tessellator tessellator, Vector3f cameraPos) {
        // environment lights

        Matrix4f worldTransform = new Matrix4f();
        worldTransform.translate(cameraPos);
        worldTransform.rotate(MirrorHandler.invCameraRotation.conjugate(new Quaternionf()));

        renderLightDepth(tessellator, cameraPos);

        //renderBackgroundLights(tessellator, cameraPos, worldTransform);
        //
        //lightRenderCalls.clear();
        //if (true) return;

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(() -> Bloomfog.backlightsPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        //RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        lightRenderCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            Bloomfog.backlightsPositionColorShader.addSampler("Sampler0", Bloomfog.lightDepth.getDepthAttachment());
            RenderSystem.setShaderTexture(0, Bloomfog.lightDepth.getDepthAttachment());
            Bloomfog.backlightsPositionColorShader.getUniformOrDefault("WorldTransform").set(worldTransform);
            Bloomfog.backlightsPositionColorShader.getUniformOrDefault("u_fog").set(Bloomfog.getFogHeights());
            BufferRenderer.drawWithGlobalProgram(buff);
        }
        
        LightMesh.renderAllSolid();

        RenderSystem.defaultBlendFunc();
    }

    private static void renderNotes(Tessellator tessellator, Vector3f cameraPos) {
        MeshLoader.COLOR_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.BOMB_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.NOTE_ARROW_INSTANCED_MESH.render(cameraPos);
        MeshLoader.NOTE_DOT_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_DOT_INSTANCED_MESH.render(cameraPos);
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
        var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vector3f cameraPos = camera.getPos().toVector3f();

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

    private static double lastSmokeSpawn = 0;
    private static final Random random = Random.create();

    public static void renderSmoke(Vector3f cameraPos) {
        var t = System.nanoTime() / 1_000_000_000d;

        if ((t - lastSmokeSpawn) > SmokeParticle.SPAWN_INTERVAL) {
            lastSmokeSpawn = t;
            BeatcraftParticleRenderer.addParticle(new SmokeParticle(random));
        }
        Bloomfog.sceneDepthBuffer = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();

        MinecraftClient.getInstance().getFramebuffer().endWrite();
        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = Bloomfog.bloomInput;
        Bloomfog.bloomInput.clear(MinecraftClient.IS_SYSTEM_MAC);
        Bloomfog.bloomInput.beginWrite(true);

        MeshLoader.SMOKE_INSTANCED_MESH.render(cameraPos);

        Bloomfog.bloomInput.endWrite();
        BeatCraftRenderer.bloomfog.overrideBuffer = false;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = null;
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.setShaderTexture(0, Bloomfog.bloomInput.getColorAttachment());
        RenderSystem.setShader(() -> Bloomfog.blitShader);
        float z = 0;
        buffer.vertex(new Vector3f(-1, -1, z)).texture(0, 0).color(0xFF020200);
        buffer.vertex(new Vector3f( 1, -1, z)).texture(1, 0).color(0xFF020200);
        buffer.vertex(new Vector3f( 1,  1, z)).texture(1, 1).color(0xFF020200);
        buffer.vertex(new Vector3f(-1,  1, z)).texture(0, 1).color(0xFF020200);


        var old = new Matrix4f(RenderSystem.getModelViewMatrix());
        RenderSystem.getModelViewMatrix().identity();

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.getModelViewMatrix().set(old);

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

    public static List<Vector3f[]> getGlowingQuadAsTris(Vector2f quadSize, float glowSpread) {
        List<Vector3f[]> tris = new ArrayList<>();

        float halfWidth = quadSize.x / 2f;
        float halfHeight = quadSize.y / 2f;
        float outerHalfWidth = halfWidth + glowSpread;
        float outerHalfHeight = halfHeight + glowSpread;

        // Outer corners (alpha = 0)
        Vector3f topLeftOuter     = new Vector3f(-outerHalfWidth,  outerHalfHeight, 0f);
        Vector3f topRightOuter    = new Vector3f( outerHalfWidth,  outerHalfHeight, 0f);
        Vector3f bottomLeftOuter  = new Vector3f(-outerHalfWidth, -outerHalfHeight, 0f);
        Vector3f bottomRightOuter = new Vector3f( outerHalfWidth, -outerHalfHeight, 0f);

        // Inner corners (alpha = 1)
        Vector3f topLeftInner     = new Vector3f(-halfWidth,  halfHeight, 1f);
        Vector3f topRightInner    = new Vector3f( halfWidth,  halfHeight, 1f);
        Vector3f bottomLeftInner  = new Vector3f(-halfWidth, -halfHeight, 1f);
        Vector3f bottomRightInner = new Vector3f( halfWidth, -halfHeight, 1f);

        // Center quad
        tris.add(new Vector3f[] { topLeftInner, bottomLeftInner, bottomRightInner });
        tris.add(new Vector3f[] { topLeftInner, bottomRightInner, topRightInner });

        // Top glow
        tris.add(new Vector3f[] { topLeftOuter, topLeftInner, topRightInner });
        tris.add(new Vector3f[] { topLeftOuter, topRightInner, topRightOuter });

        // Bottom glow
        tris.add(new Vector3f[] { bottomLeftInner, bottomLeftOuter, bottomRightOuter });
        tris.add(new Vector3f[] { bottomLeftInner, bottomRightOuter, bottomRightInner });

        // Left glow
        tris.add(new Vector3f[] { topLeftInner, topLeftOuter, bottomLeftOuter });
        tris.add(new Vector3f[] { topLeftInner, bottomLeftOuter, bottomLeftInner });

        // Right glow
        tris.add(new Vector3f[] { bottomRightInner, bottomRightOuter, topRightOuter });
        tris.add(new Vector3f[] { bottomRightInner, topRightOuter, topRightInner });

        return tris;
    }


    public static List<Vector3f[]> getCubeFaces(
        Vector3f vxyz, Vector3f vxyZ, Vector3f vXyZ, Vector3f vXyz,
        Vector3f vxYz, Vector3f vxYZ, Vector3f vXYZ, Vector3f vXYz,
        boolean includeBottomFace
    ) {
        var faces = new ArrayList<Vector3f[]>();

        faces.add(new Vector3f[] {
            vXYz, vXYZ, vxYZ, vxYz
        });
        faces.add(new Vector3f[] {
            vxyZ, vXyZ, vXYZ, vxYZ
        });
        faces.add(new Vector3f[] {
            vXyz, vxyz, vxYz, vXYz
        });
        faces.add(new Vector3f[] {
            vxyz, vxyZ, vxYZ, vxYz
        });
        faces.add(new Vector3f[] {
            vXyZ, vXyz, vXYz, vXYZ
        });
        if (includeBottomFace) {
            faces.add(new Vector3f[] {
                vxyz, vXyz, vXyZ, vxyZ
            });
        }

        return faces;
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
            {3, 2, 1, 0}, // F
            {4, 5, 6, 7}, // B
            {4, 7, 3, 0}, // L
            {2, 6, 5, 1}, // R
            {7, 6, 2, 3}, // T
            {1, 5, 4, 0}  // D
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
