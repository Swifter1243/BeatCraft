package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatCraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;

public class RotatingLightsGroup extends ActionLightGroupV2 {
    Random random = Random.create();

    private final List<Quaternionf> rotations;
    private final List<LightObject> rotatingLights;
    private final List<LightObject> staticLights;

    public RotatingLightsGroup(HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights) {
        super(collectLights(rotatingLights, staticLights));
        this.rotatingLights = rotatingLights.values().stream().toList();
        this.staticLights = staticLights.values().stream().toList();
        this.rotations = rotatingLights.values().stream().map(o -> new Quaternionf()).toList();
    }

    private static HashMap<Integer, LightObject> collectLights(HashMap<Integer, LightObject> rotatingLights, HashMap<Integer, LightObject> staticLights) {
        var lights = new HashMap<Integer, LightObject>();
        lights.putAll(rotatingLights);
        lights.putAll(staticLights);
        return lights;
    }

    public static Quaternionf getYRotation(int v) {
        return new Quaternionf().rotationY((float) v);
    }

    @Override
    public void handleEvent(ValueEvent event, EventGroup eventGroup) {
        int v = event.getValue();
        rotations.forEach(rot -> {
            rot.set(getYRotation(v));
        });
        rotatingLights.forEach(light -> {
            light.setRotation(v == 0 ? new Quaternionf() : new Quaternionf().rotationY(random.nextBetween(-180, 180) * MathHelper.RADIANS_PER_DEGREE));
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
        staticLights.forEach(light -> light.render(matrices, camera, BeatCraftRenderer.bloomfog));
    }
}
