package com.beatcraft.lightshow.ring_lights;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RingLightHandler extends LightObject {

    Random random = Random.create();

    private int ringCount;
    private final float ringOffset;
    public float zoom = 1;
    public float ringRotation = 0;
    public float rotationStep = 0;

    /// offset deltas, this is how far the head ring is allowed to jump
    public float[] jumpOffsets = new float[0];

    /// ring offsets, a list of valid rotation offsets between 2 rings
    public float[] rotationOffsets = new float[0];

    private float currentOffset = 0;


    protected static class RingHandler {
        private final RingLightHandler controller;
        private float rotation = 0;
        private Float startTime = null;
        private Float zoomStart = null;
        private float startRotation = 0;
        private float targetRotation = 0;
        private RingHandler nextRing = null;
        private float prevZoom = 1;
        private final int index;
        private final LightObject ringLight;
        private float cachedRingRotation = 0;
        private float cachedRotationStep = 0;

        protected RingHandler(RingLightHandler controller, int index, LightObject ringLight) {
            this.ringLight = ringLight;
            this.controller = controller;
            this.index = index;
        }

        protected void setTarget(float songTime, float ringRotation, float rotationStep) {
            startRotation = rotation;
            cachedRingRotation = ringRotation;
            cachedRotationStep = rotationStep;
            this.targetRotation = ringRotation + (rotationStep * index);
            startTime = songTime;
            setNext = true;
        }

        protected void setZoomStart(float songTime) {
            prevZoom = controller.zoom;
            zoomStart = songTime;
            if (nextRing != null) {
                nextRing.setZoomStart(songTime);
            }
        }

        private float lerpZoom(float songTime) {
            if (zoomStart == null) return prevZoom;

            var dt = songTime - zoomStart;
            if (dt >= 1) {
                prevZoom = controller.zoom;
                zoomStart = null;
            }

            return MathHelper.lerp(Easing.easeOutExpo(dt), prevZoom, controller.zoom);

        }

        protected void reset() {
            rotation = 0;
            startTime = null;
            zoomStart = null;
            startRotation = 0;
            targetRotation = 0;
            prevZoom = 1;
            if (nextRing != null) {
                nextRing.reset();
            }
        }

        private boolean setNext = false;

        protected void update(float songTime) {
            if (startTime != null) {
                float dt = songTime - startTime;
                rotation = MathHelper.lerp(Easing.easeOutExpo(dt), startRotation, targetRotation);

                if (nextRing != null && dt > 0.01f && setNext) {
                    nextRing.setTarget(songTime, cachedRingRotation+cachedRotationStep, cachedRotationStep);
                    setNext = false;
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
            if (ringLight == null) return;
            ringLight.setOffset(
                new Vector3f(0, 0, controller.ringOffset * lerpZoom(BeatmapPlayer.getCurrentSeconds()) * index)
                    .rotate(controller.orientation).rotate(controller.rotation)
                    .add(controller.position).add(controller.offset)
            );
            ringLight.setRotation(
                new Quaternionf().rotationZ(rotation).mul(controller.orientation).mul(controller.rotation)
            );
            ringLight.render(matrices, camera, bloomfog);
            if (nextRing != null) {
                nextRing.render(matrices, camera, bloomfog);
            }
        }

    }

    private final RingHandler headRing;

    public RingLightHandler(
        Function<BiFunction<Vector3f, Quaternionf, LightObject>, LightObject> ringFactory,
        BiFunction<Vector3f, Quaternionf, LightObject> lightBuilder,
        int count, Vector3f position, float ringGap
    ) {
        ringCount = count;
        this.position = position;
        ringOffset = ringGap;

        headRing = new RingHandler(this, 0, ringFactory.apply(lightBuilder));

        var last = headRing;

        for (int i = 1; i < ringCount; i++) {
            var current = new RingHandler(this, i, ringFactory.apply(lightBuilder));
            last.nextRing = current;
            last = current;
        }

    }

    public float rotationReset = 0;

    public void reset() {
        ringRotation = 0;
        rotationStep = rotationReset;
        currentOffset = 0;
        zoom = 1;
        headRing.reset();
        headRing.startTime = 0f;
        headRing.update(0);
    }

    public void update(float songTime) {
        headRing.update(songTime);
    }

    @Override
    public RingLightHandler cloneOffset(Vector3f offset) {
        return this;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        headRing.render(matrices, camera, bloomfog);
    }


    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
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

        headRing.setTarget(BeatmapPlayer.getCurrentSeconds(), ringRotation, rotationStep);

    }

    public void setZoom(float value) {
        headRing.setZoomStart(BeatmapPlayer.getCurrentSeconds());
        zoom = value;
    }

    public float getZoom() {
        return zoom;
    }

}
