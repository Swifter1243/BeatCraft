package com.beatcraft.mixin;


import com.beatcraft.logic.HapticsHandler;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.effect.MirrorHandler;
import com.beatcraft.render.effect.SkyFogController;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.effect.SaberRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(WorldRenderer.class)
//@Debug(export = true)
public abstract class WorldRendererMixin {
    @Shadow protected abstract void renderLayer(RenderLayer renderLayer, double x, double y, double z, Matrix4f matrix4f, Matrix4f positionMatrix);

    @Inject(method="render", at=@At("HEAD"))
    public void render(
        RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci
    ) {
        if (BeatCraftRenderer.bloomfog == null) BeatCraftRenderer.init();
        SkyFogController.updateColor();
        BeatCraftRenderer.onRender(new MatrixStack(), camera, tickCounter.getTickDelta(true));
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V"
        )
    )
    public void bloomFogInject(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (BeatCraftRenderer.bloomfog != null) {
            BeatCraftRenderer.bloomfog.render(false, tickCounter.getTickDelta(true));
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V"
        )
    )
    public void endFrameInject(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, @Local VertexConsumerProvider.Immediate immediate) {
        MirrorHandler.drawMirror();
        BeatCraftRenderer.earlyRender(immediate);
        DebugRenderer.render();
        HUDRenderer.render(immediate);
        BeatcraftParticleRenderer.renderParticles();
        BeatCraftRenderer.render();
        SaberRenderer.renderAll();
        BeatCraftRenderer.bloomfog.renderBloom();
        HapticsHandler.endFrame();
    }

    @WrapOperation(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V",
            ordinal = 1
        )
    )
    private void overrideSunMoonShader(Supplier<ShaderProgram> program, Operation<Void> original) {
        original.call((Supplier<ShaderProgram>) GameRenderer::getPositionTexColorProgram);
    }

    @WrapOperation(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Tessellator;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)Lnet/minecraft/client/render/BufferBuilder;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;")
        )
    )
    private BufferBuilder modifyBufferFormat(Tessellator instance, VertexFormat.DrawMode drawMode, VertexFormat format, Operation<BufferBuilder> original) {
        return instance.begin(drawMode, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/BufferBuilder;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;",
            shift = At.Shift.AFTER
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;SUN:Lnet/minecraft/util/Identifier;")
        )
    )
    private void addColorData(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci, @Local BufferBuilder bufferBuilder2) {
        var fade = SkyFogController.getColorModifier();
        var c = (int)(255 * fade);
        var color = (c << 24) + 0xFFFFFF;
        bufferBuilder2.color(color);
    }

}