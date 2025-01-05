package com.beatcraft.render.item;

import com.beatcraft.items.ModItems;
import com.beatcraft.render.item.renderers.SaberItemRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public class GeckolibRenderInit {

    public static void init() {
        ModItems.SABER_ITEM.renderProvider.setValue(new GeoRenderProvider() {
            private SaberItemRenderer renderer;
            @Override
            public BuiltinModelItemRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new SaberItemRenderer();
                }
                return renderer;
            }
        });
    }

}
