package com.beatcraft.client.render.effect;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class MirrorHandler {

    private final BeatmapPlayer mapController;

    private static final ArrayList<MirrorHandler> mirrors = new ArrayList<>();

    public static Quaternionf invCameraRotation = new Quaternionf();

    // this is for the solid block faces of mirror blocks, not mirrored objects
    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> plainMirrorCalls = new ArrayList<>();

    // lists for mirrored renders
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> drawCalls = new ArrayList<>();
    private final ArrayList<Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> mirrorDraws = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorNotes = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorArrows = new ArrayList<>();
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> mirrorWallGlows = new ArrayList<>();
    private final ArrayList<Runnable> earlyCalls = new ArrayList<>();
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();

    public static TextureTarget mirrorFramebuffer;
    public static TextureTarget depthFramebuffer;

    public static ShaderInstance mirrorShader;
    public static ShaderInstance mirrorPositionColorClip;

    public static void init() {

        try {
            mirrorShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "light_mirror", DefaultVertexFormat.POSITION_COLOR);
            mirrorPositionColorClip = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "position_color_clip", DefaultVertexFormat.POSITION_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MirrorHandler(BeatmapPlayer map) {
        mapController = map;
        var window = Minecraft.getInstance().getWindow();
        mirrorFramebuffer = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
        depthFramebuffer = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
        mirrors.add(this);
    }

    public static void resize() {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        var window = Minecraft.getInstance().getWindow();
        mirrorFramebuffer.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), Minecraft.ON_OSX);
        depthFramebuffer.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), Minecraft.ON_OSX);
    }

    public void recordMirrorLightDraw(Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        mirrorDraws.add(call);
    }

    public void recordMirrorNoteDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        mirrorNotes.add(call);
    }

    public void recordMirrorArrowDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        mirrorArrows.add(call);
    }

    public void recordMirrorLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        mirrorWallGlows.add(call);
    }

    public void recordEarlyRenderCall(Runnable call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        earlyCalls.add(call);
    }

    public void recordCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        drawCalls.add(call);
    }

    public void recordPlainCall(BiConsumer<BufferBuilder, Vector3f> call) {
        plainMirrorCalls.add(call);
    }

    public void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        if (!BeatcraftClient.playerConfig.doMirror()) return;
        obstacleRenderCalls.add(call);
    }

    private void renderEarly(Tesselator tessellator, Vector3f cameraPos) {
        for (var call : earlyCalls) {
            call.run();
        }
        earlyCalls.clear();

    }

    private void renderObstacles(Tesselator tessellator, Vector3f cameraPos) {
        if (mapController == null) {
            obstacleRenderCalls.clear();
            return;
        }

        int color = mapController.difficulty.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var call : obstacleRenderCalls) {
            call.accept(buffer, cameraPos, color);
        }
        obstacleRenderCalls.clear();

        var buff = buffer.build();

        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);

            BufferUploader.drawWithShader(buff);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);

        }

    }

    private void renderNotes(Tesselator tessellator, Vector3f cameraPos) {
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

    private void renderNotes0(Tesselator tessellator, Vector3f cameraPos) {
        // notes and debris
        BufferBuilder triBuffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        int oldTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShader(() -> BeatcraftRenderer.noteShader);
        RenderSystem.setShaderTexture(0, MeshLoader.NOTE_TEXTURE);
        for (var renderCall : mirrorNotes) {
            try {
                renderCall.accept(triBuffer, cameraPos);
            } catch (Exception e) {
                Beatcraft.LOGGER.error("Render call failed! ", e);
            }
        }
        var triBuff = triBuffer.build();
        if (triBuff != null) {
            BufferUploader.drawWithShader(triBuff);
        }

        triBuffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        RenderSystem.setShader(() -> BeatcraftRenderer.arrowShader);
        RenderSystem.setShaderTexture(0, MeshLoader.ARROW_TEXTURE);
        for (var renderCall : mirrorArrows) {
            try {
                renderCall.accept(triBuffer, cameraPos);
            } catch (Exception e) {
                Beatcraft.LOGGER.error("Render call failed! ", e);
            }
        }
        triBuff = triBuffer.build();
        if (triBuff != null) {
            BufferUploader.drawWithShader(triBuff);
        }

        RenderSystem.setShaderTexture(0, oldTexture);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        mirrorNotes.clear();
        mirrorArrows.clear();
    }

    private void renderFloorLights(Tesselator tessellator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : mirrorWallGlows) {
            call.accept(buffer, cameraPos);
        }

        mirrorWallGlows.clear();

        var buff = buffer.build();
        if (buff == null) return;

        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);

        BufferUploader.drawWithShader(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private void renderForDepth(Tesselator tessellator, Vector3f cameraPos) {
        depthFramebuffer.setClearColor(0, 0, 0, 1);
        depthFramebuffer.clear(Minecraft.ON_OSX);

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = depthFramebuffer;
        depthFramebuffer.bindWrite(true);

        var buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var call : drawCalls) {
            call.accept(buffer, cameraPos, invCameraRotation.conjugate(new Quaternionf()));
        }

        var buff = buffer.build();
        if (buff != null) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(buff);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

        }

        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        depthFramebuffer.unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    public void drawMirror() {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Vector3f cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        Matrix4f worldTransform = new Matrix4f();
        worldTransform.translate(cameraPos);

        var q = MemoryPool.newQuaternionf(invCameraRotation).conjugate();
        worldTransform.rotate(q);

        if (BeatcraftClient.playerConfig.doMirror()) {
            renderForDepth(tessellator, cameraPos);
        }

        // render mirror block non-reflective faces
        for (var call : plainMirrorCalls) {
            call.accept(buffer, cameraPos);
        }
        plainMirrorCalls.clear();
        if (!BeatcraftClient.playerConfig.doMirror()) {
            q.set(invCameraRotation);
            q.conjugate();
            for (var call : drawCalls) {
                call.accept(buffer, cameraPos, q);
            }
            drawCalls.clear();
        }
        MemoryPool.releaseSafe(q);


        var buff = buffer.build();

        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(() -> Bloomfog.bloomfogPositionColor);
            Bloomfog.bloomfogPositionColor.safeGetUniform("WorldTransform").set(worldTransform);
            Bloomfog.bloomfogPositionColor.safeGetUniform("u_fog").set(Bloomfog.getFogHeights());
            BeatcraftRenderer.bloomfog.loadTex();
            BufferUploader.drawWithShader(buff);
            RenderSystem.enableCull();
        }

        mirrorFramebuffer.setClearColor(0, 0, 0, 1);
        mirrorFramebuffer.clear(Minecraft.ON_OSX);

        if (!BeatcraftClient.playerConfig.doMirror()) {
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
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            return;
        };


        Minecraft.getInstance().getMainRenderTarget().unbindWrite();
        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = mirrorFramebuffer;
        mirrorFramebuffer.bindWrite(true);

        renderEarly(tessellator, cameraPos);

        buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var call : mirrorDraws) {
            call.accept(buffer, cameraPos, new Quaternionf(), true);
        }
        mirrorDraws.clear();
        buff = buffer.build();
        if (buff != null) {
            RenderSystem.setShader(() -> mirrorPositionColorClip);
            RenderSystem.setShaderTexture(0, depthFramebuffer.getDepthTextureId());
            mirrorPositionColorClip.setSampler("Sampler0", depthFramebuffer.getDepthTextureId());
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            BufferUploader.drawWithShader(buff);
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        }

        renderNotes(tessellator, cameraPos);

        renderFloorLights(tessellator, cameraPos);

        renderObstacles(tessellator, cameraPos);

        LightMesh.renderAllMirror();
        //RenderSystem.depthMask(false);
        //RenderSystem.disableCull();


        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        mirrorFramebuffer.unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);


        buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var call : drawCalls) {
            call.accept(buffer, cameraPos, invCameraRotation.conjugate(new Quaternionf()));
        }
        drawCalls.clear();

        buff = buffer.build();
        if (buff != null) {
            RenderSystem.setShader(() -> mirrorShader);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderTexture(0, mirrorFramebuffer.getColorTextureId());
            mirrorShader.setSampler("Sampler0", mirrorFramebuffer.getColorTextureId());
            RenderSystem.setShaderTexture(1, mirrorFramebuffer.getDepthTextureId());
            mirrorShader.setSampler("Sampler1", mirrorFramebuffer.getDepthTextureId());
            RenderSystem.setShaderTexture(2, BeatcraftRenderer.bloomfog.blurredBuffer.getColorTextureId());
            mirrorShader.setSampler("Sampler2", BeatcraftRenderer.bloomfog.blurredBuffer.getColorTextureId());
            mirrorShader.safeGetUniform("WorldPos").set(cameraPos);
            mirrorShader.safeGetUniform("GameTime").set(BeatcraftClient.random.nextFloat());
            BufferUploader.drawWithShader(buff);
            RenderSystem.depthMask(false);
        }

    }


}