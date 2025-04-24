package com.beatcraft.lightshow.environment.lightgroup;

import com.beatcraft.BeatCraft;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.lights.TransformState;
import com.beatcraft.render.BeatCraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.HashMap;

public abstract class LightGroupV3 extends LightGroup {

    public final HashMap<Integer, LightObject> lights;

    public LightGroupV3(HashMap<Integer, LightObject> lights) {
        this.lights = lights;
    }

    public int getLightCount() {
        return lights.size();
    }

    public void setLightState(int id, LightState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setLightState(state);
        }
        else {
            BeatCraft.LOGGER.error("LightGroupV3 setLightState: No LightObject with id {} found", id);
        }
    }

    public void setTransform(int id, TransformState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setTransformState(state);
        }
        else {
            BeatCraft.LOGGER.error("LightGroupV setTransform3: No LightObject with id {} found", id);
        }
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        lights.forEach((key, light) -> {
            light.render(matrices, camera, BeatCraftRenderer.bloomfog);
        });
    }
}
