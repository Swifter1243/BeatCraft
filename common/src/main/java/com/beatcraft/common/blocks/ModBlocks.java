package com.beatcraft.common.blocks;

import net.minecraft.world.level.block.Block;

import java.util.ServiceLoader;

public class ModBlocks {
    private static final IBlockLookup lookup = ServiceLoader.load(IBlockLookup.class).findFirst().orElseThrow(() -> new RuntimeException("Could not initialize blocks"));

    public static Block REFLECTIVE_MIRROR_BLOCK;
    public static Block BLACK_MIRROR_BLOCK;
    public static Block BLACK_MIRROR_SLAB;
    public static Block BLACK_MIRROR_COLUMN;
    public static Block BLACK_MIRROR_STRIP;
    public static Block BLACK_MIRROR_STAIR;

    public static Block CELL_LIGHT_PANEL;
    public static Block CORNER_LIGHT_PANEL;
    public static Block EDGE_LIGHT_PANEL;
    public static Block COLUMN_LIGHT_PANEL;
    public static Block END_LIGHT_PANEL;

    public static void init() {
        lookup.init();
    }
}
