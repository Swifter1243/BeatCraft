package com.beatcraft.client.lightshow.environment.lightgroup;


import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

import java.util.HashMap;

// V2 has no step/offset pattern stuff, just pre-set motion algorithms (that I don't know)
//
public abstract class LightGroupV2 extends LightGroup {

    public final HashMap<Integer, LightObject> lights;

    public LightGroupV2(BeatmapPlayer map, HashMap<Integer, LightObject> lights) {
        super(map);
        this.lights = lights;
    }

    public void setLightState(int id, LightState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setLightState(state);
        }
        else {
            Beatcraft.LOGGER.error("LightGroupV2: No LightObject with id {} found", id);
        }
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        lights.forEach((key, light) -> {
            light.render(matrices, camera, alpha, BeatcraftRenderer.bloomfog);
        });
    }
}
