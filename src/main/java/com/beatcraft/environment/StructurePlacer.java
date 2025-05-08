package com.beatcraft.environment;

import com.beatcraft.BeatCraft;
import com.beatcraft.environment.structure_placers.*;
import com.beatcraft.world.PlacedEnvironmentState;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructurePlacer {
    public static String currentStructure = "";
    private static PlacedEnvironmentState worldPlacementState;

    public static void setState(PlacedEnvironmentState state) {
        worldPlacementState = state;
        currentStructure = state.getPlacedEnvironment();
    }

    private static final EnvironmentPlacer DEFAULT = new TheFirstStructure();
    private static final EnvironmentPlacer EMPTY = new EmptyStructure();
    private static final EnvironmentPlacer STRIPS = new StripRunwayPlacer();
    private static final EnvironmentPlacer TRIANGLE = new TriangleStructure();

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
        structurePlacers.put("WeaveEnvironment", EMPTY);
        structurePlacers.put("OriginsEnvironment", STRIPS);
        structurePlacers.put("TriangleEnvironment", TRIANGLE);
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
                worldPlacementState.setPlacedEnvironment(currentStructure);

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
        worldPlacementState.setPlacedEnvironment("");

        return true;
    }


}
