package com.beatcraft.environment.structure_placers;

import net.minecraft.server.world.ServerWorld;

public interface EnvironmentPlacer {
    void placeStructure(ServerWorld world);
    void removeStructure(ServerWorld world);
}
