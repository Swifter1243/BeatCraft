package com.beatcraft.mixin;

import com.beatcraft.render.BeatCraftRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(MinecraftClient.class)
public class MinecraftClientFramebufferMixin {

    @Inject(
        method="getFramebuffer",
        at = @At("HEAD"),
        cancellable = true
    )
    public void getFrameBuffer(CallbackInfoReturnable<Framebuffer> ci) {
        if (BeatCraftRenderer.bloomfog != null && BeatCraftRenderer.bloomfog.overrideBuffer && BeatCraftRenderer.bloomfog.overrideFramebuffer != null) {
            ci.setReturnValue(BeatCraftRenderer.bloomfog.overrideFramebuffer);
            ci.cancel();
        }
    }
}
