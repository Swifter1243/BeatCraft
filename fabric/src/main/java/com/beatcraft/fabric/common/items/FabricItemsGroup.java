package com.beatcraft.fabric.common.items;

import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.items.ModItemGroup;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.common.items.SaberItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
            var redSaber = new ItemStack(Holder.direct(FabricItems.SABER_ITEM), 1, DataComponentPatch.builder()
                .set(ModComponents.SABER_COLOR_COMPONENT.get(), 12595248)
                .set(ModComponents.AUTO_SYNC_COLOR.get(), 0)
                .build()
            );
            var blueSaber = new ItemStack(Holder.direct(FabricItems.SABER_ITEM), 1, DataComponentPatch.builder()
                .set(ModComponents.SABER_COLOR_COMPONENT.get(), 2122920)
                .set(ModComponents.AUTO_SYNC_COLOR.get(), 1)
                .build()
            );
            group.accept(redSaber);
            group.accept(blueSaber);
        });

    }

}
