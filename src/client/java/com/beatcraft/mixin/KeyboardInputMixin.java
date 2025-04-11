package com.beatcraft.mixin;

import com.beatcraft.logic.InputSystem;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {


    @Inject(
        method = "tick",
        at = @At(
            value = "TAIL"
        )
    )
    private void lockMovement(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (InputSystem.isMovementLocked()) {
            this.movementForward = 0;
            this.movementSideways = 0;
            this.jumping = false;
        }
    }

}
