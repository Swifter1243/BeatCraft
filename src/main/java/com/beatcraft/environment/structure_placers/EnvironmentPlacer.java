package com.beatcraft.environment.structure_placers;

import com.beatcraft.BeatCraft;
import com.beatcraft.utils.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashSet;

public interface EnvironmentPlacer {
    void placeStructure(ServerWorld world);
    void removeStructure(ServerWorld world);

    default void placeStructure(ServerWorld world, Identifier id, BlockPos pos) {
        var structureOpt = world.getStructureTemplateManager().getTemplate(id);

        if (structureOpt.isPresent()) {
            var struct = structureOpt.get();

            struct.place(world, pos, new BlockPos(0, 0, 0), new StructurePlacementData(), world.random, 2);

        } else {
            BeatCraft.LOGGER.error("Failed to find structure: {}", id);
        }
    }

    default void removeStructure(ServerWorld world, Identifier id, BlockPos pos) {
        var structureOpt = world.getStructureTemplateManager().getTemplate(id);

        if (structureOpt.isPresent()) {
            var struct = structureOpt.get();

            Vec3i size = struct.getSize();

            fillArea(world, pos, pos.add(size).subtract(new Vec3i(1, 1, 1)), Blocks.AIR.getDefaultState());
        }
    }

    default void fillArea(ServerWorld world, BlockPos min, BlockPos max, BlockState blockState) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    world.setBlockState(new BlockPos(x, y, z), blockState);
                }
            }
        }
    }

    default void fillLine(ServerWorld world, BlockPos start, BlockPos end, BlockState state) {

        HashSet<BlockPos> positions = new HashSet<>();

        int dx = Math.abs(end.getX() - start.getX());
        int dy = Math.abs(end.getY() - start.getY());
        int dz = Math.abs(end.getZ() - start.getZ());

        if (dx + dy + dz == 0) {
            world.setBlockState(start, state);
            return;
        }

        int maxStep = Math.max(Math.max(dx, dy), dz);

        if (maxStep == dx) {
            for (int step = 0; step <= dx; step++) {
                int x = start.getX() + step * Integer.signum(end.getX() - start.getX());
                int y = Math.round(start.getY() + step * ((float) dy / dx) * Integer.signum(end.getY() - start.getY()));
                int z = Math.round(start.getZ() + step * ((float) dz / dx) * Integer.signum(end.getZ() - start.getZ()));
                positions.add(new BlockPos(x, y, z));
            }
        } else if (maxStep == dy) {
            for (int step = 0; step <= dy; step++) {
                int y = start.getY() + step * Integer.signum(end.getY() - start.getY());
                int x = Math.round(start.getX() + step * ((float) dx / dy) * Integer.signum(end.getX() - start.getX()));
                int z = Math.round(start.getZ() + step * ((float) dz / dy) * Integer.signum(end.getZ() - start.getZ()));
                positions.add(new BlockPos(x, y, z));
            }
        } else {
            for (int step = 0; step <= dz; step++) {
                int z = start.getZ() + step * Integer.signum(end.getZ() - start.getZ());
                int x = Math.round(start.getX() + step * ((float) dx / dz) * Integer.signum(end.getX() - start.getX()));
                int y = Math.round(start.getY() + step * ((float) dy / dz) * Integer.signum(end.getY() - start.getY()));
                positions.add(new BlockPos(x, y, z));
            }
        }

        for (BlockPos pos : positions) {
            world.setBlockState(pos, state);
        }
    }

}
