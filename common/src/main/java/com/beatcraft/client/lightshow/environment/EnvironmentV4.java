package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public abstract class EnvironmentV4 extends EnvironmentV3 {

    public EnvironmentV4(BeatmapPlayer map) {
        super(map);
    }

    @Override
    public float getVersion() {
        return 4;
    }

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
    public void render(PoseStack matrices, Camera camera, float alpha) {
        super.render(matrices, camera, alpha);
    }
}
