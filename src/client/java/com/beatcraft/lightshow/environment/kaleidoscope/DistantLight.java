package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DistantLight extends LightObject {

    public DistantLight(Vector3f pos, Quaternionf ori) {
        position = pos;
        orientation = ori;
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new DistantLight(position.add(offset, new Vector3f()), new Quaternionf(orientation));
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
