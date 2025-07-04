package com.beatcraft.blocks;

import com.beatcraft.blocks.entity.ColorNoteDisplayBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ColorNoteDisplayBlock extends BlockWithEntity implements BlockEntityProvider {
    private static final MapCodec<ColorNoteDisplayBlock> CODEC = createCodec(ColorNoteDisplayBlock::new);

    protected ColorNoteDisplayBlock(Settings settings) {
        super(settings);
    }

    public ColorNoteDisplayBlock() {
        this(Settings.create().noCollision());
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ColorNoteDisplayBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var ent = world.getBlockEntity(pos);

        if (ent instanceof ColorNoteDisplayBlockEntity noteDisplay) {
            noteDisplay.cutAngle = (noteDisplay.cutAngle + (((player.isSneaking() ? 1 : -1)) % 9) + 9) % 9;
        }
        return ActionResult.SUCCESS;
    }
}
