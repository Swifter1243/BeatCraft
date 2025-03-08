package com.beatcraft.mixin.vivecraft;

import com.beatcraft.logic.InputSystem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(MCVR.class)
public abstract class MCVRMixin {

    @WrapOperation(
        method = "processBindings",
        at = @At(
            value = "INVOKE",
            target = "Lorg/vivecraft/client_vr/provider/MCVR;changeHotbar(I)V"
        ),
        remap = false
    )
    private void hotbarLock(MCVR instance, int dir, Operation<Void> original) {
        if (!InputSystem.isHotbarLocked()) {
            original.call(instance, dir);
        }
    }


}
