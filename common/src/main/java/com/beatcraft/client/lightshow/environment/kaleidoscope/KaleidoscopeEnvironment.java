package com.beatcraft.client.lightshow.environment.kaleidoscope;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.environment.Environment;
import com.beatcraft.client.lightshow.environment.EnvironmentV2;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.client.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.client.lightshow.ring_lights.RingLightHandlerOld;
import com.beatcraft.client.render.environment.KaleidoscopeRenderer;
import com.beatcraft.client.render.lights.FloodLight;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class KaleidoscopeEnvironment extends EnvironmentV2 {

    private final KaleidoscopeRenderer renderer = new KaleidoscopeRenderer();

    private RingLightHandler ringLights;
    /*
    Lights:
    2 light on opposite sides per segment
    20 segments

    Back = tips
    Ring = middle
    Left = half 1 between
    Right = half 2 between
    center = distant lights and end spike lights

     */

    HashMap<Integer, LightObject> middle;
    HashMap<Integer, LightObject> left;
    HashMap<Integer, LightObject> right;
    HashMap<Integer, LightObject> back;

    public KaleidoscopeEnvironment(BeatmapController map) {
        super(map);
    }

    @Override
    public String getID() {
        return "KaleidoscopeEnvironment";
    }

    @Override
    public void setup() {
        var rpd = Mth.DEG_TO_RAD;

        AtomicInteger linkI = new AtomicInteger(1);
        AtomicInteger linkO = new AtomicInteger(41);

        ringLights = new RingLightHandler(
            mapController,
            new RingLightHandler.RingLightData(
                (map, pos) -> new RingSpike(map, pos, new Quaternionf()),
                (lights) -> {
                    int idx = linkI.get();
                    var light = lights.get(idx);
                    linkI.set(idx + 2);
                    return light;
                },
                new RingLightHandler.LightDelta(
                    1, 40, 2, 5
                ),
                new RingLightHandler.PresetPositions(
                    new float[]{
                        -90 * rpd,
                        90 * rpd
                    },
                    new float[]{
                        0,
                        1 * rpd, -1 * rpd,
                        2 * rpd, -2 * rpd,
                        5 * rpd, -5 * rpd,
                        10 * rpd, -10 * rpd,
                        12.5f * rpd, -12.5f * rpd,
                        15 * rpd, -15 * rpd,
                        20 * rpd, -20 * rpd,
                        22.5f * rpd, -22.5f * rpd,
                        25 * rpd, -25 * rpd,
                        30 * rpd, -30 * rpd,
                        45 * rpd, -45 * rpd,
                    }
                ),
                new Vector3f(0, 0, 12),
                20,
                0, 45f * rpd
            ),
            new RingLightHandler.RingLightData(
                (map, pos) -> new FloodLight(
                    map,
                    4f, 4, 250, 200, 2f,
                    new float[]{20f},
                    pos, new Quaternionf(), 1
                ),
                (lights) -> lights.get(linkO.getAndIncrement()),
                new RingLightHandler.LightDelta(
                    41, 61, 1, 0
                ),
                new RingLightHandler.PresetPositions(
                    new float[]{
                        -90 * rpd,
                        -45 * rpd,
                        90 * rpd,
                        45 * rpd,
                    },
                    new float[]{
                        0,
                        1 * rpd,
                        2 * rpd,
                        3 * rpd,
                        4 * rpd,
                        5 * rpd,
                        10 * rpd,
                        15 * rpd,
                        -1 * rpd,
                        -2 * rpd,
                        -3 * rpd,
                        -4 * rpd,
                        -5 * rpd,
                        -10 * rpd,
                        -15 * rpd,
                    }
                ),
                new Vector3f(0, 0, 250),
                10,
                0, 10f * rpd
            )
        );

        middle = new HashMap<>();
        left = new HashMap<>();
        right = new HashMap<>();
        back = new HashMap<>();

        var mi = 1;
        var li = 1;
        var ri = 1;
        var bi = 1;

        for (int i = 1; i < 40; i += 2) {
            var baseLight = (RingSpike) ringLights.lights.get(i);
            var controllers = baseLight.getControllers();

            // add the missing spike tip lights
            ringLights.lights.put(i + 1, controllers[0]);

            middle.put(mi++, controllers[1]);
            middle.put(mi++, controllers[2]);

            left.put(li++, controllers[3]);
            right.put(ri++, controllers[4]);

            back.put(bi++, controllers[5]);
            back.put(bi++, controllers[6]);

        }

        for (int i = 41; i < 61; ++i) {
            // back.put(bi++, ringLights.lights.get(i)); // TODO: make better lights
            ringLights.lights.remove(i);
        }


        super.setup();
    }

    @Override
    protected LightGroupV2 setupLeftLasers() {

        return new RotatingLightsGroup(mapController, new HashMap<>(), left);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        return new RotatingLightsGroup(mapController, new HashMap<>(), right);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {

        return new StaticLightsGroup(mapController, back);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        return new StaticLightsGroup(mapController, middle);
    }

    @Override
    protected LightGroupV2 setupRingLights() {
        return ringLights;
    }

    private static final float[] FOG_HEIGHTS = new float[]{-28, -8};
    @Override
    public float[] getFogHeights() {
        return FOG_HEIGHTS;
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        super.render(matrices, camera, alpha);

        renderer.render(matrices, camera, mapController, alpha);
    }

}
