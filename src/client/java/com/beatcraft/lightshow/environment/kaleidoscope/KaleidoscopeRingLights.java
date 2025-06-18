package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.render.BeatCraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class KaleidoscopeRingLights extends ActionLightGroupV2 {

    private static final float DISTANT_Z = 50;
    private static final float DISTANT_Y = 0;
    private static final float SPIKES_Z = 9;
    private static final float SPIKES_Y = 1;

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;

    protected static HashMap<Integer, LightObject> buildRingLights() {
        var map = new HashMap<Integer, LightObject>();

        for (int i = 1; i < 40; i += 2) {
            try {
                map.put(i, new RingSpike(new Vector3f(), new Quaternionf()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    public KaleidoscopeRingLights() {
        super(buildRingLights());

        //
        innerRing = new RingLightHandler(this::createInner, this::linkInner, 20, new Vector3f(), 0);
        outerRing = new RingLightHandler(this::createOuter, this::linkOuter, 10, new Vector3f(), 5);

        var rpd = MathHelper.RADIANS_PER_DEGREE;

        innerRing.ringRotation = 45 * rpd;
        innerRing.jumpOffsets = new float[]{
            -90 * rpd,
            90 * rpd
        };

        innerRing.rotationOffsets = new float[]{
            0,
            1 * rpd, -1 * rpd,
            2 * rpd, -2 * rpd,
            5 * rpd, -5 * rpd,
            10 * rpd, -10 * rpd,
            12.5f * rpd, -12.5f * rpd,
            15 * rpd, -15 * rpd,
            20 * rpd, -20 * rpd,
            22.5f * rpd, -22.5f * rpd,
            25 * rpd, -25 * rpd
        };


        outerRing.jumpOffsets = new float[]{
            -90 * rpd,
            90 * rpd
        };

        outerRing.rotationOffsets = new float[]{
            0,
            1 * rpd,
            2 * rpd,
            3 * rpd,
            4 * rpd,
            5 * rpd,
            -1 * rpd,
            -2 * rpd,
            -3 * rpd,
            -4 * rpd,
            -5 * rpd
        };

    }


    private int linkInnerIndex = 1;
    private LightObject linkInner(Vector3f pos, Quaternionf ori) {
        var light = lights.get(linkInnerIndex);
        linkInnerIndex += 2;
        light.setPosition(pos);
        light.setRotation(ori);
        return light;
    }

    private LightObject linkOuter(Vector3f pos, Quaternionf ori) {
        return new DistantLight(pos, ori);
    }

    private int fetchIndex = 1;
    private LightObject createInner(BiFunction<Vector3f, Quaternionf, LightObject> f) {
        var light = lights.get(fetchIndex);
        fetchIndex += 2;
        return light;
    }

    private LightObject createOuter(BiFunction<Vector3f, Quaternionf, LightObject> f) {
        return f.apply(new Vector3f(0, 0, 50), new Quaternionf());
    }


    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        switch (eventGroup)
        {
            case RING_SPIN -> handleRingSpin();
            case RING_ZOOM -> handleRingZoom();
        }
    }

    private void handleRingSpin() {
        innerRing.spinRandom();
        outerRing.spinRandom();
    }

    private void handleRingZoom() {
        innerRing.setZoom(innerRing.getZoom() >= 0.99 ? 0.3f : 1);
    }

    @Override
    public void update(float beat, double deltaTime) {
        float t = BeatmapPlayer.getCurrentSeconds();
        innerRing.update(t);
        outerRing.update(t);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        innerRing.render(matrices, camera, BeatCraftRenderer.bloomfog);
        outerRing.render(matrices, camera, BeatCraftRenderer.bloomfog);
    }
}
