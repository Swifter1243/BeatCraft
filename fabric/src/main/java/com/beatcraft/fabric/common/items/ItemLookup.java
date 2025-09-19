package com.beatcraft.fabric.common.items;

import com.beatcraft.common.items.IItemLookup;
import com.beatcraft.common.items.ModItems;

public class ItemLookup implements IItemLookup {
    @Override
    public void init() {
        ModItems.SABER_ITEM = FabricItems.SABER_ITEM;
        ModItems.HEADSET_ITEM = FabricItems.HEADSET_ITEM;
    }
}
