package com.beatcraft.render.lightshow_event_visualizer;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.EnvironmentV3;
import com.beatcraft.lightshow.environment.EnvironmentV4;
import com.beatcraft.lightshow.event.EventBuilder;
import com.beatcraft.render.DebugRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class EventVisualizer {

    public static ArrayList<Integer> hiddenGroups = new ArrayList<>();

    public static HashMap<Integer, ArrayList<Integer>> hiddenIDs = new HashMap<>();

    public static Integer previewGroup = null;

    private static ArrayList<EventBuilder.GroupKey> targets = new ArrayList<>();

    private static final float LANE_WIDTH = 1f;
    private static final float LANE_GAP = 0.2f;

    private static final float SUB_LANE_WIDTH = 0.1f;
    private static final float SUB_LANE_GAP = 0.0333f;

    private static float beatSpacing = 1;
    private static float fDist = 1;
    private static float bDist = 1;

    private static final HashMap<Integer, Integer> lightCounts = new HashMap<>();


    public static void init() {

        var beatmap = BeatmapPlayer.currentBeatmap;
        if (beatmap == null) return;
        var environment = beatmap.lightShowEnvironment;
        if (environment == null) return;

        lightCounts.clear();

        beatSpacing = BeatCraftClient.playerConfig.getDebugLightshowBeatSpacing();
        fDist = BeatCraftClient.playerConfig.getDebugLightshowLookAhead();
        bDist = BeatCraftClient.playerConfig.getDebugLightshowLookBehind();

        if (environment instanceof EnvironmentV4 env4) {
            int groupCount = env4.getGroupCount();
            for (int group = 0; group < groupCount; group++) {
                lightCounts.put(group, env4.getLightCount(group));
            }
        } else if (environment instanceof EnvironmentV3 env3) {
            int groupCount = env3.getGroupCount();
            for (int group = 0; group < groupCount; group++) {
                lightCounts.put(group, env3.getLightCount(group));
            }

            targets.clear();

            for (int group = 0; group < groupCount; group++) {
                if (hiddenGroups.contains(group)) continue;
                int lightCount = env3.getLightCount(group);

                for (int lightID = 0; lightID < lightCount; lightID++) {
                    if (hiddenIDs.computeIfAbsent(group, k -> new ArrayList<>()).contains(lightID)) {
                        continue;
                    }
                    targets.add(new EventBuilder.GroupKey(group, lightID));
                }

            }



        } else if (environment instanceof EnvironmentV2 env2) {
            // ??
        }

    }


    public static void render() {
        if (DebugRenderer.doDebugRendering && BeatCraftClient.playerConfig.doLightshowEventRendering()) {

            var beatmap = BeatmapPlayer.currentBeatmap;
            if (beatmap == null) return;
            var environment = beatmap.lightShowEnvironment;
            if (environment == null) return;

            var version = environment.getVersion();

            if (version == 4) {
                var env4 = (EnvironmentV4) environment;


            } else if (version == 3) {
                var env3 = (EnvironmentV3) environment;

                if (previewGroup == null) {
                    int groupCount = env3.getGroupCount();

                    int visibleGroups = groupCount;
                    for (var groupID : hiddenGroups) {
                        if (groupID < groupCount) {
                            visibleGroups--;
                        }
                    }


                } else {

                }


            } else if (version == 2) {
                var env2 = (EnvironmentV2) environment;


            }


        }
    }


}
