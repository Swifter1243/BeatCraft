package com.beatcraft.client.lightshow.environment.weave;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.lightshow.environment.EnvironmentV3;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.client.lightshow.environment.lightgroup.OrientableLightGroup;
import com.beatcraft.client.lightshow.event.events.LightEventV3;
import com.beatcraft.client.lightshow.event.events.RotationEventV3;
import com.beatcraft.client.lightshow.event.events.TranslationEvent;
import com.beatcraft.client.lightshow.event.handlers.GroupEventHandlerV3;
import com.beatcraft.client.lightshow.lights.CompoundTransformState;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.TransformState;
import com.beatcraft.client.render.lights.FloodLight;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeaveEnvironment extends EnvironmentV3 {
    /*
     * Environment notes:
     * 4 major groups
     * each with 4 subgroups
     *
     * 0,0 is at x:7ish y:-0.125ish, z:8
     *
     *
     */

    private static final float CENTER_Y = 1.75f;

    private static final float INNER_OFFSET_X = 3.5f;
    private static final float INNER_OFFSET_Y = 5f;
    private static final float[] INNER_SEGMENTS = new float[]{INNER_OFFSET_X-0.001f, INNER_OFFSET_X+0.001f, INNER_OFFSET_Y-0.001f, INNER_OFFSET_Y+0.001f};

    private static final float OUTER_OFFSET_X = 6f;
    private static final float OUTER_OFFSET_Y = 3.75f;
    private static final float[] OUTER_SEGMENTS = new float[]{OUTER_OFFSET_Y-0.001f, OUTER_OFFSET_Y+0.001f, INNER_OFFSET_X-0.001f, INNER_OFFSET_X+0.001f};

    private static final float SIDE_OFFSET_X = 7f;
    private static final float SIDE_OFFSET_Y = 1.875f;
    private static final float[] SIDE_SEGMENTS = new float[]{SIDE_OFFSET_Y-0.001f, SIDE_OFFSET_Y+0.001f, SIDE_OFFSET_X-0.001f, SIDE_OFFSET_X+0.001f};

    private static final float OUTER_Z = 8;
    private static final float INNER_Z = 8.875f;
    private static final float SIDE_Z = 9.75f;

    private static final float DISTANT_W = 5.5f;
    private static final float DISTANT_H = 3.75f;
    private static final float DISTANT_Z = 35f;

    private static final float INNER_LENGTH   = 16f;
    private static final float OUTER_LENGTH   = 16f;
    private static final float SIDE_LENGTH    = 16f;
    private static final float DISTANT_LENGTH = 37.5f;

    private static final float INNER_FADE_LENGTH   = 9f;
    private static final float OUTER_FADE_LENGTH   = 9f;
    private static final float SIDE_FADE_LENGTH    = 9f;
    private static final float DISTANT_FADE_LENGTH = 22.5f;

    private static final float INNER_SPREAD   = 0.15f;
    private static final float OUTER_SPREAD   = 0.15f;
    private static final float SIDE_SPREAD    = 0.15f;
    private static final float DISTANT_SPREAD = 0.15f;

    private static final Vector3f LIGHT_GAP = new Vector3f(0, 0, 3.5f);

    private HashMap<Integer, Pair<LightGroupV3, GroupEventHandlerV3>> eventGroups;

    @Override
    public HashMap<Integer, Pair<LightGroupV3, GroupEventHandlerV3>> getEventGroups() {
        return eventGroups;
    }

    public WeaveEnvironment(BeatmapPlayer map) {
        super(map);
    }

    @Override
    public String getID() {
        return "WeaveEnvironment";
    }

    @Override
    public void setup() {
        eventGroups = new HashMap<>();
        setupOuterLights();
        setupInnerLights();
        setupSideLights();
        setupDistantLights();
    }

    private List<LightObject> stackLights(LightObject light, Vector3f step, int count) {
        var result = new ArrayList<LightObject>();
        result.add(light);
        for (int i = 0; i < count; i++) {
            light = light.cloneOffset(step);
            result.add(light);
        }
        return result;
    }

    private void setupOuterLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, OUTER_LENGTH, OUTER_FADE_LENGTH, OUTER_SPREAD,
                OUTER_SEGMENTS,
                new Vector3f(OUTER_OFFSET_X, CENTER_Y-OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf()//.rotationY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.PNP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerBL = new OrientableLightGroup(mapController, lights);
        var outerBLHandler = new GroupEventHandlerV3(outerBL);
        eventGroups.put(0, new Pair<>(outerBL, outerBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, OUTER_LENGTH, OUTER_FADE_LENGTH, OUTER_SPREAD,
                OUTER_SEGMENTS,
                new Vector3f(-OUTER_OFFSET_X, CENTER_Y-OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf()//.rotationY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NPN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerBR = new OrientableLightGroup(mapController, lights);
        var outerBRHandler = new GroupEventHandlerV3(outerBR);
        eventGroups.put(1, new Pair<>(outerBR, outerBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, OUTER_LENGTH, OUTER_FADE_LENGTH, OUTER_SPREAD,
                OUTER_SEGMENTS,
                new Vector3f(OUTER_OFFSET_X, CENTER_Y+OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(180 * Mth.DEG_TO_RAD)//.rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.PPN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerTL = new OrientableLightGroup(mapController, lights);
        var outerTLHandler = new GroupEventHandlerV3(outerTL);
        eventGroups.put(2, new Pair<>(outerTL, outerTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, OUTER_LENGTH, OUTER_FADE_LENGTH, OUTER_SPREAD,
                OUTER_SEGMENTS,
                new Vector3f(-OUTER_OFFSET_X, CENTER_Y+OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(180 * Mth.DEG_TO_RAD)//.rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerTR = new OrientableLightGroup(mapController, lights);
        var outerTRHandler = new GroupEventHandlerV3(outerTR);
        eventGroups.put(3, new Pair<>(outerTR, outerTRHandler));

    }

    private void setupInnerLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, INNER_LENGTH, INNER_FADE_LENGTH, INNER_SPREAD,
                INNER_SEGMENTS,
                new Vector3f(INNER_OFFSET_X, CENTER_Y-INNER_OFFSET_Y, INNER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf()//.rotationY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.PNP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerBL = new OrientableLightGroup(mapController, lights);
        var innerBLHandler = new GroupEventHandlerV3(innerBL);
        eventGroups.put(4, new Pair<>(innerBL, innerBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, INNER_LENGTH, INNER_FADE_LENGTH, INNER_SPREAD,
                INNER_SEGMENTS,
                new Vector3f(-INNER_OFFSET_X, CENTER_Y-INNER_OFFSET_Y, INNER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf()//.rotationY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NPN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerBR = new OrientableLightGroup(mapController, lights);
        var innerBRHandler = new GroupEventHandlerV3(innerBR);
        eventGroups.put(5, new Pair<>(innerBR, innerBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, INNER_LENGTH, INNER_FADE_LENGTH, INNER_SPREAD,
                INNER_SEGMENTS,
                new Vector3f(INNER_OFFSET_X, CENTER_Y+INNER_OFFSET_Y, INNER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(180 * Mth.DEG_TO_RAD)//.rotateY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.PPN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerTL = new OrientableLightGroup(mapController, lights);
        var innerTLHandler = new GroupEventHandlerV3(innerTL);
        eventGroups.put(6, new Pair<>(innerTL, innerTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, INNER_LENGTH, INNER_FADE_LENGTH, INNER_SPREAD,
                INNER_SEGMENTS,
                new Vector3f(-INNER_OFFSET_X, CENTER_Y+INNER_OFFSET_Y, INNER_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(180 * Mth.DEG_TO_RAD)//.rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerTR = new OrientableLightGroup(mapController, lights);
        var innerTRHandler = new GroupEventHandlerV3(innerTR);
        eventGroups.put(7, new Pair<>(innerTR, innerTRHandler));
    }

    private void setupSideLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, SIDE_LENGTH, SIDE_FADE_LENGTH, SIDE_SPREAD,
                SIDE_SEGMENTS,
                new Vector3f(SIDE_OFFSET_X, CENTER_Y-SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(90 * Mth.DEG_TO_RAD)//.rotateY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NPP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideBL = new OrientableLightGroup(mapController, lights);
        var sideBLHandler = new GroupEventHandlerV3(sideBL);
        eventGroups.put(8, new Pair<>(sideBL, sideBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, SIDE_LENGTH, SIDE_FADE_LENGTH, SIDE_SPREAD,
                SIDE_SEGMENTS,
                new Vector3f(-SIDE_OFFSET_X, CENTER_Y-SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(-90 * Mth.DEG_TO_RAD)//.rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideBR = new OrientableLightGroup(mapController, lights);
        var sideBRHandler = new GroupEventHandlerV3(sideBR);
        eventGroups.put(9, new Pair<>(sideBR, sideBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, SIDE_LENGTH, SIDE_FADE_LENGTH, SIDE_SPREAD,
                SIDE_SEGMENTS,
                new Vector3f(SIDE_OFFSET_X, CENTER_Y+SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(90 * Mth.DEG_TO_RAD)//.rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNN
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideTL = new OrientableLightGroup(mapController, lights);
        var sideTLHandler = new GroupEventHandlerV3(sideTL);
        eventGroups.put(10, new Pair<>(sideTL, sideTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, SIDE_LENGTH, SIDE_FADE_LENGTH, SIDE_SPREAD,
                SIDE_SEGMENTS,
                new Vector3f(-SIDE_OFFSET_X, CENTER_Y+SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationZ(-90 * Mth.DEG_TO_RAD)//.rotateY(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NPP
            ),
            LIGHT_GAP, 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideTR = new OrientableLightGroup(mapController, lights);
        var sideTRHandler = new GroupEventHandlerV3(sideTR);
        eventGroups.put(11, new Pair<>(sideTR, sideTRHandler));
    }

    private void setupDistantLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, DISTANT_LENGTH, DISTANT_FADE_LENGTH, DISTANT_SPREAD,
                new float[0],
                new Vector3f(
                    DISTANT_W,
                    CENTER_Y+DISTANT_H+(DISTANT_H*2/9f),
                    DISTANT_Z
                ),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationX(-90 * Mth.DEG_TO_RAD)//.rotateY(180 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.XYZ,
                CompoundTransformState.Polarity.NNP
            ),
            new Vector3f(-(DISTANT_W*2)/11f, 0, 0), 11)
        ) {
            lights.put(lightID++, light);
        }
        var distantT = new OrientableLightGroup(mapController, lights);
        var distantTHandler = new GroupEventHandlerV3(distantT);
        eventGroups.put(12, new Pair<>(distantT, distantTHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, DISTANT_LENGTH, DISTANT_FADE_LENGTH, DISTANT_SPREAD,
                new float[0],
                new Vector3f(-DISTANT_W, (CENTER_Y-DISTANT_H)-(DISTANT_H*2/9f), DISTANT_Z),
                new Quaternionf()
            ).withRotation(
                new Quaternionf().rotationX(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.XYZ,
                CompoundTransformState.Polarity.PNP
            ),
            new Vector3f((DISTANT_W*2)/11f, 0, 0), 11)
        ) {
            lights.put(lightID++, light);
        }
        var distantD = new OrientableLightGroup(mapController, lights);
        var distantDHandler = new GroupEventHandlerV3(distantD);
        eventGroups.put(13, new Pair<>(distantD, distantDHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, DISTANT_LENGTH, DISTANT_FADE_LENGTH, DISTANT_SPREAD,
                new float[0],
                new Vector3f(DISTANT_W+(DISTANT_W*2/11f), CENTER_Y-DISTANT_H, DISTANT_Z),
                new Quaternionf().rotateY(90 * Mth.DEG_TO_RAD)
            ).withRotation(
                new Quaternionf().rotateX(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNP
            ),
            new Vector3f(0, (DISTANT_H*2)/9f, 0), 9)
        ) {
            lights.put(lightID++, light);
        }
        var distantL = new OrientableLightGroup(mapController, lights);
        var distantLHandler = new GroupEventHandlerV3(distantL);
        eventGroups.put(14, new Pair<>(distantL, distantLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                mapController,
                0.125f, 0.075f, DISTANT_LENGTH, DISTANT_FADE_LENGTH, DISTANT_SPREAD,
                new float[0],
                new Vector3f((-DISTANT_W)-(DISTANT_W*2/11f), CENTER_Y+DISTANT_H, DISTANT_Z),
                new Quaternionf().rotateY(-90 * Mth.DEG_TO_RAD)
            ).withRotation(
                new Quaternionf().rotateX(-90 * Mth.DEG_TO_RAD)
            ).withRotationSwizzle(
                CompoundTransformState.Swizzle.ZYX,
                CompoundTransformState.Polarity.NNN
            ),
            new Vector3f(0, -(DISTANT_H*2)/9f, 0), 9)
        ) {
            lights.put(lightID++, light);
        }
        var distantR = new OrientableLightGroup(mapController, lights);
        var distantRHandler = new GroupEventHandlerV3(distantR);
        eventGroups.put(15, new Pair<>(distantR, distantRHandler));

    }

    @Override
    public WeaveEnvironment reset() {
        super.reset();
        eventGroups.forEach((k, v) -> {
            v.getB().reset();
            v.getB().clear();
            v.getA().reset();
        });

        return this;
    }

    @Override
    public int getGroupCount() {
        return 16;
    }

    @Override
    public int getLightCount(int group) {
        if (group < 12) { return 8; }
        else if (group < 14) { return 12; }
        else { return 10; }
    }

    @Override
    protected void linkEvents(
        int group, int lightID,
        List<LightEventV3> lightEvents,
        HashMap<TransformState.Axis, ArrayList<RotationEventV3>> rotationEvents,
        HashMap<TransformState.Axis,ArrayList<TranslationEvent>> ignored,
        List<Integer> ignored0
    ) {

        if (eventGroups.containsKey(group)) {
            eventGroups.get(group).getB().linkLightEvents(lightEvents);
            eventGroups.get(group).getB().linkRotationEvents(lightID, rotationEvents);
        }

    }

    @Override
    public void seek(float beat) {
        eventGroups.forEach((k, v) -> {
            v.getB().seek(beat);
        });
    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);
        eventGroups.forEach((k, v) -> {
            v.getB().update(beat);
        });

    }

    @Override
    public void render(PoseStack matrices, Camera camera) {
        super.render(matrices, camera);

        eventGroups.forEach((k, v) -> {
            v.getA().render(matrices, camera);
        });

    }
}
