package com.beatcraft.lightshow.environment;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Environment {
    public Environment() {
        setup();
    }

    public String getID() {
        return "Default";
    }

    public void render(MatrixStack matrices, Camera camera) {

    }

    public void seek(float beat) {

    }

    public void update(float beat, double deltaTime) {

    }

    public abstract void setup();

    public abstract Environment reset();

}
