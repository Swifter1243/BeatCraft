package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.EventGroup;
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
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RingLightGroup extends ActionLightGroupV2 {

    private BeatmapPlayer mapController;

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;




    private static HashMap<Integer, LightObject> buildRingLights(Callable<LightObject> lightFactory) {
        HashMap<Integer, LightObject> map = new HashMap<>();

        for (int i = 1; i <= 30*4; i++) {
            try {
                map.put(i, lightFactory.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        return map;
    }

    private int linkIndex = 1;
    private LightObject linkLight(BeatmapPlayer map, Vector3f position, Quaternionf orientation) {
        var light = lights.get(linkIndex++);
        light.setPosition(position);
        light.setRotation(orientation);
        return light;
    }

    public void reset() {
        innerRing.reset();
        outerRing.reset();
    }

    public RingLightGroup(
        BeatmapPlayer map,
        Function<TriFunction<BeatmapPlayer, Vector3f, Quaternionf, LightObject>, LightObject> innerRingFactory,
        Function<TriFunction<BeatmapPlayer, Vector3f, Quaternionf, LightObject>, LightObject> outerRingFactory,
        Callable<LightObject> outerLightFactory
    ) {
        this(
            map,
            innerRingFactory, outerRingFactory, outerLightFactory,
            2, 15, 7, 8.75f
        );
    }


    public RingLightGroup(
        BeatmapPlayer map,
        Function<TriFunction<BeatmapPlayer, Vector3f, Quaternionf, LightObject>, LightObject> innerRingFactory,
        Function<TriFunction<BeatmapPlayer, Vector3f, Quaternionf, LightObject>, LightObject> outerRingFactory,
        Callable<LightObject> outerLightFactory,
        float height, int outerRingCount, float outerRingZ, float outerRingSpacing
    ) {
        // start at idx 1
        super(map, buildRingLights(outerLightFactory));
        mapController = map;

        innerRing = new RingLightHandler(map, innerRingFactory, (m, v, q) -> null, 30, new Vector3f(0, height, 14), 5);
        outerRing = new RingLightHandler(map, outerRingFactory, this::linkLight, outerRingCount, new Vector3f(0, height, outerRingZ), outerRingSpacing);

        var rpd = Mth.DEG_TO_RAD;

        innerRing.jumpOffsets = new float[]{
            -90 * rpd,
            90 * rpd
        };

        outerRing.jumpOffsets = new float[]{
            -90 * rpd,
            90 * rpd
        };

        innerRing.rotationOffsets = new float[]{
            0,
            3 * rpd,
            -3 * rpd,
            7 * rpd,
            -7 * rpd,
            11 * rpd,
            -11 * rpd
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
