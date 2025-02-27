package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ColumnLightTileBlockEntity extends BlockEntity {
    public ColumnLightTileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COLUMN_LIGHT_BLOCK_ENTITY_TYPE, pos, state);
    }
}
