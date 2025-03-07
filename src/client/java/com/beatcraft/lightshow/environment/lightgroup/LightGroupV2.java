package com.beatcraft.lightshow.environment.lightgroup;


import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

// V2 has no step/offset pattern stuff, just pre-set motion algorithms (that I don't know)
//
public abstract class LightGroupV2 extends LightGroup {

    protected ArrayList<LightObject> lights = new ArrayList<>();

    public abstract void handleEvent(EventGroup group, Object obj);

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        lights.forEach(light -> {
            light.render(matrices, camera, BeatcraftRenderer.bloomfog);
        });
    }
}
