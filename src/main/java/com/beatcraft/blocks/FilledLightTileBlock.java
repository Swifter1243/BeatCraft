package com.beatcraft.blocks;

import com.beatcraft.blocks.entity.FilledLightTileBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilledLightTileBlock extends BlockWithEntity implements BlockEntityProvider {
    private static final MapCodec<FilledLightTileBlock> CODEC = createCodec(FilledLightTileBlock::new);

    public static final DirectionProperty FACE = DirectionProperty.of("face");
    public static final IntProperty PATTERN = IntProperty.of("pattern", 0, 2);

    public FilledLightTileBlock() {
        this(Settings.create().noCollision().hardness(3f).resistance(5f).sounds(BlockSoundGroup.GLASS));
    }

    public FilledLightTileBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACE, Direction.DOWN).with(PATTERN, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACE);
        builder.add(PATTERN);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LightTileBlock.getVoxel(state, FACE, 1.0f);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        int s = state.get(PATTERN);
        s++;
        if (s >= 3) {
            s = 0;
        }
        world.setBlockState(pos, state.with(PATTERN, s));
        return ActionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState parent = super.getPlacementState(ctx);
        if (parent == null) return null;
        Direction face = ctx.getSide().getOpposite();

        return parent.with(FACE, face);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FilledLightTileBlockEntity(pos, state);
    }
}
