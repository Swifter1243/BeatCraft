package com.beatcraft.neoforge.common.items;

import com.beatcraft.common.items.IItemLookup;
import com.beatcraft.common.items.ModItems;

public class ItemLookup implements IItemLookup {
    @Override
    public void init() {
        ModItems.SABER_ITEM = NeoforgeItems.SABER_ITEM.asItem();
        ModItems.HEADSET_ITEM = NeoforgeItems.HEADSET_ITEM.asItem();
    }
}
