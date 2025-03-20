package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.mesh.MeshLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public static ShaderProgram mirrorShader;

    public static void init() {
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer = new SimpleFramebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);

        mirrorBloomfog = new Bloomfog(false);

        try {
            mirrorShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "light_mirror", VertexFormats.POSITION_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resize() {
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer.resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), true);
        mirrorBloomfog.resize(window.getWidth(), window.getHeight(), false);
    }

    public static void recordMirrorLightDraw(Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        mirrorDraws.add(call);
    }

    public static void recordMirrorNoteDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        mirrorNotes.add(call);
    }

    public static void recordMirrorArrowDraw(BiConsumer<BufferBuilder, Vector3f> call) {
        mirrorArrows.add(call);
    }

    public static void recordMirrorLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        mirrorWallGlows.add(call);
    }

    public static void recordEarlyRenderCall(Runnable call) {
        earlyCalls.add(call);
    }

    public static void recordCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        drawCalls.add(call);
    }

    public static void recordPlainCall(BiConsumer<BufferBuilder, Vector3f> call) {
        plainMirrorCalls.add(call);
    }

    public static void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
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
        // notes and debris
        BufferBuilder triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

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
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        var triBuff = triBuffer.endNullable();
        if (triBuff != null) {
            BufferRenderer.drawWithGlobalProgram(triBuff);
        }

        triBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.setShader(() -> BeatcraftRenderer.arrowShader);
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

    public static void drawMirror() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();


        // render mirror block non-reflective faces
        for (var call : plainMirrorCalls) {
            call.accept(buffer, cameraPos);
        }
        plainMirrorCalls.clear();

        var buff = buffer.endNullable();

        if (buff != null) {
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(() -> Bloomfog.bloomfogPositionColor);
            BeatcraftRenderer.bloomfog.loadTex();
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.enableCull();
        }


        mirrorFramebuffer.setClearColor(0, 0, 0, 1);
        mirrorFramebuffer.clear(true);

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = mirrorFramebuffer;
        mirrorFramebuffer.beginWrite(true);

        renderEarly(tessellator, cameraPos);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : mirrorDraws) {
            call.accept(buffer, cameraPos, invCameraRotation, true);
        }
        mirrorDraws.clear();
        buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();

        }

        renderNotes(tessellator, cameraPos);

        renderFloorLights(tessellator, cameraPos);

        renderObstacles(tessellator, cameraPos);

        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
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
            RenderSystem.setShaderTexture(0, mirrorFramebuffer.getColorAttachment());
            mirrorShader.addSampler("Sampler0", mirrorFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, mirrorFramebuffer.getDepthAttachment());
            mirrorShader.addSampler("Sampler1", mirrorFramebuffer.getDepthAttachment());
            mirrorShader.getUniformOrDefault("WorldPos").set(cameraPos);
            mirrorShader.getUniformOrDefault("GameTime").set(BeatCraftClient.random.nextFloat());
            BufferRenderer.drawWithGlobalProgram(buff);
        }

    }


}
