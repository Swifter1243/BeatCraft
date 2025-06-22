package com.beatcraft.blocks;

import com.beatcraft.blocks.entity.ReflectiveMirrorBlockEntity;
import com.beatcraft.blocks.entity.ReflectiveMirrorStripBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class ReflectiveMirrorStripBlock extends BlockWithEntity implements BlockEntityProvider {
    private static final MapCodec<ReflectiveMirrorStripBlock> CODEC = createCodec(ReflectiveMirrorStripBlock::new);

    public static final DirectionProperty ROTATION = DirectionProperty.of("rotation", Direction.NORTH, Direction.EAST);

    public static final BooleanProperty FULL_PLATE = BooleanProperty.of("full_plate");

    public ReflectiveMirrorStripBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ROTATION, Direction.NORTH).with(FULL_PLATE, false));
    }

    public ReflectiveMirrorStripBlock() {
        this(Settings.create().hardness(3f).resistance(5f).sounds(BlockSoundGroup.GLASS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ROTATION, FULL_PLATE);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState parent = super.getPlacementState(ctx);
        if (parent == null) return null;

        Direction rot = LightTileBlock.getPlaceOrientation(Direction.DOWN, ctx.getHitPos());

        if (rot == Direction.SOUTH) {
            rot = Direction.NORTH;
        } else if (rot == Direction.WEST) {
            rot = Direction.EAST;
        }

        return parent.with(ROTATION, rot);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getCollisionShape(state, world, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FULL_PLATE)) {
            return VoxelShapes.cuboid(
                0, 0.875, 0,
                1, 1, 1
            );
        }
        if (state.get(ReflectiveMirrorStripBlock.ROTATION) == Direction.EAST) {
            return VoxelShapes.cuboid(
                0, 0.875, 2d/16d,
                1, 1, 14d/16d
            );
        } else {
            return VoxelShapes.cuboid(
                2d/16d, 0.875, 0,
                14d/16d, 1, 1
            );
        }
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ReflectiveMirrorStripBlockEntity(pos, state);
    }
}
