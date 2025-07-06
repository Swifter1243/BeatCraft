package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.render.mesh.MeshLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class MirrorHandler {

    private static Bloomfog mirrorBloomfog;

    public static Quaternionf invCameraRotation = new Quaternionf();

    // this is for the solid block faces of mirror blocks, not mirrored objects
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> plainMirrorCalls = new ArrayList<>();

    // lists for mirrored renders
    private static final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> drawCalls = new ArrayList<>();
    private static final ArrayList<Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> mirrorDraws = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorNotes = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorArrows = new ArrayList<>();
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorWallGlows = new ArrayList<>();
    private static final ArrayList<Runnable> earlyCalls = new ArrayList<>();
    private static final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();

    public static SimpleFramebuffer mirrorFramebuffer;
    public static SimpleFramebuffer depthFramebuffer;

    public static ShaderProgram mirrorShader;
    public static ShaderProgram mirrorPositionColorClip;

    public static void init() {
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer = new SimpleFramebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
        depthFramebuffer = new SimpleFramebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);

        mirrorBloomfog = new Bloomfog(false);

        try {
            mirrorShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "light_mirror", VertexFormats.POSITION_COLOR);
            mirrorPositionColorClip = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "position_color_clip", VertexFormats.POSITION_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resize() {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), MinecraftClient.IS_SYSTEM_MAC);
        depthFramebuffer.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), MinecraftClient.IS_SYSTEM_MAC);
        mirrorBloomfog.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), false);
    }

    public static void recordMirrorLightDraw(Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        mirrorDraws.add(call);
    }

    public static void recordMirrorNoteDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        mirrorNotes.add(call);
    }

    public static void recordMirrorArrowDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        mirrorArrows.add(call);
    }

    public static void recordMirrorLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        mirrorWallGlows.add(call);
    }

    public static void recordEarlyRenderCall(Runnable call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        earlyCalls.add(call);
    }

    public static void recordCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        drawCalls.add(call);
    }

    public static void recordPlainCall(BiConsumer<BufferBuilder, Vector3f> call) {
        plainMirrorCalls.add(call);
    }

    public static void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        if (!BeatCraftClient.playerConfig.doMirror()) return;
        obstacleRenderCalls.add(call);
    }

    private static void renderEarly(Tessellator tessellator, Vector3f cameraPos) {
        for (var call : earlyCalls) {
            call.run();
        }
        earlyCalls.clear();

    }

    private static void renderObstacles(Tessellator tessellator, Vector3f cameraPos) {
        if (BeatmapPlayer.currentBeatmap == null) {
            obstacleRenderCalls.clear();
            return;
        }

        int color = BeatmapPlayer.currentBeatmap.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : obstacleRenderCalls) {
            call.accept(buffer, cameraPos, color);
        }
        obstacleRenderCalls.clear();

        var buff = buffer.endNullable();

        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

            BufferRenderer.drawWithGlobalProgram(buff);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);

        }

    }

    private static void renderNotes(Tessellator tessellator, Vector3f cameraPos) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        var q = MemoryPool.newQuaternionf();
        MeshLoader.MIRROR_COLOR_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_BOMB_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_NOTE_DOT_INSTANCED_MESH.render(cameraPos);
        MeshLoader.MIRROR_CHAIN_DOT_INSTANCED_MESH.render(cameraPos);
        MemoryPool.releaseSafe(q);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private static void renderNotes0(Tessellator tessellator, Vector3f cameraPos) {
        // notes and debris
        BufferBuilder triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        int oldTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShader(() -> BeatCraftRenderer.noteShader);
        RenderSystem.setShaderTexture(0, MeshLoader.NOTE_TEXTURE);
        for (var renderCall : mirrorNotes) {
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

        RenderSystem.setShader(() -> BeatCraftRenderer.arrowShader);
        RenderSystem.setShaderTexture(0, MeshLoader.ARROW_TEXTURE);
        for (var renderCall : mirrorArrows) {
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
        mirrorNotes.clear();
        mirrorArrows.clear();
    }

    private static void renderFloorLights(Tessellator tessellator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : mirrorWallGlows) {
            call.accept(buffer, cameraPos);
        }

        mirrorWallGlows.clear();

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static void renderForDepth(Tessellator tessellator, Vector3f cameraPos) {
        depthFramebuffer.setClearColor(0, 0, 0, 1);
        depthFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = depthFramebuffer;
        depthFramebuffer.beginWrite(true);

        var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : drawCalls) {
            call.accept(buffer, cameraPos, invCameraRotation.conjugate(new Quaternionf()));
        }

        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

        }

        BeatCraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatCraftRenderer.bloomfog.overrideBuffer = false;
        depthFramebuffer.endWrite();
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    public static void drawMirror() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        Matrix4f worldTransform = new Matrix4f();
        worldTransform.translate(cameraPos);

        var q = MemoryPool.newQuaternionf(invCameraRotation).conjugate();
        worldTransform.rotate(q);

        if (BeatCraftClient.playerConfig.doMirror()) {
            renderForDepth(tessellator, cameraPos);
        }

        // render mirror block non-reflective faces
        for (var call : plainMirrorCalls) {
            call.accept(buffer, cameraPos);
        }
        plainMirrorCalls.clear();
        if (!BeatCraftClient.playerConfig.doMirror()) {
            q.set(invCameraRotation);
            q.conjugate();
            for (var call : drawCalls) {
                call.accept(buffer, cameraPos, q);
            }
            drawCalls.clear();
        }
        MemoryPool.releaseSafe(q);


        var buff = buffer.endNullable();

        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(() -> Bloomfog.bloomfogPositionColor);
            Bloomfog.bloomfogPositionColor.getUniformOrDefault("WorldTransform").set(worldTransform);
            Bloomfog.bloomfogPositionColor.getUniformOrDefault("u_fog").set(Bloomfog.getFogHeights());
            BeatCraftRenderer.bloomfog.loadTex();
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.enableCull();
        }

        mirrorFramebuffer.setClearColor(0, 0, 0, 1);
        mirrorFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        if (!BeatCraftClient.playerConfig.doMirror()) {
            earlyCalls.clear();
            mirrorDraws.clear();
            mirrorNotes.clear();
            mirrorArrows.clear();
            mirrorWallGlows.clear();
            obstacleRenderCalls.clear();
            MeshLoader.MIRROR_COLOR_NOTE_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_BOMB_NOTE_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_NOTE_DOT_INSTANCED_MESH.cancelDraws();
            MeshLoader.MIRROR_CHAIN_DOT_INSTANCED_MESH.cancelDraws();

            LightMesh.cancelMirrorDraws();

            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            return;
        };


        MinecraftClient.getInstance().getFramebuffer().endWrite();
        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = mirrorFramebuffer;
        mirrorFramebuffer.beginWrite(true);

        renderEarly(tessellator, cameraPos);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : mirrorDraws) {
            call.accept(buffer, cameraPos, new Quaternionf(), true);
        }
        mirrorDraws.clear();
        buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(() -> mirrorPositionColorClip);
            RenderSystem.setShaderTexture(0, depthFramebuffer.getDepthAttachment());
            mirrorPositionColorClip.addSampler("Sampler0", depthFramebuffer.getDepthAttachment());
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        }

        renderNotes(tessellator, cameraPos);

        renderFloorLights(tessellator, cameraPos);

        renderObstacles(tessellator, cameraPos);

        LightMesh.renderAllMirror();
        //RenderSystem.depthMask(false);
        //RenderSystem.disableCull();


        BeatCraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatCraftRenderer.bloomfog.overrideBuffer = false;
        mirrorFramebuffer.endWrite();
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);


        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : drawCalls) {
            call.accept(buffer, cameraPos, invCameraRotation.conjugate(new Quaternionf()));
        }
        drawCalls.clear();

        buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(() -> mirrorShader);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderTexture(0, mirrorFramebuffer.getColorAttachment());
            mirrorShader.addSampler("Sampler0", mirrorFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, mirrorFramebuffer.getDepthAttachment());
            mirrorShader.addSampler("Sampler1", mirrorFramebuffer.getDepthAttachment());
            RenderSystem.setShaderTexture(2, BeatCraftRenderer.bloomfog.blurredBuffer.getColorAttachment());
            mirrorShader.addSampler("Sampler2", BeatCraftRenderer.bloomfog.blurredBuffer.getColorAttachment());
            mirrorShader.getUniformOrDefault("WorldPos").set(cameraPos);
            mirrorShader.getUniformOrDefault("GameTime").set(BeatCraftClient.random.nextFloat());
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.depthMask(false);
        }

    }


}
