package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class KaleidoscopeRingLights extends RingLightGroup {

    private static final float DISTANT_Z = 50;
    private static final float DISTANT_Y = 0;
    private static final float SPIKES_Z = 9;
    private static final float SPIKES_Y = 1;

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;

    public KaleidoscopeRingLights(
        Function<BiFunction<Vector3f, Quaternionf, LightObject>, LightObject> innerRingFactory,
        Function<BiFunction<Vector3f, Quaternionf, LightObject>, LightObject> outerRingFactory,
        Callable<LightObject> outerLightFactory
    ) {
        super(innerRingFactory, outerRingFactory, outerLightFactory);

        //
        innerRing = new RingLightHandler(innerRingFactory, this::linkInner, 20, new Vector3f(), 0);
        outerRing = new RingLightHandler(innerRingFactory, this::linkOuter, 10, new Vector3f(), 5);


    }


    private LightObject linkInner(Vector3f pos, Quaternionf ori) {
        return null;
    }

    private LightObject linkOuter(Vector3f pos, Quaternionf ori) {
        return null;
    }



}
