package com.beatcraft.mixin;

import com.beatcraft.BeatCraftClient;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
@Debug(export = true)
public class GameRendererMixin {

    @Inject(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Matrix4f;<init>()V"
        )
    )
    public void overridePlayerCameraPos(RenderTickCounter tickCounter, CallbackInfo ci, @Local Camera camera) {
        camera.pos = camera.pos.add(BeatCraftClient.playerCameraPosition).add(BeatCraftClient.playerGlobalPosition);
        camera.blockPos.set(camera.pos.x, camera.pos.y, camera.pos.z);
        camera.getRotation().add(BeatCraftClient.playerCameraRotation).add(BeatCraftClient.playerGlobalRotation);
    }

}
