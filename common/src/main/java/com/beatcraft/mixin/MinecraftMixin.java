package com.beatcraft.mixin;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.logic.InputSystem;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.items.ModItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
        method = "startAttack",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fpfcInputOverride(CallbackInfoReturnable<Boolean> cir) {
        var ths = ((Minecraft) ((Object) this));
        assert ths.player != null;
        if (BeatmapManager.isTracked(ths.player.getUUID()) && ths.player.getMainHandItem().is(ModItems.SABER_ITEM)) {
            var map = BeatmapManager.getNearestFiltered(ths.player.position().toVector3f(), (bc) -> bc.trackedPlayer != null && bc.trackedPlayer.equals(ths.player.getUUID()));
            if (map == null) return;
            map.hudRenderer.triggerPressed = true;
            map.hudRenderer.pointerSaber = NoteType.BLUE;
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
        method = "runTick",
        at = @At("HEAD")
    )
    private void resetTriggerPress(boolean bl, CallbackInfo ci) {
        for (var map : BeatmapManager.beatmaps) {
            map.hudRenderer.triggerPressed = false;
        }
    }

    @WrapOperation(
        method = "handleKeybinds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            ordinal = 1
        )
    )
    private void mapPauseInject(Minecraft instance, Screen screen, Operation<Void> original) {
        boolean inVr = (ClientDataHolderVR.getInstance().vr != null && ClientDataHolderVR.getInstance().vr.isActive());
        if (!inVr) {
            original.call(instance, screen);
            return;
        }
        assert Minecraft.getInstance().player != null;
        var player = Minecraft.getInstance().player;
        if (BeatmapManager.isTracked(player.getUUID())) {
            var nearest = BeatmapManager.getNearestFiltered(player.getPosition(0).toVector3f(), map -> map.trackedPlayer != null && map.trackedPlayer.equals(player.getUUID()));

            if (nearest == null) return;

            if (nearest.scene == HUDRenderer.MenuScene.InGame) {
                InputSystem.unlockHotbar();
                nearest.pause();
            } else if (nearest.scene == HUDRenderer.MenuScene.Paused) {
                nearest.resume();
            } else {
                original.call(instance, screen);
            }

        }
    }

}
