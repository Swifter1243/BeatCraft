package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RotatingLightsGroup extends ActionLightGroupV2 {
    Random random = Random.create();

    private final List<Quaternionf> rotations;
    private final List<LightObject> rotatingLights;
    private final List<LightObject> staticLights;

    public RotatingLightsGroup(HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights) {
        lights.putAll(rotatingLights);
        lights.putAll(staticLights);
        this.rotatingLights = rotatingLights.values().stream().toList();
        this.staticLights = staticLights.values().stream().toList();
        this.rotations = rotatingLights.values().stream().map(o -> new Quaternionf()).toList();
    }

    public boolean isLightEventGroup(EventGroup group) {
        return group == EventGroup.LEFT_LASERS || group == EventGroup.RIGHT_LASERS;
    }

    public boolean isValueEventGroup(EventGroup group) {
        return group == EventGroup.LEFT_ROTATING_LASERS || group == EventGroup.RIGHT_ROTATING_LASERS;
    }

    public static Quaternionf getYRotation(int v, boolean isCC) {
        if (v < 1 || v > 9) throw new IllegalArgumentException("v must be between 1 and 9");

        float baseSpeed = ((float) Math.PI) / (10.0f - v); // Degrees per second (9s for v=1, 1s for v=9)
        if (isCC) baseSpeed = -baseSpeed; // Reverse direction if counterclockwise

        return new Quaternionf().rotationY(baseSpeed);
    }

    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        int v = event.getValue();
        rotations.forEach(rot -> {
            rot.set(getYRotation(v, random.nextBoolean()));
        });
        rotatingLights.forEach(light -> {
            light.setRotation(new Quaternionf().rotationY(random.nextBetween(-180, 180) * MathHelper.RADIANS_PER_DEGREE));
        });
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
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
        staticLights.forEach(light -> light.render(matrices, camera, BeatcraftRenderer.bloomfog));
    }
}
