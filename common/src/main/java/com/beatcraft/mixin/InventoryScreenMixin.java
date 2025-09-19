package com.beatcraft.mixin;

import com.beatcraft.client.render.item.HeadsetItemRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Inject(
        method = "renderEntityInInventoryFollowsMouse",
        at = @At("HEAD")
    )
    private static void startRender(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity, CallbackInfo ci) {
        HeadsetItemRenderer.renderingInventoryEntity = true;
    }

    @Inject(
        method = "renderEntityInInventoryFollowsMouse",
        at = @At("TAIL")
    )
    private static void endRender(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity, CallbackInfo ci) {
        HeadsetItemRenderer.renderingInventoryEntity = false;
    }

}
