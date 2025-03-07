package com.beatcraft.lightshow.environment.lightgroup;


import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

// V2 has no step/offset pattern stuff, just pre-set motion algorithms (that I don't know)
//
public abstract class LightGroupV2 extends LightGroup {

    protected HashMap<Integer, LightObject> lights = new HashMap<>();

    public abstract void handleEvent(EventGroup group, Object obj);

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        lights.forEach((key, light) -> {
            light.render(matrices, camera, BeatcraftRenderer.bloomfog);
        });
    }
}
