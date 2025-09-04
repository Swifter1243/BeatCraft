package com.beatcraft.common.items;

import com.beatcraft.Beatcraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class ModItemGroup {

    public static final ResourceKey<CreativeModeTab> GROUP_KEY = ResourceKey.create(
        BuiltInRegistries.CREATIVE_MODE_TAB.key(), Beatcraft.id("item_group")
    );

}
