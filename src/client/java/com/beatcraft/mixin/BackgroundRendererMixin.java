package com.beatcraft.mixin;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.environment.Environment;
import com.beatcraft.render.effect.SkyFogController;
import com.beatcraft.utils.MathUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"
        )
    )
    private static void applySkyModifier(float red, float green, float blue, float alpha, Operation<Void> original) {
        float mod = (float) SkyFogController.getColorModifier();

        original.call(red*mod, green*mod, blue*mod, alpha);

    }

    @WrapOperation(
        method = "applyFogColor",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogColor(FFF)V"
        )
    )
    private static void applySkyMod(float red, float green, float blue, Operation<Void> original) {
        float mod = (float) SkyFogController.getColorModifier();

        original.call(red*mod, green*mod, blue*mod);
    }

}
