package com.beatcraft.mixin;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.render.effect.SaberRenderer;
import com.beatcraft.client.render.item.HeadsetItemRenderer;
import com.beatcraft.common.items.ModItems;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.api_beta.client.VivecraftClientAPI;

@Mixin(value = ItemInHandRenderer.class, priority = 990)
public class ItemInHandRendererMixin {

    @ModifyVariable(
        method = "renderArmWithItem",
        at = @At("HEAD"), index = 6, ordinal = 0,
        argsOnly = true
    )
    private ItemStack beatcraft$overrideSaberRender(ItemStack stack, AbstractClientPlayer player, @Local(argsOnly = true) PoseStack matrices, @Local(argsOnly = true) MultiBufferSource vertexConsumerProvider, @Local(argsOnly = true) InteractionHand hand, @Local(ordinal = 0, argsOnly = true) float tickDelta) {
        if (player == Minecraft.getInstance().player && VivecraftClientAPI.getInstance().isVrActive()) {
            if (stack.is(ModItems.SABER_ITEM)) {
                SaberRenderer.renderSaber(stack, matrices, vertexConsumerProvider, hand, player, tickDelta);
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

}
