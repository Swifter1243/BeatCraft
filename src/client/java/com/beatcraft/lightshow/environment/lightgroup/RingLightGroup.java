package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.thefirst.OuterRing;
import com.beatcraft.lightshow.environment.thefirst.InnerRing;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.lights.GlowingCuboid;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RingLightGroup extends ActionLightGroupV2 {

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
    private LightObject linkLight(Vector3f position, Quaternionf orientation) {
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
        Function<BiFunction<Vector3f, Quaternionf, LightObject>, LightObject> innerRingFactory,
        Function<BiFunction<Vector3f, Quaternionf, LightObject>, LightObject> outerRingFactory,
        Callable<LightObject> outerLightFactory
    ) {
        // start at idx 1
        super(buildRingLights(outerLightFactory));

        innerRing = new RingLightHandler(innerRingFactory, (v, q) -> null, 30, new Vector3f(0, 2, 14), 5);
        outerRing = new RingLightHandler(outerRingFactory, this::linkLight, 15, new Vector3f(0, 2, 7), 8.75f);

        var rpd = MathHelper.RADIANS_PER_DEGREE;

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
