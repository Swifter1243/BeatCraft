package com.beatcraft.client.beatmap;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.effect.MirrorHandler;
import com.beatcraft.client.render.effect.ObstacleGlowRenderer;
import com.beatcraft.client.render.gl.GlUtil;
import com.beatcraft.client.render.instancing.debug.TransformationWidgetInstanceData;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.common.utils.MathUtil;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class BeatmapRenderer {

    public enum RenderStyle {
        HEADSET,
        DISTANCE,
    }

    private final BeatmapPlayer mapController;

    public RenderStyle renderStyle;
    public boolean doSkyEffects = true;
    public boolean skipWorldRender = false;

    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> bloomfogPosColCalls = new ArrayList<>();
    public final ArrayList<Runnable> renderCalls = new ArrayList<>();
    public final ArrayList<TriConsumer<BufferBuilder, Vector3f, Integer>> obstacleRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> laserPreRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> lightRenderCalls = new ArrayList<>();
    public final ArrayList<BiConsumer<BufferBuilder, Vector3f>> arcRenderCalls = new ArrayList<>();


    public BeatmapRenderer(BeatmapPlayer map, RenderStyle style) {
        mapController = map;
        renderStyle = style;
    }


    public void recordObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        obstacleRenderCalls.add(call);
    }

    public void recordMirroredObstacleRenderCall(TriConsumer<BufferBuilder, Vector3f, Integer> call) {
        //obstacleRenderCalls.add(call);
    }

    public void recordRenderCall(Runnable call) {
        renderCalls.add(call);
    }

    public void recordArcRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        arcRenderCalls.add(call);
    }

    public void recordLaserRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserRenderCalls.add(call);
    }

    public void recordLaserPreRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        laserPreRenderCalls.add(call);
    }

    public void recordLightRenderCall(BiConsumer<BufferBuilder, Vector3f> call) {
        lightRenderCalls.add(call);
    }

    public void recordBloomfogPosColCall(BiConsumer<BufferBuilder, Vector3f> call) {
        bloomfogPosColCalls.add(call);
    }

    public void recordPlainMirrorCall(BiConsumer<BufferBuilder, Vector3f> call) {

    }

    private void renderLightDepth(Tesselator tesselator, Vector3f cameraPos) {

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = Bloomfog.lightDepth;

        Bloomfog.lightDepth.bindWrite(true);
        Bloomfog.lightDepth.setClearColor(0, 0, 0, 1);
        Bloomfog.lightDepth.clear(Minecraft.ON_OSX);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);


        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        var buff = buffer.build();
        if (buff != null) {
            BufferUploader.drawWithShader(buff);
        }

        Bloomfog.lightDepth.unbindWrite();

        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatcraftRenderer.bloomfog.overrideBuffer = false;

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

    }

    private void renderEnvironmentLights(Tesselator tesselator, Vector3f cameraPos) {
        // environment lights

        Matrix4f worldTransform = new Matrix4f();
        worldTransform.translate(cameraPos);
        worldTransform.rotate(MirrorHandler.invCameraRotation.conjugate(new Quaternionf()));

        renderLightDepth(tesselator, cameraPos);

        //renderBackgroundLights(tesselator, cameraPos, worldTransform);
        //
        //lightRenderCalls.clear();
        //if (true) return;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.setShader(() -> Bloomfog.backlightsPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        //RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        for (var call : lightRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        lightRenderCalls.clear();

        var buff = buffer.build();
        if (buff != null) {
            Bloomfog.backlightsPositionColorShader.setSampler("Sampler0", Bloomfog.lightDepth.getDepthTextureId());
            RenderSystem.setShaderTexture(0, Bloomfog.lightDepth.getDepthTextureId());
            Bloomfog.backlightsPositionColorShader.safeGetUniform("WorldTransform").set(worldTransform);
            Bloomfog.backlightsPositionColorShader.safeGetUniform("u_fog").set(Bloomfog.getFogHeights());
            BufferUploader.drawWithShader(buff);
        }

        LightMesh.renderAllSolid();

        RenderSystem.defaultBlendFunc();
    }


    private void renderFloorLightsPhase1(Tesselator tesselator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : laserPreRenderCalls) {
            call.accept(buffer, cameraPos);
        }
        laserPreRenderCalls.clear();

        var buff = buffer.build();
        if (buff == null) return;


        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);

        BufferUploader.drawWithShader(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private void renderFloorLights(Tesselator tesselator, Vector3f cameraPos) {
        // floor tiles and walls
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : laserRenderCalls) {
            call.accept(buffer, cameraPos);
        }

        laserRenderCalls.clear();

        var buff = buffer.build();
        if (buff == null) return;


        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);

        BufferUploader.drawWithShader(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }


    private void renderObstacles(Tesselator tesselator, Vector3f cameraPos) {

        if (mapController.difficulty == null) {
            obstacleRenderCalls.clear();
            return;
        }

        int color = mapController.difficulty.getSetDifficulty()
            .getColorScheme().getObstacleColor().toARGB(0.15f);
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (var call : obstacleRenderCalls) {
            call.accept(buffer, cameraPos, color);
        }
        obstacleRenderCalls.clear();

        var buff = buffer.build();

        if (buff != null) {
            ObstacleGlowRenderer.grabScreen();

            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);

            var scene = ObstacleGlowRenderer.framebuffer;

            RenderSystem.setShader(() -> ObstacleGlowRenderer.distortionShader);
            RenderSystem.setShaderTexture(0, scene.getColorTextureId());
            ObstacleGlowRenderer.distortionShader.safeGetUniform("Time").set(System.nanoTime() / 1_000_000_000f);

            buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);

            BufferUploader.drawWithShader(buff);

            RenderSystem.disableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();

        }

    }

    public void pre_render(PoseStack matrices, Difficulty difficulty, Camera camera, float distance) {

        var tesselator = Tesselator.getInstance();
        var cameraPos = camera.getPosition().toVector3f();
        renderEnvironmentLights(tesselator, cameraPos);

        // render notes
    }
    public void render(PoseStack matrices, Difficulty difficulty, Camera camera, float distance) {
        float alpha = 0;

        switch (renderStyle) {
            case DISTANCE -> {
                if (distance <= 10) {
                    alpha = 1;
                } else {
                    alpha = Math.clamp(MathUtil.inverseLerp(300, 0, (distance - 10)), 0, 1);
                }
            }
            case HEADSET -> {
                alpha = BeatcraftClient.wearingHeadset ? 1 : 0;
            }
        }


        var tesselator = Tesselator.getInstance();
        var cameraPos = camera.getPosition().toVector3f();

        renderFloorLightsPhase1(tesselator, cameraPos);

        renderObstacles(tesselator, cameraPos);

        renderFloorLights(tesselator, cameraPos);

        if (difficulty != null) {
            difficulty.render(matrices, camera, alpha);
        }

        if (BeatcraftClient.playerConfig.debug.beatmap.renderBeatmapPosition()) {
            MeshLoader.MATRIX_LOCATOR_MESH.draw(TransformationWidgetInstanceData.create(matrices.last().pose()));
        }

    }


}
