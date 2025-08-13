package com.beatcraft.client.lightshow.environment.kaleidoscope;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.effect.Bloomfog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DistantLight extends LightObject {

    public DistantLight(BeatmapPlayer map, Vector3f pos, Quaternionf ori) {
        super(map);
        position = pos;
        orientation = ori;
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new DistantLight(mapController, position.add(offset, new Vector3f()), new Quaternionf(orientation));
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {

    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
