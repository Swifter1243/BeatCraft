package com.beatcraft.mixin.vivecraft;

import com.beatcraft.logic.InputSystem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(value = MCVR.class, remap = false)
public abstract class MCVRMixin {

    @Shadow protected ClientDataHolderVR dh;

    @Shadow public float seatedRot;

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

    @ModifyExpressionValue(
        method = "processBindings",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z",
            ordinal = 1,
            remap = true
        ),
        remap = false
    )
    private boolean lockMovement(boolean original) {
        return !InputSystem.isMovementLocked() && original;
    }

    @ModifyExpressionValue(
        method = "processBindings",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z",
            ordinal = 2,
            remap = true
        ),
        remap = false
    )
    private boolean lockRotation(boolean original) {
        return !InputSystem.isMovementLocked() && original;
    }

    @Unique
    private float beatcraft$worldRotation;

    @Inject(
        method = "processBindings",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;toDegrees(D)D",
            ordinal = 1
        ),
        remap = false
    )
    private void storeOldRotation(CallbackInfo ci) {
        beatcraft$worldRotation = this.dh.vrSettings.worldRotation;
    }



    @Inject(
        method = "processBindings",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z",
            ordinal = 20,
            remap = true
        ),
        remap = false
    )
    private void resetWorldPos(CallbackInfo ci) {
        if (InputSystem.isMovementLocked()) {
            this.dh.vrSettings.worldRotation = beatcraft$worldRotation;
            this.seatedRot = beatcraft$worldRotation;
        }
    }

}
