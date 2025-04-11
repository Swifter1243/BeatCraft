package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public abstract class EnvironmentV4 extends Environment {

    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        loadV4(difficulty, json);
    }

    private void loadV4(Difficulty difficulty, JsonObject json) {

    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
    }
}
