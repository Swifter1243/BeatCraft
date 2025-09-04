package com.beatcraft.common.items;

import net.minecraft.world.item.Item;

import java.util.ServiceLoader;

public class ModItems {
    private static IItemLookup lookup = ServiceLoader.load(IItemLookup.class).findFirst().orElseThrow(() -> new RuntimeException("Could not initialize items"));

    public static Item SABER_ITEM;

    public static void init() {
        lookup.init();
    }

}
