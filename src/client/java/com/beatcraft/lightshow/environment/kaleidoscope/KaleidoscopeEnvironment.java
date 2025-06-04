package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;

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

    @Override
    protected LightGroupV2 setupLeftLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupRingLights() {
        //ringLights = new KaleidoscopeRingLights(
        //
        //);
        return null;
    }

    private static final float[] FOG_HEIGHTS = new float[]{-12, -2};
    @Override
    public float[] getFogHeights() {
        return FOG_HEIGHTS;
    }
}
