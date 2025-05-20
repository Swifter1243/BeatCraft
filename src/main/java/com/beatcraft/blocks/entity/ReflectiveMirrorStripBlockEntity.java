package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ReflectiveMirrorStripBlockEntity extends BlockEntity {
    public ReflectiveMirrorStripBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK_ENTITY, pos, state);
    }
}
