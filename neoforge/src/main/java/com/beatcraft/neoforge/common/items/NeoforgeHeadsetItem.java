package com.beatcraft.neoforge.common.items;

import com.beatcraft.common.items.HeadsetItem;
import com.beatcraft.neoforge.client.render.item.NeoforgeHeadsetItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.function.Consumer;

public class NeoforgeHeadsetItem extends HeadsetItem {
    public NeoforgeHeadsetItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new NeoforgeHeadsetItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
                );
            }
        });
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}
