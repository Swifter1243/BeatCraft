package com.beatcraft.mixin;

import com.beatcraft.client.beatmap.BeatmapManager;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @ModifyReturnValue(
        method = "getSkyColor",
        at = @At(
            "RETURN"
        )
    )
    private Vec3 applySkyModifier(Vec3 original) {
        var f = BeatmapManager.getSkyFadeFactor();
        return original.multiply(f, f, f);
    }

    @ModifyReturnValue(
        method = "getStarBrightness",
        at = @At("RETURN")
    )
    private float getSkyFogStarBrightness(float original) {
        return original * (float) BeatmapManager.getSkyFadeFactor();
    }
}
