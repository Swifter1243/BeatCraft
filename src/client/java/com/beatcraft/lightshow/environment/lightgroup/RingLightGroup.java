package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.thefirst.OuterRing;
import com.beatcraft.lightshow.environment.thefirst.InnerRing;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.lights.GlowingCuboid;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;

public class RingLightGroup extends ActionLightGroupV2 {

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;



    private static final float ringRadius = 27;
    private static final float lightLength = 6;
    private static final float lightSize = 0.2f;

    private static HashMap<Integer, LightObject> buildRingLights() {
        HashMap<Integer, LightObject> map = new HashMap<>();

        for (int i = 1; i <= 30*4; i++) {
            map.put(i, new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-lightLength/2, -lightSize, -lightSize),
                    new Vector3f(lightLength/2, lightSize, lightSize)
                ),
                new Vector3f(0, ringRadius-(lightSize+0.01f), lightSize),
                new Quaternionf()
            ));
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

    public RingLightGroup() {
        // start at idx 1
        super(buildRingLights());

        innerRing = new RingLightHandler(InnerRing::getInstance, (v, q) -> null, 30, new Vector3f(0, 2, 10), 5);
        outerRing = new RingLightHandler(OuterRing::new, this::linkLight, 15, new Vector3f(0, 2, 7), 8.75f);

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
        innerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
        outerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
    }
}
