package com.beatcraft.mixin;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Debug(export = true)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method="renderLevel", at=@At("HEAD"))
    public void renderSky(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        BeatcraftRenderer.renderSky(camera, deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            ordinal = 0
        )
    )
    public void renderBloomfog(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        BeatcraftRenderer.renderBloomfog(deltaTracker.getGameTimeDeltaPartialTick(true));
    }


    @Inject(
        method = "renderLevel",
        at  = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            ordinal = 1
        )
    )
    public void renderHUD(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local MultiBufferSource.BufferSource bufferSource) {
        BeatcraftRenderer.renderMirror();
        BeatcraftRenderer.renderHUD(bufferSource);
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V"
        )
    )
    public void renderBeatmap(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        var cameraPos = camera.getPosition().toVector3f();
        BeatcraftRenderer.renderBeatmap(camera);
        BeatcraftRenderer.renderDebug(cameraPos);
        BeatcraftRenderer.renderParticles();
        BeatcraftRenderer.renderSabers();
        BeatcraftRenderer.renderSmoke();
        BeatcraftRenderer.renderBloom();
    }


    @WrapOperation(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V",
            ordinal = 1
        )
    )
    private void overrideSunMoonShader(Supplier<ShaderInstance> program, Operation<Void> original) {
        original.call((Supplier<ShaderInstance>) GameRenderer::getPositionTexColorShader);
    }

    @WrapOperation(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/Tesselator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;SUN_LOCATION:Lnet/minecraft/resources/ResourceLocation;", opcode = Opcodes.GETSTATIC)
        )
    )
    private BufferBuilder modifyBufferFormat(Tesselator instance, VertexFormat.Mode drawMode, VertexFormat format, Operation<BufferBuilder> original) {
        return instance.begin(drawMode, DefaultVertexFormat.POSITION_TEX_COLOR);

    }

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;addVertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
            shift = At.Shift.AFTER
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;SUN_LOCATION:Lnet/minecraft/resources/ResourceLocation;", opcode = Opcodes.GETSTATIC)
        )
    )
    private void addColorData(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci, @Local BufferBuilder bufferBuilder2) {
        var fade = BeatmapManager.getSkyFadeFactor();
        var c = (int)(255 * fade);
        var color = (c << 24) + 0xFFFFFF;
        bufferBuilder2.setColor(color);
    }

}
