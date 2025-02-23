package com.beatcraft.mixin;

import com.beatcraft.items.ModItems;
import com.beatcraft.render.effect.SaberRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.api_beta.client.VivecraftClientAPI;

@Mixin(value = HeldItemRenderer.class, priority = 990)
public abstract class HeldItemRendererMixin {


    @Shadow public abstract void renderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light);

    @ModifyVariable(
        method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"), index = 6, ordinal = 0,
        argsOnly = true
    )
    private ItemStack beatcraft$overrideSaberRender(ItemStack stack, AbstractClientPlayerEntity player, @Local(argsOnly = true) MatrixStack matrices, @Local(argsOnly = true) VertexConsumerProvider vertexConsumerProvider, @Local(argsOnly = true) Hand hand, @Local(ordinal = 0, argsOnly = true) float tickDelta) {
        if (player == MinecraftClient.getInstance().player && VivecraftClientAPI.getInstance().isVrActive()) {
            if (stack.isOf(ModItems.SABER_ITEM)) {
                SaberRenderer.renderSaber(stack, matrices, (VertexConsumerProvider.Immediate) vertexConsumerProvider, hand, player, tickDelta);
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

}
