package com.beatcraft.neoforge.client.render.item;

import com.beatcraft.client.render.item.SaberItemRenderer;
import com.beatcraft.neoforge.common.items.NeoforgeItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NeoforgeSaberItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final SaberItemRenderer renderer;
    public NeoforgeSaberItemRenderer(BlockEntityRenderDispatcher arg, EntityModelSet arg2) {
        super(arg, arg2);
        renderer = new SaberItemRenderer();
    }

    @Override
    public void renderByItem(ItemStack arg, @NotNull ItemDisplayContext itemDisplayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, int j) {
        if (arg.is(NeoforgeItems.SABER_ITEM)) {
            renderer.render(arg, itemDisplayContext, poseStack, multiBufferSource, i, j);
        }
    }
}
