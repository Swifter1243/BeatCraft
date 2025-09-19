package com.beatcraft.fabric.common.items;

import com.beatcraft.common.items.ModItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FabricItemsGroup {

    public static final CreativeModeTab GROUP = FabricItemGroup.builder().icon(
        () -> new ItemStack(FabricItems.SABER_ITEM)
    ).title(
        Component.translatable("itemGroup.beatcraft.group")
    ).build();

    public static void init() {

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModItemGroup.GROUP_KEY, GROUP);

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.GROUP_KEY).register(group -> {
            group.accept(FabricItems.SABER_ITEM);
            group.accept(FabricItems.HEADSET_ITEM);
        });

    }

}
