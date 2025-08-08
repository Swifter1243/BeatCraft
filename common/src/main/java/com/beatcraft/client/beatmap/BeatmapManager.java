package com.beatcraft.client.beatmap;

import com.beatcraft.client.beatmap.data.Difficulty;

import java.util.ArrayList;

public class BeatmapManager {

    public static final ArrayList<BeatmapPlayer> beatmaps = new ArrayList<>();

    private static final float[] DEFAULT_FOG_HEIGHTS = new float[]{-50, -30};
    public static float[] getAverageFogHeight() {

        // TODO: interpolate fog heights of all beatmaps based on distance? (or just use nearest)

        return DEFAULT_FOG_HEIGHTS;
    }

    public static BeatmapPlayer nearestBeatmap() {
        // TODO
        throw new RuntimeException("Not yet implemented");
    }

    public static boolean hasNearbyBeatmap() {
        return false;
    }


}
