package com.beatcraft.environment.structure_placers;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.blocks.ReflectiveMirrorStripBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class StripRunwayPlacer implements EnvironmentPlacer {


    @Override
    public void placeStructure(ServerWorld world) {
        generateRunway(world, true);
    }

    @Override
    public void removeStructure(ServerWorld world) {
        generateRunway(world, false);
    }


    private static final List<Integer> skipPositions = List.of(-1, 0, 1, 2, -2, -3, -4, 3);

    private void generateRunway(ServerWorld world, boolean place) {
        BlockState state = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockState state2 = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.BARRIER.getDefaultState();
        BlockState state3 = place ? ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK.getDefaultState().with(ReflectiveMirrorStripBlock.ROTATION, Direction.NORTH) : Blocks.AIR.getDefaultState();
        BlockState state4 = place ? ModBlocks.REFLECTIVE_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockState state5 = place ? ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK.getDefaultState().with(ReflectiveMirrorStripBlock.ROTATION, Direction.EAST) : Blocks.AIR.getDefaultState();


        fillArea(world, new BlockPos(-2, -1, 7), new BlockPos(1, 0, 300), Blocks.AIR.getDefaultState());
        if (place) fillArea(world, new BlockPos(-2, -1, 8), new BlockPos(1, -1, 300), state3);

        fillArea(world, new BlockPos(-4, -1, 8), new BlockPos(-4, -1, 300), state3);
        fillArea(world, new BlockPos(3, -1, 8), new BlockPos(3, -1, 300), state3);


        world.setBlockState(new BlockPos(2, -64, 8), state2);
        world.setBlockState(new BlockPos(-3, -64, 8), state2);

        world.setBlockState(new BlockPos(2, -64, 9), state2);
        world.setBlockState(new BlockPos(-3, -64, 9), state2);

        world.setBlockState(new BlockPos(-3, -1, 9), state4);
        world.setBlockState(new BlockPos(2, -1, 9), state4);

        for (int y = -63; y < -1; y++) {
            world.setBlockState(new BlockPos(2, y, 8), state);
            world.setBlockState(new BlockPos(-3, y, 8), state);
            world.setBlockState(new BlockPos(2, y, 9), state);
            world.setBlockState(new BlockPos(-3, y, 9), state);
        }

        for (int x = -63; x < 64; x++) {
            world.setBlockState(new BlockPos(x, -1, 8), skipPositions.contains(x) ? state4 : state5);
        }

    }


}

