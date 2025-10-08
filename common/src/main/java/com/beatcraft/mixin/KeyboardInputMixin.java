package com.beatcraft.mixin;

import com.beatcraft.client.logic.InputSystem;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void lockMovement(boolean bl, float f, CallbackInfo ci) {
        if (InputSystem.isMovementLocked()) {
            var ths = ((KeyboardInput) ((Object) this));
            ths.forwardImpulse = 0;
            ths.leftImpulse = 0;
            ths.jumping = false;
        }
    }

}
