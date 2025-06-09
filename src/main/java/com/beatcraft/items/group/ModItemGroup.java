package com.beatcraft.items.group;

import com.beatcraft.BeatCraft;
import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.items.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    public static final RegistryKey<ItemGroup> GROUP_KEY = RegistryKey.of(
        Registries.ITEM_GROUP.getKey(), Identifier.of(BeatCraft.MOD_ID, "item_group")
    );
    public static final ItemGroup GROUP = FabricItemGroup.builder().icon(
        () -> new ItemStack(ModItems.SABER_ITEM)
    ).displayName(
        Text.translatable("itemGroup.beatcraft.group")
    ).build();

    public static void init() {

        Registry.register(Registries.ITEM_GROUP, GROUP_KEY, GROUP);

        ItemGroupEvents.modifyEntriesEvent(GROUP_KEY).register(group -> {
            group.add(ModItems.SABER_ITEM);

            group.add(ModBlocks.BLACK_MIRROR_BLOCK);

            group.add(ModBlocks.FILLED_LIGHT_TILE_BLOCK);
            group.add(ModBlocks.EDGE_LIGHT_TILE_BLOCK);
            group.add(ModBlocks.CORNER_LIGHT_TILE_BLOCK);
            group.add(ModBlocks.COLUMN_LIGHT_TILE_BLOCK);
            group.add(ModBlocks.END_LIGHT_TILE_BLOCK);

            group.add(ModBlocks.REFLECTIVE_MIRROR_BLOCK);
            group.add(ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK);
            group.add(ModBlocks.COLOR_NOTE_DISPLAY_BLOCK);
        });
    }

}
