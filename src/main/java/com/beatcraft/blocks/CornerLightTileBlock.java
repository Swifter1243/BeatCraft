package com.beatcraft.blocks;

import com.beatcraft.blocks.entity.CornerLightTileBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class CornerLightTileBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec<CornerLightTileBlock> CODEC = createCodec(CornerLightTileBlock::new);

    public static final DirectionProperty FACE = DirectionProperty.of("face");
    public static final DirectionProperty ROTATION = DirectionProperty.of("rotation");

    public CornerLightTileBlock() {
        this(Settings.create().noCollision().hardness(3f).resistance(5f).sounds(BlockSoundGroup.GLASS));
    }

    public CornerLightTileBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACE, Direction.DOWN).with(ROTATION, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACE);
        builder.add(ROTATION);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LightTileBlock.getVoxel(state, FACE, 1.0f);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState parent = super.getPlacementState(ctx);
        if (parent == null) return null;
        Direction face = ctx.getSide().getOpposite();

        Direction rot = LightTileBlock.getCornerOrientation(face, ctx.getHitPos());

        return parent.with(FACE, face).with(ROTATION, rot);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CornerLightTileBlockEntity(pos, state);
    }
}
