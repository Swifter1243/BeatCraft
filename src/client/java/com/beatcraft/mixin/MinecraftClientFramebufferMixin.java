package com.beatcraft.mixin;

import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientFramebufferMixin {

    @Inject(
        method="getFramebuffer",
        at = @At("HEAD"),
        cancellable = true
    )
    public void getFrameBuffer(CallbackInfoReturnable<Framebuffer> ci) {
        if (BeatcraftRenderer.bloomfog.overrideBuffer) {
            ci.setReturnValue(BeatcraftRenderer.bloomfog.framebuffer);
            ci.cancel();
        }
    }
}
