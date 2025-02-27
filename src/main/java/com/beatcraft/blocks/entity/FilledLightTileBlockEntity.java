package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class FilledLightTileBlockEntity extends BlockEntity {
    public FilledLightTileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FILLED_LIGHT_BLOCK_ENTITY_TYPE, pos, state);
    }
}
