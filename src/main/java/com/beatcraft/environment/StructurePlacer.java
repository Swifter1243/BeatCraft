package com.beatcraft.environment;

import com.beatcraft.BeatCraft;
import com.beatcraft.environment.structure_placers.EnvironmentPlacer;
import com.beatcraft.environment.structure_placers.TheFirstStructure;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;

public class StructurePlacer {
    public static String currentStructure = "";

    private static final EnvironmentPlacer DEFAULT = new TheFirstStructure();

    private static final HashMap<String, EnvironmentPlacer> structurePlacers = new HashMap<>();

    public static void init() {
        structurePlacers.put("Default", DEFAULT);
    }

    public static void placeStructure(String struct, ServerWorld world) {

        // Only place environments in flat worlds
        if (!BeatCraft.isFlatWorld) return;

        if (!currentStructure.equals(struct)) {
            var last = structurePlacers.getOrDefault(currentStructure, DEFAULT);
            if (last != null) {
                last.removeStructure(world);
            }

            currentStructure = struct;

            var current = structurePlacers.get(struct);
            if (current != null) {
                current.placeStructure(world);
            }
        }

    }

}
