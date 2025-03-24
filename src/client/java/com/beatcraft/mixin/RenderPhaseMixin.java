package com.beatcraft.mixin;

import com.beatcraft.BeatCraft;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPhase.class)
public abstract class RenderPhaseMixin {
    @Mutable
    @Shadow @Final private Runnable beginAction;


    //@Inject(
    //    method = "<init>",
    //    at = @At("TAIL")
    //)
    //private void injectBloomfog(String name, Runnable beginAction, Runnable endAction, CallbackInfo ci) {
    //    if (name.equals("bloomfog_solid")) {
    //        this.beginAction = () -> {
    //            beginAction.run();
    //            RenderSystem.setShader(() -> Bloomfog.bloomfogSolidShader);
    //            BeatCraftRenderer.bloomfog.loadTexSecondary();
    //            Bloomfog.bloomfogSolidShader.addSampler("Sampler1", BeatCraftRenderer.bloomfog.blurredBuffer.getColorAttachment());
    //        };
    //    }
    //}
}
