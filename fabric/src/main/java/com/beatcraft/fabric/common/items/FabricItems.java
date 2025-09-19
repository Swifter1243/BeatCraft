package com.beatcraft.fabric.common.items;

import com.beatcraft.Beatcraft;
import com.beatcraft.common.items.HeadsetItem;
import com.beatcraft.common.items.SaberItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;

public class FabricItems {

    public static final SaberItem SABER_ITEM = (SaberItem) register(new SaberItem(new Item.Properties().stacksTo(1)), "saber");
    public static final HeadsetItem HEADSET_ITEM = (HeadsetItem) register(new HeadsetItem(new Item.Properties().stacksTo(1).equipmentSlot((e, s) -> EquipmentSlot.HEAD)), "vr_headset");

    public static Item register(Item item, String id) {
        return Registry.register(BuiltInRegistries.ITEM, Beatcraft.id(id), item);
    }

    public static void init() {}

}
