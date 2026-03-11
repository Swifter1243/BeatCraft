package com.beatcraft.client.lightshow.ring_lights;

import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.client.lightshow.event.events.RingRotationEvent;
import com.beatcraft.client.lightshow.event.events.RingZoomEvent;
import com.beatcraft.client.lightshow.event.events.ValueEvent;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.RandomSource;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.function.Function;

public class RingLightHandler extends ActionLightGroupV2 {

    public record LightDelta(
        int startId, int endId, int idStep, float deltaZ
    ) {}

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

    @FunctionalInterface
    public interface LightFactory {
        LightObject invoke(Vector3f pos);
    }

    public record RingLightData(
        LightMesh mesh,
        LightFactory factory,
        Function<HashMap<Integer, LightObject>, LightObject> linker,
        LightDelta delta,
        PresetPositions presets,
        Vector3f startPosition,
        int count,
        float startAngle,
        float startOffset
    ) {}

    public class IndividualRingLightHandler extends LightObject {

        private final IndividualRingLightHandler.RingHandler headRing;

        private final float ringGap;

        private float startZoom = 1.0f;
        private float targetZoom = 1.0f;
        private float startZoomTime = 0;
        private float endZoomTime = 0;

        public float zoom = 1.0f;

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
            var minFx = org.joml.Math.min(startFx, endFx);
            var maxFx = org.joml.Math.max(startFx, endFx);
            return org.joml.Math.clamp(minFx, maxFx, Math.lerp(startFx, endFx, f));
        }


        protected class RingHandler {

            private IndividualRingLightHandler.RingHandler nextRing = null;
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
                        .rotate(IndividualRingLightHandler.this.rotation)
                        .add(position)
                        .add(IndividualRingLightHandler.this.offset)
                    ;
                    rot.rotationZ(rotation)
                        .mul(orientation)
                        .mul(IndividualRingLightHandler.this.rotation);

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


        public IndividualRingLightHandler(
            BeatmapController map,
            Function<HashMap<Integer, LightObject>, LightObject> linker,
            int count, Vector3f position, float ringGap,
            PresetPositions presets
        ) {
            super(map);
            this.position = position;
            this.ringGap = ringGap;
            this.presets = presets;

            headRing = new IndividualRingLightHandler.RingHandler(0, linker.apply(lights));

            var last = headRing;

            for (int i = 1; i < count; ++i) {
                var current = new IndividualRingLightHandler.RingHandler(i, linker.apply(lights));
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
            return headRing.targetRotation;
        }

        public void spinTo(float angle, float offset, float propagationTime, float spinTime) {
            var t = mapController.currentSeconds;
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


    private final IndividualRingLightHandler innerRing;
    private final IndividualRingLightHandler outerRing;

    private final RandomSource random = RandomSource.create();

    protected static HashMap<Integer, LightObject> buildRingLights(
        RingLightData innerData,
        RingLightData outerData
    ) {
        var map = new HashMap<Integer, LightObject>();

        var pos = new Vector3f(0, 0, 8);

        if (innerData.mesh != null) {
            RingLight.clearInstances(innerData.mesh);
        }
        if (outerData.mesh != null) {
            RingLight.clearInstances(outerData.mesh);
        }
        for (int i = innerData.delta.startId; i < innerData.delta.endId; i += innerData.delta.idStep) {
            try {
                map.put(i, innerData.factory.invoke(new Vector3f(pos)));
                pos.add(0, 0, innerData.delta.deltaZ);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pos.set(0, 0, 8);
        for (int i = outerData.delta.startId; i < outerData.delta.endId; i += outerData.delta.idStep) {
            map.put(i, outerData.factory.invoke(new Vector3f(pos)));
            pos.add(0, 0, outerData.delta.deltaZ);
        }

        return map;
    }

    public RingLightHandler(
        BeatmapController map,
        RingLightData innerData,
        RingLightData outerData
    ) {
        super(map, buildRingLights(innerData, outerData));

        innerRing = new IndividualRingLightHandler(
            map, innerData.linker, innerData.count,
            innerData.startPosition, innerData.delta.deltaZ,
            innerData.presets
        );

        outerRing = new IndividualRingLightHandler(
            map, outerData.linker, outerData.count,
            outerData.startPosition, outerData.delta.deltaZ,
            outerData.presets
        );

        innerRing.spinTo(innerData.startAngle, innerData.startOffset, 0, 0);
        outerRing.spinTo(outerData.startAngle, outerData.startOffset, 0, 0);

    }

    public void reset() {
        innerRing.reset();
        outerRing.reset();
    }

    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        switch (eventGroup)
        {
            case RING_SPIN -> handleRingSpin((RingRotationEvent) event);
            case RING_ZOOM -> handleRingZoom((RingZoomEvent) event);
        }
    }

    private void handleRingSpin(RingRotationEvent event) {
        switch (event.target) {
            case Both -> {
                event.apply(innerRing, random);
                event.apply(outerRing, random);
            }
            case Inner -> event.apply(innerRing, random);
            case Outer -> event.apply(outerRing, random);
        }
    }

    private void handleRingZoom(RingZoomEvent event) {
        var step = event.step;
        if (step == null) {
            step = innerRing.zoom >= 0.5f ? 0.3f : 1f;
        }
        innerRing.setZoom(step, event.speed);
        outerRing.setZoom(step, event.speed);
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
