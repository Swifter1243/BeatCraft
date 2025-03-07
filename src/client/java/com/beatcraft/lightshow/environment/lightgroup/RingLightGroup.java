package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.thefirst.OuterRing;
import com.beatcraft.lightshow.environment.thefirst.InnerRing;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public class RingLightGroup extends ActionLightGroupV2 {

    private final RingLightHandler innerRing;
    private final RingLightHandler outerRing;

    public RingLightGroup() {
        innerRing = new RingLightHandler(InnerRing.getInstance(), 30, new Vector3f(0, 2, 10), 5);
        outerRing = new RingLightHandler(OuterRing.getInstance(), 15, new Vector3f(0, 2, 7), 8.75f);

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
        super.render(matrices, camera);
        innerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
        outerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
    }
}
