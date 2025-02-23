package com.beatcraft.items;

import com.beatcraft.BeatCraft;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final SaberItem SABER_ITEM = (SaberItem) register(new SaberItem(), "saber");

    public static Item register(Item item, String id) {
        return Registry.register(Registries.ITEM, Identifier.of(BeatCraft.MOD_ID, id), item);
    }

    public static void init() {

    }

}
