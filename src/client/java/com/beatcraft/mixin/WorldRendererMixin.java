package com.beatcraft.mixin;


import com.beatcraft.logic.HapticsHandler;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.effect.BeatcraftParticleRenderer;
import com.beatcraft.render.effect.SaberRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method="render", at=@At("HEAD"))
    public void render(
        RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci
    ) {
        BeatcraftRenderer.onRender(new MatrixStack(), camera);
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V"
        )
    )
    public void endFrameInject(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci, @Local VertexConsumerProvider.Immediate immediate) {
        BeatcraftRenderer.earlyRender(immediate);
        DebugRenderer.render();
        HUDRenderer.render(immediate);
        BeatcraftParticleRenderer.renderParticles();
        BeatcraftRenderer.render();
        SaberRenderer.renderAll();
        HapticsHandler.endFrame();
    }

}