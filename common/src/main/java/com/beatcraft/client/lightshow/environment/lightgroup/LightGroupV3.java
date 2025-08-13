package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.lightshow.lights.TransformState;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

import java.util.HashMap;

public abstract class LightGroupV3 extends LightGroup {

    public final HashMap<Integer, LightObject> lights;

    public LightGroupV3(BeatmapPlayer map, HashMap<Integer, LightObject> lights) {
        super(map);
        this.lights = lights;
    }

    public int getLightCount() {
        return lights.size();
    }

    public void reset() {
        lights.forEach((id, light) -> {
            light.resetState();
        });
    }

    public void setLightState(int id, LightState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setLightState(state);
        }
        else {
            Beatcraft.LOGGER.error("LightGroupV3 setLightState: No LightObject with id {} found", id);
        }
    }

    public void setTransform(int id, TransformState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setTransformState(state);
        }
        else {
            Beatcraft.LOGGER.error("LightGroupV setTransform3: No LightObject with id {} found", id);
        }
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        lights.forEach((key, light) -> {
            light.render(matrices, camera, alpha, BeatcraftRenderer.bloomfog);
        });
    }
}
