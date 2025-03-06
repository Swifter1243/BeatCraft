package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.LightGroupV2;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public class RingLightGroup extends LightGroupV2 {

    private final RingLightHandler innerRing;
    //private final RingLightHandler outerRing;

    public RingLightGroup() {
        innerRing = new RingLightHandler(InnerRing.getInstance(), 30, new Vector3f(0, 2, 10), 1.333f);
        //outerRing = new RingLightHandler(null, 15, new Vector3f(0, 2, 7), 2.5f);

        var rpd = MathHelper.RADIANS_PER_DEGREE;

        innerRing.jumpOffsets = new float[]{
            -45 * rpd,
            45 * rpd
        };

        //outerRing.jumpOffsets = new float[]{
        //    -45 * rpd,
        //    45 * rpd
        //};

        innerRing.rotationOffsets = new float[]{
            0,
            5 * rpd,
            -5 * rpd,
            10 * rpd,
            -10 * rpd,
            15 * rpd,
            -15 * rpd
        };

    }

    @Override
    public void handleEvent(EventGroup group, Object obj) {
        //if (group == EventGroup.RING_LIGHTS && obj instanceof LightState lightState) {
        //    outerRing.setLightState(lightState);
        //}

        if (group == EventGroup.RING_SPIN && obj instanceof Integer i && i != -1000000000) {
            innerRing.spinRandom();
            //outerRing.spinRandom();
        }

        if (group == EventGroup.RING_ZOOM && obj instanceof Integer i && i != -1000000000) {
            innerRing.setZoom(innerRing.getZoom() == 1 ? 0.4f : 1);
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
