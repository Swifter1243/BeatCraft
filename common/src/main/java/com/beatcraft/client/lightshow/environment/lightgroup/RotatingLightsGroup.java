package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.event.events.SpinningLightEvent;
import com.beatcraft.client.lightshow.event.events.ValueEvent;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;

public class RotatingLightsGroup extends ActionLightGroupV2 {
    RandomSource random = RandomSource.create();

    private final List<Quaternionf> rotations;
    private final List<LightObject> rotatingLights;
    private final List<LightObject> staticLights;
    private final Vector3f spinAxis;

    public RotatingLightsGroup(BeatmapController map, HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights) {
        super(map, collectLights(rotatingLights, staticLights));
        this.rotatingLights = rotatingLights.values().stream().toList();
        this.staticLights = staticLights.values().stream().toList();
        this.rotations = rotatingLights.values().stream().map(o -> new Quaternionf()).toList();
        this.spinAxis = new Vector3f(0, 1, 0);
    }

    public RotatingLightsGroup(BeatmapController map, HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights, Vector3f spinAxis) {
        super(map, collectLights(rotatingLights, staticLights));
        this.rotatingLights = rotatingLights.values().stream().toList();
        this.staticLights = staticLights.values().stream().toList();
        this.rotations = rotatingLights.values().stream().map(o -> new Quaternionf()).toList();
        this.spinAxis = spinAxis;
    }

    private static HashMap<Integer, LightObject> collectLights(HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights) {
        var lights = new HashMap<Integer, LightObject>();
        lights.putAll(rotatingLights);
        lights.putAll(staticLights);
        return lights;
    }

    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        var spinEvent = (SpinningLightEvent) event;
        var speed = spinEvent.direction.apply(spinEvent.speed, random);
        rotations.forEach(rot -> {
            rot.rotationAxis(speed, spinAxis);
        });

        if (!spinEvent.lockRotation) {
            int v = event.getValue();
            rotatingLights.forEach(light -> {
                light.setRotation(
                    v == 0
                        ? new Quaternionf()
                        : new Quaternionf()
                        .rotationAxis(random.nextIntBetweenInclusive(-180, 180) * Mth.DEG_TO_RAD, spinAxis)
                );
            });
        }
    }

    @Override
    public void update(float beat, double deltaTime) {
        for (int i = 0; i < rotatingLights.size(); i++) {
            LightObject light = rotatingLights.get(i);
            Quaternionf rotation = rotations.get(i);
            light.addRotation(new Quaternionf().slerp(rotation, (float) deltaTime));
        }
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        super.render(matrices, camera, alpha);
        staticLights.forEach(light -> light.render(matrices, camera, alpha, BeatcraftRenderer.bloomfog));
    }
}
