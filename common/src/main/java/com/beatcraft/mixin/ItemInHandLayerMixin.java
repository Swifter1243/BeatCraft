package com.beatcraft.mixin;

import com.beatcraft.client.render.effect.SaberRenderer;
import com.beatcraft.common.items.ModItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @WrapOperation(
        method = "renderArmWithItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
        )
    )
    private void beatcraft$overrideSaber(ItemInHandRenderer instance, LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        var override = (itemStack.is(ModItems.SABER_ITEM) && (itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND));

        if (override) {
            poseStack.pushPose();
            SaberRenderer.transformSaber(poseStack);
        }
        original.call(instance, livingEntity, itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i);
        if (override) {
            poseStack.popPose();
        }
    }

}
