package com.beatcraft.mixin;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.InputSystem;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(MinecraftClient.class)
@Debug(export = true)
public class MinecraftClientMixin {

    @WrapOperation(
        method = "handleInputEvents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            ordinal = 1
        )
    )
    private void mapPauseInject(MinecraftClient instance, Screen screen, Operation<Void> original) {
        boolean inVr = (ClientDataHolderVR.getInstance().vr != null && ClientDataHolderVR.getInstance().vr.isActive());

        if (inVr && HUDRenderer.scene == HUDRenderer.MenuScene.InGame) {
            InputSystem.unlockHotbar();
            GameLogicHandler.pauseMap();
        } else if (inVr && HUDRenderer.scene == HUDRenderer.MenuScene.Paused) {
            GameLogicHandler.unpauseMap();
        } else {
            original.call(instance, screen);
        }
    }

}
