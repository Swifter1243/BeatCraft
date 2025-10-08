package com.beatcraft.mixin;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.common.items.ModItems;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
            var map = BeatmapManager.getNearestFiltered(ths.player.position().toVector3f(), (bc) -> bc.trackedPlayer.equals(ths.player.getUUID()));
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

}
