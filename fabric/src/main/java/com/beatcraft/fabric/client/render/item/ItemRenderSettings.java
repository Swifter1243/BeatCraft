package com.beatcraft.fabric.client.render.item;

import com.beatcraft.fabric.common.items.FabricItems;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class ItemRenderSettings {

    public static void init() {
        BuiltinItemRendererRegistry.INSTANCE.register(FabricItems.SABER_ITEM, new FabricSaberItemRenderer());
    }

}
