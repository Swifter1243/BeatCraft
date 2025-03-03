package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.LightGroupV2;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class RotatingLightsGroup extends LightGroupV2 {
    Random random = Random.create();

    private ArrayList<Quaternionf> rotations = new ArrayList<>();
    public ArrayList<LightObject> staticLights = new ArrayList<>();

    public RotatingLightsGroup(List<LightObject> lightObjects, List<LightObject> staticLights) {
        lights.addAll(lightObjects);
        lights.forEach(l -> {
            rotations.add(new Quaternionf());
        });
        this.staticLights.addAll(staticLights);
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
    public void handleEvent(EventGroup group, Object obj) {
        if (isLightEventGroup(group) && obj instanceof LightState state) {
            lights.forEach(l -> {
                l.setLightState(state);
            });
            staticLights.forEach(l -> {
                l.setLightState(state);
            });
        }
        if (isValueEventGroup(group) && obj instanceof Integer v) {
            if (v == 0) {
                rotations.forEach(Quaternionf::identity);
                lights.forEach(light -> light.setRotation(new Quaternionf()));
            } else if (1 <= v && v <= 9) {
                rotations.forEach(rot -> {
                    rot.set(getYRotation(v, random.nextBoolean()));
                });
                lights.forEach(light -> {
                    light.setRotation(new Quaternionf().rotationY(random.nextBetween(-180, 180) * MathHelper.RADIANS_PER_DEGREE));
                });
            }
        }
    }

    @Override
    public void update(float beat, double deltaTime) {
        for (int i = 0; i < lights.size(); i++) {
            LightObject light = lights.get(i);
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
