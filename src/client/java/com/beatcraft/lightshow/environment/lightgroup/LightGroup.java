package com.beatcraft.lightshow.environment.lightgroup;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public abstract class LightGroup {

    public abstract void update(float beat, double deltaTime);

    public abstract void render(MatrixStack matrices, Camera camera);

}
