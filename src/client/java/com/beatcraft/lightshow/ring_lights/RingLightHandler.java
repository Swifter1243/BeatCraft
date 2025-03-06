package com.beatcraft.lightshow.ring_lights;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RingLightHandler extends LightObject {

    Random random = Random.create();

    private LightObject ringLight;
    private int ringCount;
    private float ringOffset;
    private float zoom = 1;
    private float ringRotation = 0;
    private float rotationStep = 0;

    /// offset deltas, this is how far the head ring is allowed to jump
    public float[] jumpOffsets = new float[0];

    /// ring offsets, a list of valid rotation offsets between 2 rings
    public float[] rotationOffsets = new float[0];

    private float currentOffset = 0;


    protected static class RingHandler {
        private RingLightHandler controller;
        private float rotation = 0;
        private Float startTime = null;
        private float startRotation = 0;
        private float targetRotation = 0;
        private RingHandler nextRing = null;
        private final int index;

        protected RingHandler(RingLightHandler controller, int index) {
            this.controller = controller;
            this.index = index;
        }

        protected void setTarget(float songTime) {
            startRotation = rotation;
            this.targetRotation = controller.ringRotation + (controller.rotationStep * index);
            startTime = songTime;
        }

        protected void update(float songTime) {
            if (startTime != null) {
                float dt = songTime - startTime;
                rotation = MathHelper.lerp(startRotation, targetRotation, Easing.easeOutExpo(dt));

                if (dt > 0.1f && nextRing != null) {
                    nextRing.setTarget(songTime);
                }

                if (dt >= 1) {
                    rotation = targetRotation;
                    startTime = null;
                }

            }

            if (nextRing != null) {
                nextRing.update(songTime);
            }

        }

        protected void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
            controller.ringLight.setOffset(
                new Vector3f(0, 0, controller.ringOffset * controller.zoom * index)
                    .rotate(controller.orientation).rotate(controller.rotation)
                    .add(controller.position).add(controller.offset)
            );
            controller.ringLight.setRotation(
                new Quaternionf(controller.orientation).mul(controller.rotation)
            );
            controller.ringLight.render(matrices, camera, bloomfog);
            if (nextRing != null) {
                nextRing.render(matrices, camera, bloomfog);
            }
        }

    }

    private final RingHandler headRing;

    public RingLightHandler(LightObject ringLight, int count, Vector3f position, float ringGap) {
        this.ringLight = ringLight;
        ringCount = count;
        this.position = position;
        ringOffset = ringGap;

        headRing = new RingHandler(this, 0);

        var last = headRing;

        for (int i = 1; i < ringCount; i++) {
            var current = new RingHandler(this, i);
            last.nextRing = current;
            last = current;
        }

    }

    public void update(float songTime) {
        headRing.update(songTime);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        headRing.render(matrices, camera, bloomfog);
    }

    @Override
    public void setBrightness(float value) {
        ringLight.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        ringLight.setColor(color);
    }

    @Override
    public void setLightState(LightState state) {
        ringLight.setLightState(state);
    }

    public void spinRandom() {

        float offset = currentOffset;

        while (offset == currentOffset && rotationOffsets.length > 1) {
            var i = random.nextBetween(0, rotationOffsets.length - 1);
            offset = rotationOffsets[i];
        }

        rotationStep = offset;

        var i = random.nextBetween(0, jumpOffsets.length-1);

        ringRotation += jumpOffsets[i];
        headRing.setTarget(BeatmapPlayer.getCurrentSeconds());

    }

    public void setZoom(float value) {
        zoom = value;
    }

    public float getZoom() {
        return zoom;
    }

}
