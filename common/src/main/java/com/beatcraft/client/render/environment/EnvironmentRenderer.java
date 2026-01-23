package com.beatcraft.client.render.environment;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public interface EnvironmentRenderer {

    default void renderEnv(PoseStack matrices, Camera camera, BeatmapController map, float alpha) {
        if (BeatcraftClient.playerConfig.quality.renderEnvironment()) {
            render(matrices, camera, map, alpha);
        }
    }

    void render(PoseStack matrices, Camera camera, BeatmapController map, float alpha);

}
