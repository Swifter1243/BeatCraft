package com.beatcraft.mixin.client;


import com.beatcraft.ClientRenderSubscriber;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method="render", at=@At("HEAD"))
    public void render(
            MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        ClientRenderSubscriber.onRender(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
    }
}