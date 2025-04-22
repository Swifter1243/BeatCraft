package com.beatcraft.environment.structure_placers;

import net.minecraft.server.world.ServerWorld;

public class EmptyStructure implements EnvironmentPlacer {
    @Override
    public void placeStructure(ServerWorld world) {
        // Do nothing
    }

    @Override
    public void removeStructure(ServerWorld world) {
        // Do nothing
    }
}
