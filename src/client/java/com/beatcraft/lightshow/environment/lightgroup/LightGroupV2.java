package com.beatcraft.lightshow.environment.lightgroup;


import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

// V2 has no step/offset pattern stuff, just pre-set motion algorithms (that I don't know)
//
public abstract class LightGroupV2 extends LightGroup {

    public final HashMap<Integer, LightObject> lights;

    public LightGroupV2(HashMap<Integer, LightObject> lights) {
        this.lights = lights;
    }

    public void setLightState(int id, LightState state) {
        if (lights.containsKey(id)) {
            lights.get(id).setLightState(state);
        }
        else {
            BeatCraft.LOGGER.error("LightGroupV2: No LightObject with id {} found", id);
        }
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        lights.forEach((key, light) -> {
            light.render(matrices, camera, BeatcraftRenderer.bloomfog);
        });
    }
}
