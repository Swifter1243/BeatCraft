package com.beatcraft.client.lightshow.ring_lights;

import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class RingLightHandler extends LightObject {

    private final RingHandler headRing;

    private final float ringGap;

    private float startZoom = 1.0f;
    private float targetZoom = 1.0f;
    private float startZoomTime = 0;
    private float endZoomTime = 0;

    public float zoom = 1.0f;

    private float currentRotation = 0;

    public final PresetPositions presets;

    private float lerpEffect(
        float currentTime,
        float startTime, float endTime,
        float startFx, float endFx
    ) {
        if (startTime == endTime) {
            return currentTime >= startTime ? endFx : startFx;
        }
        var f = MathUtil.inverseLerp(startTime, endTime, currentTime);
        f = Easing.easeOutExpo(f);
        var minFx = Math.min(startFx, endFx);
        var maxFx = Math.max(startFx, endFx);
        return Math.clamp(minFx, maxFx, Math.lerp(startFx, endFx, f));
    }

    public record PresetPositions(
        float[] jumpOffsets,
        float[] rotationOffsets
    ) {
        public float getJumpOffset(RandomSource random) {
            var i = random.nextInt(0, jumpOffsets.length);
            return jumpOffsets[i];
        }
        public float getRotationOffset(RandomSource random) {
            var i = random.nextInt(0, rotationOffsets.length);
            return rotationOffsets[i];
        }
    }



    protected class RingHandler {

        private RingHandler nextRing = null;
        private final int index;
        private final LightObject ringLight;

        private float startRotation = 0;
        private float targetRotation = 0;
        private float startRotationTime = 0;
        private float endRotationTime = 0;

        private float firstAngle = 0;
        private float offset = 0;

        private float propagationDuration = 0;
        private boolean triggeredPropagation = false;

        private float rotation = 0;

        protected RingHandler(int index, LightObject ringLight) {
            this.index = index;
            this.ringLight = ringLight;
        }

        protected void setTarget(float firstAngle, float offset, float startTime, float endTime, float propTime) {
            startRotation = rotation;
            this.firstAngle = firstAngle;
            this.offset = offset;
            targetRotation = firstAngle + (offset * index);
            startRotationTime = startTime;
            endRotationTime = endTime;
            propagationDuration = propTime;
            triggeredPropagation = false;
            if (propTime == 0 && nextRing != null) {
                if (startTime == endTime) {
                    rotation = targetRotation;
                    startRotation = targetRotation;
                }
                triggeredPropagation = true;
                nextRing.setTarget(firstAngle, offset, startRotationTime+propagationDuration, (startRotationTime+propagationDuration) + (endRotationTime-startRotationTime), propagationDuration);
            }
        }

        protected void update(float songTime) {

            rotation = lerpEffect(songTime, startRotationTime, endRotationTime, startRotation, targetRotation);

            if (nextRing != null) {
                if (!triggeredPropagation) {
                    if (propagationDuration == 0 || 1f <= MathUtil.inverseLerp(startRotationTime, startRotationTime+propagationDuration, songTime)) {
                        triggeredPropagation = true;
                        nextRing.setTarget(firstAngle, offset, startRotationTime+propagationDuration, (startRotationTime+propagationDuration) + (endRotationTime-startRotationTime), propagationDuration);
                    }
                }

                nextRing.update(songTime);
            }
        }

        protected void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
            if (ringLight != null) {

                var off = new Vector3f();
                var rot = new Quaternionf();

                off.set(0, 0, ringGap * zoom * index)
                    .rotate(orientation)
                    .rotate(RingLightHandler.this.rotation)
                    .add(position)
                    .add(RingLightHandler.this.offset)
                ;
                rot.rotationZ(rotation)
                    .mul(orientation)
                    .mul(RingLightHandler.this.rotation);

                ringLight.setOffset(off);
                ringLight.setRotation(rot);

                ringLight.render(matrices, camera, alpha, bloomfog);
            }
            if (nextRing != null) nextRing.render(matrices, camera, alpha, bloomfog);

        }

        protected void reset() {
            startRotation = 0;
            targetRotation = 0;
            startRotationTime = 0;
            endRotationTime = 0;
            if (nextRing != null) nextRing.reset();
        }

    }


    public RingLightHandler(
        BeatmapController map,
        Function<TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject>, LightObject> ringFactory,
        TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject> lightBuilder,
        int count, Vector3f position, float ringGap,
        PresetPositions presets
    ) {
        super(map);
        this.position = position;
        this.ringGap = ringGap;
        this.presets = presets;

        headRing = new RingHandler(0, ringFactory.apply(lightBuilder));

        var last = headRing;

        for (int i = 1; i < count; ++i) {
            var current = new RingHandler(i, ringFactory.apply(lightBuilder));
            last.nextRing = current;
            last = current;
        }

    }

    public void update(float songTime) {

        zoom = lerpEffect(songTime, startZoomTime, endZoomTime, startZoom, targetZoom);

        headRing.update(songTime);
    }

    public void setZoom(float value, float speedPercentage) {
        startZoom = zoom;
        targetZoom = value;
        startZoomTime = mapController.currentSeconds;
        endZoomTime = startZoomTime + (1f / speedPercentage);
    }

    public float getCurrentRotation() {
        return headRing.rotation;
    }

    public void spinTo(float angle, float offset, float propagationTime, float spinTime) {
        var t = mapController.currentSeconds;
        currentRotation = angle;
        headRing.setTarget(angle, offset, t, t + spinTime, propagationTime);
    }

    public void reset() {
        startZoom = 1f;
        targetZoom = 1f;
        startZoomTime = 0f;
        endZoomTime = 0f;
        headRing.reset();
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return this;
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
        headRing.render(matrices, camera, alpha, bloomfog);
    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }


}
