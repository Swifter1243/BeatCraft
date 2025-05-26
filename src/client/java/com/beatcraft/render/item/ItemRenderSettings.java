package com.beatcraft.render.item;

import com.beatcraft.items.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class ItemRenderSettings {
    public static void init() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.SABER_ITEM, new SaberItemRenderer());
    }
}
