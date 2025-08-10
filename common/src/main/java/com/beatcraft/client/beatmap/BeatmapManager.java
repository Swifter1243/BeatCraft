package com.beatcraft.client.beatmap;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class BeatmapManager {

    public static final ArrayList<BeatmapPlayer> beatmaps = new ArrayList<>();

    private static final float[] DEFAULT_FOG_HEIGHTS = new float[]{-50, -30};
    public static float[] getAverageFogHeight() {

        // TODO: interpolate fog heights of all beatmaps based on distance? (or just use nearest)

        return DEFAULT_FOG_HEIGHTS;
    }

    public static BeatmapPlayer nearestBeatmapToPlayer() {
        // TODO
        var playerCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        var nearestDist = Float.POSITIVE_INFINITY;
        BeatmapPlayer nearest = null;

        for (var map : beatmaps) {
            var dist = map.getRenderOrigin().distance(playerCamera);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = map;
            }
        }

        return nearest;

    }

    public static boolean hasNearbyBeatmapToPlayer() {
        var playerCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var renderDist = Minecraft.getInstance().gameRenderer.getRenderDistance();
        for (var map : beatmaps) {
            var pos = map.getRenderOrigin();

            if (playerCamera.distance(pos) <= renderDist + 64) {
                return true;
            }

        }
        return false;
    }

    public static String getMapsInfo() {

        if (beatmaps.isEmpty()) {
            return "No maps to display";
        } else {
            var info = new StringBuilder();

            info.append(String.format("Info for %s map%s:\n", beatmaps.size(), beatmaps.size() == 1 ? "" : "s"));

            for (var map : beatmaps) {
                info.append(map.getDisplayInfo()).append("\n");
            }

            return info.toString();
        }

    }

}
