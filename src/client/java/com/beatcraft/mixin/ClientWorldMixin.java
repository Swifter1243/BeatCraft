package com.beatcraft.mixin;

import com.beatcraft.render.effect.SkyFogController;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @ModifyReturnValue(
        method = "getSkyColor",
        at = @At(
            "RETURN"
        )
    )
    private Vec3d applySkyModifier(Vec3d original) {
        return original.multiply(SkyFogController.getColorModifier());
    }


}
