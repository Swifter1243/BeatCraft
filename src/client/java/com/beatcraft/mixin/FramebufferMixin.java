package com.beatcraft.mixin;

import com.beatcraft.BeatCraft;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    @ModifyArg(
        method = "clear",
        order = 2000,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V"
        )
    )
    private boolean allowBloomfogResize(boolean setViewport) {
        return setViewport || Bloomfog.bloomfog_resize;
    }
}
