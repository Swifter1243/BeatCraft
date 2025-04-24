package com.beatcraft.lightshow.environment.weave;

import com.beatcraft.lightshow.environment.EnvironmentV3;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.environment.lightgroup.OrientableLightGroup;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.event.handlers.GroupEventHandlerV3;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.render.lights.FloodLight;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private static final float INNER_OFFSET_X = 4f;
    private static final float INNER_OFFSET_Y = 4f;

    private static final float OUTER_OFFSET_X = 5.5f;
    private static final float OUTER_OFFSET_Y = 3f;

    private static final float SIDE_OFFSET_X = 7f;
    private static final float SIDE_OFFSET_Y = 1.8f;

    private static final float OUTER_Z = 8;
    private static final float INNER_Z = 8.3333f;
    private static final float SIDE_Z = 8.6667f;

    private static final float DISTANT_W = 6f;
    private static final float DISTANT_H = 4.5f;
    private static final float DISTANT_Z = 30f;


    private HashMap<Integer, Pair<LightGroupV3, GroupEventHandlerV3>> eventGroups;

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
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(OUTER_OFFSET_X, CENTER_Y-OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf().rotationY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerBL = new OrientableLightGroup(lights);
        var outerBLHandler = new GroupEventHandlerV3(outerBL);
        eventGroups.put(0, new Pair<>(outerBL, outerBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-OUTER_OFFSET_X, CENTER_Y-OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf().rotationY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerBR = new OrientableLightGroup(lights);
        var outerBRHandler = new GroupEventHandlerV3(outerBR);
        eventGroups.put(1, new Pair<>(outerBR, outerBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(OUTER_OFFSET_X, CENTER_Y+OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf().rotationZ(180 * MathHelper.RADIANS_PER_DEGREE).rotateY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerTL = new OrientableLightGroup(lights);
        var outerTLHandler = new GroupEventHandlerV3(outerTL);
        eventGroups.put(2, new Pair<>(outerTL, outerTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-OUTER_OFFSET_X, CENTER_Y+OUTER_OFFSET_Y, OUTER_Z),
                new Quaternionf().rotationZ(180 * MathHelper.RADIANS_PER_DEGREE).rotateY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var outerTR = new OrientableLightGroup(lights);
        var outerTRHandler = new GroupEventHandlerV3(outerTR);
        eventGroups.put(3, new Pair<>(outerTR, outerTRHandler));

    }

    private void setupInnerLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(INNER_OFFSET_X, CENTER_Y-INNER_OFFSET_Y, INNER_Z),
                new Quaternionf().rotationY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerBL = new OrientableLightGroup(lights);
        var innerBLHandler = new GroupEventHandlerV3(innerBL);
        eventGroups.put(4, new Pair<>(innerBL, innerBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-INNER_OFFSET_X, CENTER_Y-INNER_OFFSET_Y, INNER_Z),
                new Quaternionf().rotationY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerBR = new OrientableLightGroup(lights);
        var innerBRHandler = new GroupEventHandlerV3(innerBR);
        eventGroups.put(5, new Pair<>(innerBR, innerBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(INNER_OFFSET_X, CENTER_Y+INNER_OFFSET_Y, INNER_Z),
                new Quaternionf().rotationZ(180 * MathHelper.RADIANS_PER_DEGREE).rotateY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerTL = new OrientableLightGroup(lights);
        var innerTLHandler = new GroupEventHandlerV3(innerTL);
        eventGroups.put(6, new Pair<>(innerTL, innerTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-INNER_OFFSET_X, CENTER_Y+INNER_OFFSET_Y, INNER_Z),
                new Quaternionf().rotationZ(180 * MathHelper.RADIANS_PER_DEGREE).rotateY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var innerTR = new OrientableLightGroup(lights);
        var innerTRHandler = new GroupEventHandlerV3(innerTR);
        eventGroups.put(7, new Pair<>(innerTR, innerTRHandler));
    }

    private void setupSideLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(SIDE_OFFSET_X, CENTER_Y-SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf().rotationZ(90 * MathHelper.RADIANS_PER_DEGREE).rotateY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideBL = new OrientableLightGroup(lights);
        var sideBLHandler = new GroupEventHandlerV3(sideBL);
        eventGroups.put(8, new Pair<>(sideBL, sideBLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-SIDE_OFFSET_X, CENTER_Y-SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf().rotationZ(-90 * MathHelper.RADIANS_PER_DEGREE).rotateY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideBR = new OrientableLightGroup(lights);
        var sideBRHandler = new GroupEventHandlerV3(sideBR);
        eventGroups.put(9, new Pair<>(sideBR, sideBRHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(SIDE_OFFSET_X, CENTER_Y+SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf().rotationZ(90 * MathHelper.RADIANS_PER_DEGREE).rotateY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideTL = new OrientableLightGroup(lights);
        var sideTLHandler = new GroupEventHandlerV3(sideTL);
        eventGroups.put(10, new Pair<>(sideTL, sideTLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-SIDE_OFFSET_X, CENTER_Y+SIDE_OFFSET_Y, SIDE_Z),
                new Quaternionf().rotationZ(-90 * MathHelper.RADIANS_PER_DEGREE).rotateY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, 0, 2), 7)
        ) {
            lights.put(lightID++, light);
        }
        var sideTR = new OrientableLightGroup(lights);
        var sideTRHandler = new GroupEventHandlerV3(sideTR);
        eventGroups.put(11, new Pair<>(sideTR, sideTRHandler));
    }

    private void setupDistantLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(
                    DISTANT_W,
                    CENTER_Y+DISTANT_H+(DISTANT_H*2/9f),
                    DISTANT_Z
                ),
                new Quaternionf()
                    .rotationX(-90 * MathHelper.RADIANS_PER_DEGREE)
                    .rotateY(180 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(-(DISTANT_W*2)/11f, 0, 0), 11)
        ) {
            lights.put(lightID++, light);
        }
        var distantT = new OrientableLightGroup(lights);
        var distantTHandler = new GroupEventHandlerV3(distantT);
        eventGroups.put(12, new Pair<>(distantT, distantTHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(-DISTANT_W, (CENTER_Y-DISTANT_H)-(DISTANT_H*2/9f), DISTANT_Z),
                new Quaternionf().rotationX(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f((DISTANT_W*2)/11f, 0, 0), 11)
        ) {
            lights.put(lightID++, light);
        }
        var distantD = new OrientableLightGroup(lights);
        var distantDHandler = new GroupEventHandlerV3(distantD);
        eventGroups.put(13, new Pair<>(distantD, distantDHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f(DISTANT_W+(DISTANT_W*2/11f), CENTER_Y-DISTANT_H, DISTANT_Z),
                new Quaternionf().rotationX(-90 * MathHelper.RADIANS_PER_DEGREE).rotateY(-90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, (DISTANT_H*2)/9f, 0), 9)
        ) {
            lights.put(lightID++, light);
        }
        var distantL = new OrientableLightGroup(lights);
        var distantLHandler = new GroupEventHandlerV3(distantL);
        eventGroups.put(14, new Pair<>(distantL, distantLHandler));

        lightID = 0;
        lights = new HashMap<>();
        for (var light : stackLights(
            new FloodLight(
                0.125f, 0.075f, 30, 14, 0.1f,
                new Vector3f((-DISTANT_W)-(DISTANT_W*2/11f), CENTER_Y+DISTANT_H, DISTANT_Z),
                new Quaternionf().rotationX(-90 * MathHelper.RADIANS_PER_DEGREE).rotateY(90 * MathHelper.RADIANS_PER_DEGREE)
            ),
            new Vector3f(0, -(DISTANT_H*2)/9f, 0), 9)
        ) {
            lights.put(lightID++, light);
        }
        var distantR = new OrientableLightGroup(lights);
        var distantRHandler = new GroupEventHandlerV3(distantR);
        eventGroups.put(15, new Pair<>(distantR, distantRHandler));

    }

    @Override
    public WeaveEnvironment reset() {
        eventGroups.forEach((k, v) -> {
            v.getRight().clear();
            v.getRight().reset();
        });

        return this;
    }

    @Override
    protected int getLightCount(int group) {
        if (group < 12) { return 8; }
        else if (group < 14) { return 12; }
        else { return 10; }
    }

    @Override
    protected void linkEvents(int group, int lightID, List<LightEventV3> lightEvents, HashMap<TransformState.Axis, List<TransformEvent>> transformEvents) {

        if (eventGroups.containsKey(group)) {
            eventGroups.get(group).getRight().linkLightEvents(lightEvents);
            eventGroups.get(group).getRight().linkTransformEvents(lightID, transformEvents);
        }

    }


    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);
        eventGroups.forEach((k, v) -> {
            v.getRight().update(beat);
        });

    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);

        eventGroups.forEach((k, v) -> {
            v.getLeft().render(matrices, camera);
        });

    }
}
