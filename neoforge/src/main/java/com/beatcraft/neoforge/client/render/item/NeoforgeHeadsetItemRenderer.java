package com.beatcraft.neoforge.client.render.item;

import com.beatcraft.client.render.item.HeadsetItemRenderer;
import com.beatcraft.neoforge.common.items.NeoforgeItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NeoforgeHeadsetItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final HeadsetItemRenderer renderer;
    public NeoforgeHeadsetItemRenderer(BlockEntityRenderDispatcher arg, EntityModelSet arg2) {
        super(arg, arg2);
        renderer = new HeadsetItemRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext itemDisplayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, int j) {
        if (stack.is(NeoforgeItems.HEADSET_ITEM)) {
            renderer.render(stack, itemDisplayContext, poseStack, multiBufferSource, i, j);
        }
    }
}
