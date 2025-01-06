package com.beatcraft.render.block;

import com.beatcraft.blocks.ModBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class BlockRenderSettings {


    public static void init() {

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
            ModBlocks.COLUMN_LIGHT_TILE_BLOCK,
            ModBlocks.CORNER_LIGHT_TILE_BLOCK,
            ModBlocks.EDGE_LIGHT_TILE_BLOCK,
            ModBlocks.END_LIGHT_TILE_BLOCK,
            ModBlocks.FILLED_LIGHT_TILE_BLOCK
        );

    }

}
