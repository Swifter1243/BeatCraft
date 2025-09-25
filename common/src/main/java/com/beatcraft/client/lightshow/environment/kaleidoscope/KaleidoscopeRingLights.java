package com.beatcraft.client.lightshow.environment.kaleidoscope;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.client.lightshow.event.events.ValueEvent;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;

public class KaleidoscopeRingLights extends ActionLightGroupV2 {

    private static final float DISTANT_Z = 50;
    private static final float DISTANT_Y = 0;
    private static final float SPIKES_Z = 9;
    private static final float SPIKES_Y = 1;

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;

    protected static HashMap<Integer, LightObject> buildRingLights(BeatmapController beatmap) {
        var map = new HashMap<Integer, LightObject>();

        var pos = new Vector3f(0, 0, 8);

        RingSpike.clearInstances();
        for (int i = 1; i < 40; i += 2) {
            try {
                map.put(i, new RingSpike(beatmap, new Vector3f(pos), new Quaternionf()));
                pos.add(0, 0, 5);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    public KaleidoscopeRingLights(BeatmapController map) {
        super(map, buildRingLights(map));

        //
        innerRing = new RingLightHandler(map, this::createInner, this::linkInner, 20, new Vector3f(0, 0, 12), 5);
        outerRing = new RingLightHandler(map, this::createOuter, this::linkOuter, 10, new Vector3f(), 0);

        var rpd = Mth.DEG_TO_RAD;

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

        innerRing.spinTo(0, 45f/2f * rpd, 0, 0);
    }


    private int linkInnerIndex = 1;
    private LightObject linkInner(BeatmapController map, Vector3f pos, Quaternionf ori) {
        var light = lights.get(linkInnerIndex);
        linkInnerIndex += 2;
        light.setPosition(pos);
        light.setRotation(ori);
        return light;
    }

    private LightObject linkOuter(BeatmapController map, Vector3f pos, Quaternionf ori) {
        return new DistantLight(mapController, pos, ori);
    }

    private int fetchIndex = 1;
    private LightObject createInner(TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject> f) {
        var light = lights.get(fetchIndex);
        fetchIndex += 2;
        return light;
    }

    private LightObject createOuter(TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject> f) {
        return f.apply(mapController, new Vector3f(0, 0, 50), new Quaternionf());
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
        float t = mapController.currentSeconds;
        innerRing.update(t);
        outerRing.update(t);
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        innerRing.render(matrices, camera, alpha, BeatcraftRenderer.bloomfog);
        outerRing.render(matrices, camera, alpha, BeatcraftRenderer.bloomfog);
    }

}
