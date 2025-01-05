package com.beatcraft.render.item.models;

import com.beatcraft.BeatCraft;
import com.beatcraft.items.SaberItem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;

public class SaberItemModel extends GeoModel<SaberItem> {

    // methods are deprecated, but you need them to satisfy extending GeoModel, so I hate it, but I don't think there's an easy way around it
    @Override
    public Identifier getModelResource(SaberItem saberItem) {
        return Identifier.of(BeatCraft.MOD_ID, "geo/saber.geo.json");
    }

    @Override
    public Identifier getTextureResource(SaberItem saberItem) {
        return Identifier.of(BeatCraft.MOD_ID, "textures/item/saber.png");
    }

    @Override
    public Identifier getAnimationResource(SaberItem saberItem) {
        return Identifier.of(BeatCraft.MOD_ID, "animations/saber.animation.json");
    }


    @Override
    public @Nullable RenderLayer getRenderType(SaberItem animatable, Identifier texture) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}
