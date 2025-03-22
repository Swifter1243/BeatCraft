package com.beatcraft.mixin;


import com.beatcraft.logic.HapticsHandler;
import com.beatcraft.render.BeatCraftRenderLayers;
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
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
@Debug(export = true)
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
            BeatCraftRenderer.bloomfog.render(false);
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
        HapticsHandler.endFrame();
    }

    //@WrapOperation(
    //    method = "render",
    //    at = @At(
    //        value = "INVOKE",
    //        target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
    //        ordinal = 0
    //    )
    //)
    //private void startBloomfogSolidRender(WorldRenderer instance, RenderLayer renderLayer, double x, double y, double z, Matrix4f matrix4f, Matrix4f positionMatrix, Operation<Void> original) {
    //    original.call(instance, renderLayer, x, y, z, matrix4f, positionMatrix);
    //    //renderLayer(BeatCraftRenderLayers.getBloomfogSolid(), x, y, z, matrix4f, positionMatrix);
    //
    //}
    //
    //@Inject(
    //    method = "render",
    //    at = @At(
    //        value = "INVOKE",
    //        target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw(Lnet/minecraft/client/render/RenderLayer;)V",
    //        ordinal = 4
    //    )
    //)
    //private void injectBloomfogSolid(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, @Local VertexConsumerProvider.Immediate immediate) {
    //    //immediate.draw(BeatCraftRenderLayers.getBloomfogSolid());
    //}


}