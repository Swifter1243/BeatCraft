package com.beatcraft.render.block;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.render.BeatCraftRenderLayers;
import com.beatcraft.render.block.entity.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockRenderSettings {


    public static void init() {

        //BlockRenderLayerMap.INSTANCE.putBlocks(BeatCraftRenderLayers.getBloomfogSolid(),
        //    ModBlocks.BLACK_MIRROR_BLOCK
        //);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
            ModBlocks.COLUMN_LIGHT_TILE_BLOCK,
            ModBlocks.CORNER_LIGHT_TILE_BLOCK,
            ModBlocks.EDGE_LIGHT_TILE_BLOCK,
            ModBlocks.END_LIGHT_TILE_BLOCK,
            ModBlocks.FILLED_LIGHT_TILE_BLOCK
        );

        BlockEntityRendererFactories.register(ModBlocks.REFLECTIVE_MIRROR_BLOCK_ENTITY, ctx -> new ReflectiveMirrorBlockEntityRenderer());
        BlockEntityRendererFactories.register(ModBlocks.EDGE_LIGHT_BLOCK_ENTITY_TYPE, ctx -> new EdgeLightTileBlockEntityRenderer());
        BlockEntityRendererFactories.register(ModBlocks.CORNER_LIGHT_BLOCK_ENTITY_TYPE, ctx -> new CornerLightTileBlockEntityRenderer());
        BlockEntityRendererFactories.register(ModBlocks.END_LIGHT_BLOCK_ENTITY_TYPE, ctx -> new EndLightTileBlockEntityRenderer());
        BlockEntityRendererFactories.register(ModBlocks.FILLED_LIGHT_BLOCK_ENTITY_TYPE, ctx -> new FilledLightTileBlockEntityRenderer());
        BlockEntityRendererFactories.register(ModBlocks.COLUMN_LIGHT_BLOCK_ENTITY_TYPE, ctx -> new ColumnLightTileBlockEntityRenderer());

    }

}
