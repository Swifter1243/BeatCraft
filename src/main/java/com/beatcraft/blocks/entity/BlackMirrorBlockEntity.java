package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class BlackMirrorBlockEntity extends BlockEntity {
    public BlackMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BLACK_MIRROR_BLOCK_ENTITY, pos, state);
    }
}
