package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class EdgeLightTileBlockEntity extends BlockEntity {
    public EdgeLightTileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EDGE_LIGHT_BLOCK_ENTITY_TYPE, pos, state);
    }
}
