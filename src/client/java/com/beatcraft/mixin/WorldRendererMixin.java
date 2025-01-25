package com.beatcraft.mixin;


import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.effect.SaberTrailRenderer;
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
    public void saberTrailRenderInject(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        DebugRenderer.render();
        BeatcraftRenderer.render();
        SaberTrailRenderer.renderAll();
    }

}