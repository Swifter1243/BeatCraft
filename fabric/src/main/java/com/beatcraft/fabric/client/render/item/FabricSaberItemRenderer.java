package com.beatcraft.fabric.client.render.item;

import com.beatcraft.client.render.item.SaberItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FabricSaberItemRenderer extends SaberItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    @Override
    public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        super.render(itemStack, itemDisplayContext, poseStack, multiBufferSource, i, i1);
    }
}
