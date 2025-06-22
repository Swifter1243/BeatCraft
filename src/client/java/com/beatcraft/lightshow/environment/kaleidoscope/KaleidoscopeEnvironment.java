package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.lightshow.lights.LightObject;

import java.util.ArrayList;
import java.util.HashMap;

public class KaleidoscopeEnvironment extends EnvironmentV2 {

    private KaleidoscopeRingLights ringLights;
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

    @Override
    public String getID() {
        return "KaleidoscopeEnvironment";
    }

    @Override
    public void setup() {

        ringLights = new KaleidoscopeRingLights();

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

        super.setup();
    }

    @Override
    protected LightGroupV2 setupLeftLasers() {

        return new RotatingLightsGroup(new HashMap<>(), left);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        return new RotatingLightsGroup(new HashMap<>(), right);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {

        return new StaticLightsGroup(back);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        return new StaticLightsGroup(middle);
    }

    @Override
    protected LightGroupV2 setupRingLights() {
        return ringLights;
    }

    private static final float[] FOG_HEIGHTS = new float[]{-12, -2};
    @Override
    public float[] getFogHeights() {
        return FOG_HEIGHTS;
    }
}
