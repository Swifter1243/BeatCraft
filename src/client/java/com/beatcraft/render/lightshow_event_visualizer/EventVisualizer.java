package com.beatcraft.render.lightshow_event_visualizer;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.EnvironmentV3;
import com.beatcraft.lightshow.environment.EnvironmentV4;
import com.beatcraft.lightshow.event.EventBuilder;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.render.DebugRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class EventVisualizer {

    public static ArrayList<Integer> hiddenGroups = new ArrayList<>();

    public static HashMap<Integer, ArrayList<Integer>> hiddenIDs = new HashMap<>();

    public static Integer previewGroup = null;

    private static final ArrayList<EventBuilder.GroupKey> targets = new ArrayList<>();

    private static final float LANE_WIDTH = 1f;
    private static final float LANE_GAP = 0.2f;

    private static final float SUB_LANE_WIDTH = 0.1f;
    private static final float SUB_LANE_GAP = 0.0333f;

    private static final float EVENT_Y = 0.02f;
    private static final float Y_SPACING = 1f;

    private static float beatSpacing = 1;
    private static float fDist = 1;
    private static float bDist = 1;

    private static float currentWidth = 0;

    public static void refresh() {

        var beatmap = BeatmapPlayer.currentBeatmap;
        if (beatmap == null) return;
        var environment = beatmap.lightShowEnvironment;
        if (environment == null) return;

        beatSpacing = BeatCraftClient.playerConfig.getDebugLightshowBeatSpacing();
        fDist = BeatCraftClient.playerConfig.getDebugLightshowLookAhead();
        bDist = BeatCraftClient.playerConfig.getDebugLightshowLookBehind();

        if (environment instanceof EnvironmentV4 env4) {
        } else if (environment instanceof EnvironmentV3 env3) {
            int groupCount = env3.getGroupCount();

            targets.clear();
            currentWidth = 0;
            var lanes = 0;

            for (int group = 0; group < groupCount; group++) {
                if (hiddenGroups.contains(group)) continue;
                int lightCount = env3.getLightCount(group);

                for (int lightID = 0; lightID < lightCount; lightID++) {
                    if (hiddenIDs.computeIfAbsent(group, k -> new ArrayList<>()).contains(lightID)) {
                        continue;
                    }
                    targets.add(new EventBuilder.GroupKey(group, lightID));
                    lanes++;
                }

                currentWidth++;

            }
            currentWidth--;
            lanes--;

            currentWidth *= (LANE_GAP - SUB_LANE_GAP);
            currentWidth += lanes * (SUB_LANE_WIDTH + SUB_LANE_GAP);



        } else if (environment instanceof EnvironmentV2 env2) {
            // ??
        }

    }


    private static float beat = 0;

    public static void update(float beat) {
        EventVisualizer.beat = beat;
    }

    public static void render(Camera camera) {
        if (DebugRenderer.doDebugRendering && BeatCraftClient.playerConfig.doLightshowEventRendering()) {

            var beatmap = BeatmapPlayer.currentBeatmap;
            if (beatmap == null) return;

            var environment = beatmap.lightShowEnvironment;
            if (environment == null) return;

            var lowerBound = beat - bDist;
            var upperBound = beat + fDist;

            var version = environment.getVersion();

            var tessellator = Tessellator.getInstance();

            if (version == 4) {
                var env4 = (EnvironmentV4) environment;

            } else if (version == 3) {
                var env3 = (EnvironmentV3) environment;

                if (previewGroup == null) {
                    var x = (currentWidth/2f);
                    var lg = 0;

                    var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

                    for (var target : targets) {
                        var cg = target.getGroup();
                        var cid = target.getLightId();

                        var lightEvents = env3.getLightEvents(cg, cid, lowerBound, upperBound);
                        var rotationXEvents = env3.getRotationEvents(cg, cid, TransformState.Axis.RX, lowerBound, upperBound);
                        var rotationYEvents = env3.getRotationEvents(cg, cid, TransformState.Axis.RY, lowerBound, upperBound);
                        var rotationZEvents = env3.getRotationEvents(cg, cid, TransformState.Axis.RZ, lowerBound, upperBound);
                        var c = camera.pos.toVector3f();

                        if (lg != cg) {
                            lg = cg;
                            x -= (SUB_LANE_WIDTH + LANE_GAP);
                        } else {
                            x -= (SUB_LANE_WIDTH + SUB_LANE_GAP);
                        }

                        buffer.vertex(new Vector3f((-currentWidth/2f) - ((SUB_LANE_WIDTH + SUB_LANE_GAP) * 2), EVENT_Y+0.01f, -0.1f).sub(c)).color(0xFFFFFFFF);
                        buffer.vertex(new Vector3f((currentWidth/2f) - SUB_LANE_WIDTH, EVENT_Y+0.01f, -0.1f).sub(c)).color(0xFFFFFFFF);
                        buffer.vertex(new Vector3f((currentWidth/2f) - SUB_LANE_WIDTH, EVENT_Y+0.01f, 0f).sub(c)).color(0xFFFFFFFF);
                        buffer.vertex(new Vector3f((-currentWidth/2f) - ((SUB_LANE_WIDTH + SUB_LANE_GAP) * 2), EVENT_Y+0.01f, 0f).sub(c)).color(0xFFFFFFFF);

                        for (var event : lightEvents) {
                            var sz = (Math.max(event.getEventBeat(), lowerBound) - beat) * beatSpacing;
                            var ez = (Math.min(event.getEventBeat() + event.getEventDuration(), upperBound) - beat) * beatSpacing;

                            var startColor = event.startState.getBloomColor();
                            var isStep = event.easing.apply(0.9f) == 0;
                            var endColor = isStep ? startColor : event.lightState.getBloomColor();

                            float startX = x;
                            float startX2 = x-SUB_LANE_WIDTH;
                            float endX = x;
                            float endX2 = x-SUB_LANE_WIDTH;
                            if (event.lightState.strobeFrequency != 0) {
                                endX = x-(SUB_LANE_WIDTH/2f);
                            }

                            if (event.startState.strobeFrequency != 0) {
                                startX = x-(SUB_LANE_WIDTH/2f);
                            }

                            buffer.vertex(new Vector3f(startX, EVENT_Y, sz).sub(c)).color(startColor);
                            buffer.vertex(new Vector3f(endX, EVENT_Y, ez).sub(c)).color(endColor);
                            buffer.vertex(new Vector3f(endX2, EVENT_Y, ez).sub(c)).color(endColor);
                            buffer.vertex(new Vector3f(startX2, EVENT_Y, sz).sub(c)).color(startColor);

                        }

                    }
                    var buff = buffer.endNullable();
                    if (buff != null) {
                        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                        RenderSystem.disableCull();
                        BufferRenderer.drawWithGlobalProgram(buff);
                        RenderSystem.enableCull();

                    }


                } else {

                }


            } else if (version == 2) {
                var env2 = (EnvironmentV2) environment;


            }


        }
    }


}
