package com.beatcraft.blocks.entity;

import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class ReflectiveMirrorBlockEntity extends BlockEntity {
    public ReflectiveMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REFLECTIVE_MIRROR_BLOCK_ENTITY, pos, state);
    }
}
