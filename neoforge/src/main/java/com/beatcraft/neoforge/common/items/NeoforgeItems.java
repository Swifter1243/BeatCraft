package com.beatcraft.neoforge.common.items;

import com.beatcraft.Beatcraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoforgeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Beatcraft.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredItem<Item> SABER_ITEM = ITEMS.register(
        "saber",
        () -> new NeoforgeSaberItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<Item> HEADSET_ITEM = ITEMS.register(
        "vr_headset",
        () -> new NeoforgeHeadsetItem(new Item.Properties().stacksTo(1))
    );



}
