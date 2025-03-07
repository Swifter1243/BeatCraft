package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
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
    //private final RingLightHandler outerRing;

    public RingLightGroup() {
        innerRing = new RingLightHandler(InnerRing.getInstance(), 30, new Vector3f(0, 2, 10), 5);
        //outerRing = new RingLightHandler(null, 15, new Vector3f(0, 2, 7), 2.5f);

        var rpd = MathHelper.RADIANS_PER_DEGREE;

        innerRing.jumpOffsets = new float[]{
            -90 * rpd,
            90 * rpd
        };

        //outerRing.jumpOffsets = new float[]{
        //    -45 * rpd,
        //    45 * rpd
        //};

        innerRing.rotationOffsets = new float[]{
            0,
            3 * rpd,
            -3 * rpd,
            7 * rpd,
            -7 * rpd,
            11 * rpd,
            -11 * rpd
        };

    }

    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        int v = event.getValue();

        switch (eventGroup)
        {
            case RING_SPIN -> handleRingSpin(v);
            case RING_ZOOM -> handleRingZoom(v);
        }
    }

    private void handleRingSpin(int v) {
        if (v == 0) {
            innerRing.spinRandom();
            //outerRing.spinRandom();
        }
    }

    private void handleRingZoom(int v) {
        //BeatCraft.LOGGER.info("ZOOM {}", i);
        if (v == 0) {
            innerRing.setZoom(innerRing.getZoom() >= 0.99 ? 0.3f : 1);
        }
    }

    @Override
    public void update(float beat, double deltaTime) {
        float t = BeatmapPlayer.getCurrentSeconds();
        innerRing.update(t);
        //outerRing.update(t);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
        innerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
        //outerRing.render(matrices, camera, BeatcraftRenderer.bloomfog);
    }
}
