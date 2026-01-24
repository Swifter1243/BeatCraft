package com.beatcraft.mixin;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @WrapOperation(
        method = "setupColor",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"
        )
    )
    private static void applySkyModifier(float red, float green, float blue, float alpha, Operation<Void> original) {
        float mod = (float) BeatmapManager.getSkyFadeFactor();

        original.call(red*mod, green*mod, blue*mod, alpha);

    }

    @WrapOperation(
        method = "levelFogColor",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogColor(FFF)V"
        )
    )
    private static void applySkyMod(float red, float green, float blue, Operation<Void> original) {
        float mod = (float) BeatmapManager.getSkyFadeFactor();

        original.call(red*mod, green*mod, blue*mod);
    }


}
