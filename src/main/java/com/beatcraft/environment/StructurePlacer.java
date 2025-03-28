package com.beatcraft.environment;

import com.beatcraft.BeatCraft;
import com.beatcraft.environment.structure_placers.EnvironmentPlacer;
import com.beatcraft.environment.structure_placers.TheFirstStructure;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructurePlacer {
    public static String currentStructure = "";

    private static final EnvironmentPlacer DEFAULT = new TheFirstStructure();

    private static final HashMap<String, EnvironmentPlacer> structurePlacers = new HashMap<>();

    public static List<String> matchEnvironments(String env) {
        ArrayList<String> matching = new ArrayList<>();
        for (var key : structurePlacers.keySet()) {
            if (key.contains(env)) {
                matching.add(key);
            }
        }
        return matching;
    }

    public static void init() {
        structurePlacers.put("Default", DEFAULT);
    }

    public static void placeStructure(String struct, ServerWorld world) {
        // Only place environments in flat worlds
        if (!BeatCraft.isFlatWorld) return;
        placeStructureForced(struct, world);
    }

    public static boolean placeStructureForced(String struct, ServerWorld world) {

        if (!currentStructure.equals(struct)) {
            var current = structurePlacers.get(struct);
            if (current != null) {
                var last = structurePlacers.get(currentStructure);
                if (last != null) {
                    last.removeStructure(world);
                }

                currentStructure = struct;

                current.placeStructure(world);
            }
            return current != null;
        }
        return false;
    }

    public static boolean removeStructure(ServerWorld world) {

        if (currentStructure.isEmpty()) {
            return false;
        }

        var last = structurePlacers.getOrDefault(currentStructure, DEFAULT);
        if (last != null) {
            last.removeStructure(world);
        }

        currentStructure = "";

        return true;
    }


}
