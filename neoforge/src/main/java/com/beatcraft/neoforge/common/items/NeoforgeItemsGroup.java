package com.beatcraft.neoforge.common.items;

import com.beatcraft.Beatcraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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
        })
        .build()
    );

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }

}
