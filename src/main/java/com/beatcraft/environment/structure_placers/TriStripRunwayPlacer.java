package com.beatcraft.environment.structure_placers;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.blocks.ReflectiveMirrorStripBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TriStripRunwayPlacer implements EnvironmentPlacer {
    @Override
    public void placeStructure(ServerWorld world) {
        generateRunway(world, true);
    }

    @Override
    public void removeStructure(ServerWorld world) {
        generateRunway(world, false);
    }

    private void generateRunway(ServerWorld world, boolean place) {
        BlockState state0 = place ? ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK.getDefaultState().with(ReflectiveMirrorStripBlock.ROTATION, Direction.NORTH) : Blocks.AIR.getDefaultState();
        BlockState state1 = place ? ModBlocks.REFLECTIVE_MIRROR_STRIP_BLOCK.getDefaultState().with(ReflectiveMirrorStripBlock.ROTATION, Direction.NORTH).with(ReflectiveMirrorStripBlock.FULL_PLATE, true) : Blocks.AIR.getDefaultState();

        fillArea(world, new BlockPos(-2, -1, 7), new BlockPos(1, 0, 300), Blocks.AIR.getDefaultState());

        fillArea(world, new BlockPos(-2, -1, 8), new BlockPos(-2, -1, 300), state0);
        fillArea(world, new BlockPos(-1, -1, 8), new BlockPos(0, -1, 300), state1);
        fillArea(world, new BlockPos(1, -1, 8), new BlockPos(1, -1, 300), state0);

    }

}
