package com.beatcraft.blocks;

import com.beatcraft.blocks.entity.BlackMirrorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

// Material properties: blocks light glow. Visually Reflective
public class BlackMirrorBlock  extends BlockWithEntity implements BlockEntityProvider {
    private static final MapCodec<BlackMirrorBlock> CODEC = createCodec(BlackMirrorBlock::new);

    public BlackMirrorBlock(Settings settings) {
        super(settings);
    }

    public BlackMirrorBlock() {
        this(Settings.create().hardness(3f).resistance(5f).sounds(BlockSoundGroup.GLASS));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlackMirrorBlockEntity(pos, state);
    }
}
