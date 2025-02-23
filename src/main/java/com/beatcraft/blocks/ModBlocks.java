package com.beatcraft.blocks;

import com.beatcraft.BeatCraft;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final BlackMirrorBlock BLACK_MIRROR_BLOCK = (BlackMirrorBlock) register(new BlackMirrorBlock(), "black_mirror_block");


    public static final FilledLightTileBlock FILLED_LIGHT_TILE_BLOCK = (FilledLightTileBlock) register(new FilledLightTileBlock(), "filled_light_tile");
    public static final EdgeLightTileBlock EDGE_LIGHT_TILE_BLOCK = (EdgeLightTileBlock) register(new EdgeLightTileBlock(), "edge_light_tile");
    public static final CornerLightTileBlock CORNER_LIGHT_TILE_BLOCK = (CornerLightTileBlock) register(new CornerLightTileBlock(), "corner_light_tile");
    public static final ColumnLightTileBlock COLUMN_LIGHT_TILE_BLOCK = (ColumnLightTileBlock) register(new ColumnLightTileBlock(), "column_light_tile");
    public static final EndLightTileBlock END_LIGHT_TILE_BLOCK = (EndLightTileBlock) register(new EndLightTileBlock(), "end_light_tile");



    private static Block register(Block block, String path) {
        Registry.register(Registries.BLOCK, Identifier.of(BeatCraft.MOD_ID, path), block);
        Registry.register(Registries.ITEM, Identifier.of(BeatCraft.MOD_ID, path), new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void init() {

    }

}
