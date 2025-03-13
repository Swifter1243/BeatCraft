package com.beatcraft.environment.structure_placers;

import com.beatcraft.BeatCraft;
import com.beatcraft.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector2i;

public class TheFirstStructure implements EnvironmentPlacer {

    private static final Identifier leftTowerTop =  BeatCraft.id("the_first/ltowertop");
    private static final Identifier rightTowerTop = BeatCraft.id("the_first/rtowertop");

    // placements:
    // left tower tops:
    // 20 2 3
    // 10 -4 -30
    private static final BlockPos leftPos1 = new BlockPos(20, 2, 3);
    private static final BlockPos leftPos2 = new BlockPos(10, -4, -30);
    // right:
    // -25 2 3
    // -15 -4 -30
    private static final BlockPos rightPos1 = new BlockPos(-25, 2, 3);
    private static final BlockPos rightPos2 = new BlockPos(-15, -4, -30);
    // pillars down:
    // 20 1 15 & 20 1 18
    // 10 -5 -18 & 10 -5 -15
    private static final Vec3i leftPillarOffset = new Vec3i(0, -1, 12);
    // -13 -5 -18 & -13 -5 -15
    // -23 1 15 & -23 1 18
    private static final Vec3i rightPillarOffset = new Vec3i(2, -1, 12);
    // rims:
    // XY: 16 -1 // -x: -17
    // spectrum viewer pillars:
    // XY: 13 -6; pillar on 1, no pillar on 0, ... // -x: -14
    // back pillars:
    // 9 108 -39 // -x: -10
    // back slant pillars:
    // 3 -64    9, z: -3, z:-15 // -x: -4
    // 3  108 -52, z:-64, z:-76
    private static final Vector2i[] lineStarts = new Vector2i[]{
        new Vector2i( 3, 9), new Vector2i( 3, -3), new Vector2i( 3, -15),
        new Vector2i(-4, 9), new Vector2i(-4, -3), new Vector2i(-4, -15)
    };
    private static final int lineOffset = -61;
    // runway legs:
    // -2 0 -8
    // 1 -1 8

    @Override
    public void placeStructure(ServerWorld world) {

        generateTowers(world, true);
        generateWavePillars(world, true);
        generateBackPillars(world, true);
        generateRunway(world, true);

        BeatCraft.LOGGER.info("Placed TheFirst environment!");
    }

    @Override
    public void removeStructure(ServerWorld world) {
        generateTowers(world, false);
        generateWavePillars(world, false);
        generateBackPillars(world, false);
        generateRunway(world, false);
    }


    private void generateWavePillars(ServerWorld world, boolean place) {
        for (int z = -207; z < 300; z += 2) {
            placeWavePillar(world, 13, z, place);
            placeWavePillar(world, -14, z, place);
        }
    }

    private void placeWavePillar(ServerWorld world, int x, int z, boolean place) {
        world.setBlockState(new BlockPos(x, -64, z), place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.BARRIER.getDefaultState());
        for (int y = -63; y < -5; y++) {
            world.setBlockState(new BlockPos(x, y, z), place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState());
        }
    }

    private void generateTowers(ServerWorld world, boolean place) {

        if (place) {
            placeStructure(world, leftTowerTop, leftPos1);
            placeStructure(world, leftTowerTop, leftPos2);
            placeStructure(world, rightTowerTop, rightPos1);
            placeStructure(world, rightTowerTop, rightPos2);
        } else {
            removeStructure(world, leftTowerTop, leftPos1);
            removeStructure(world, leftTowerTop, leftPos2);
            removeStructure(world, rightTowerTop, rightPos1);
            removeStructure(world, rightTowerTop, rightPos2);
        }

        placeTowerSupport(world, leftPos1.add(leftPillarOffset), place);
        placeTowerSupport(world, leftPos2.add(leftPillarOffset), place);

        placeTowerSupport(world, rightPos1.add(rightPillarOffset), place);
        placeTowerSupport(world, rightPos2.add(rightPillarOffset), place);

    }

    private void placeTowerSupport(ServerWorld world, BlockPos pos, boolean place) {
        BlockState state = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockState state2 = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.BARRIER.getDefaultState();

        fillArea(world, new BlockPos(pos.getX(), -63, pos.getZ()), new BlockPos(pos.getX()+2, pos.getY(), pos.getZ()+1), state);
        fillArea(world, new BlockPos(pos.getX(), -64, pos.getZ()), new BlockPos(pos.getX()+2, -64, pos.getZ()+1), state2);

        fillArea(world, new BlockPos(pos.getX(), -63, pos.getZ()+3), new BlockPos(pos.getX()+2, pos.getY(), pos.getZ()+4), state);
        fillArea(world, new BlockPos(pos.getX(), -64, pos.getZ()+3), new BlockPos(pos.getX()+2, -64, pos.getZ()+4), state2);

    }

    private void generateBackPillars(ServerWorld world, boolean place) {
        BlockState state = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockState state2 = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.BARRIER.getDefaultState();

        for (Vector2i start : lineStarts) {
            fillLine(world, new BlockPos(start.x, -64, start.y), new BlockPos(start.x, 108, start.y+lineOffset), state);
            if (!place) {
                world.setBlockState(new BlockPos(start.x, -64, start.y), Blocks.BARRIER.getDefaultState());
            }
        }

        world.setBlockState(new BlockPos(9, -64, -39), state2);
        world.setBlockState(new BlockPos(-10, -64, -39), state2);

        for (int y = -63; y < 108; y++) {
            world.setBlockState(new BlockPos(9, y, -39), state);
            world.setBlockState(new BlockPos(-10, y, -39), state);
        }
    }

    private void generateRunway(ServerWorld world, boolean place) {
        BlockState state = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState();
        BlockState state2 = place ? ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState() : Blocks.BARRIER.getDefaultState();

        fillArea(world, new BlockPos(-2, -1, 7), new BlockPos(1, 0, 300), Blocks.AIR.getDefaultState());
        if (place) fillArea(world, new BlockPos(-2, -1, 8), new BlockPos(1, -1, 300), state);

        world.setBlockState(new BlockPos(1, -64, 8), state2);
        world.setBlockState(new BlockPos(-2, -64, 8), state2);

        for (int y = -63; y < 0; y++) {
            world.setBlockState(new BlockPos(1, y, 8), state);
            world.setBlockState(new BlockPos(-2, y, 8), state);
        }

        for (int z = -207; z <= 300; z++) {
            world.setBlockState(new BlockPos(16, 0, z), state);
            world.setBlockState(new BlockPos(-17, 0, z), state);
        }

    }

}
