package com.beatcraft.neoforge.common.items;

import com.beatcraft.Beatcraft;
import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.items.SaberItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoforgeItemsGroup {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beatcraft.MOD_ID);

    public static final Supplier<CreativeModeTab> ITEMS_TAB = CREATIVE_TABS.register("item_group", () -> CreativeModeTab
        .builder()
        .icon(() -> new ItemStack(NeoforgeItems.SABER_ITEM.get()))
        .title(Component.translatable("itemGroup.beatcraft.group"))
        .displayItems((params, output) -> {
            output.accept(NeoforgeItems.SABER_ITEM);
            output.accept(NeoforgeItems.HEADSET_ITEM);
            var redSaber = new ItemStack(Holder.direct(NeoforgeItems.SABER_ITEM.asItem()), 1, DataComponentPatch.builder()
                .set(ModComponents.SABER_COLOR_COMPONENT.get(), 12595248)
                .set(ModComponents.AUTO_SYNC_COLOR.get(), 0)
                .build()
            );
            var blueSaber = new ItemStack(Holder.direct(NeoforgeItems.SABER_ITEM.asItem()), 1, DataComponentPatch.builder()
                .set(ModComponents.SABER_COLOR_COMPONENT.get(), 2122920)
                .set(ModComponents.AUTO_SYNC_COLOR.get(), 1)
                .build()
            );
            output.accept(redSaber);
            output.accept(blueSaber);
        })
        .build()
    );

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

}
