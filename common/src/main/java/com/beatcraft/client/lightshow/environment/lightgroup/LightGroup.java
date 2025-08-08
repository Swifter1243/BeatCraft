package com.beatcraft.client.lightshow.environment.lightgroup;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public abstract class LightGroup {

    protected BeatmapPlayer mapController;

    public LightGroup(BeatmapPlayer map) {
        mapController = map;
    }

    public abstract void update(float beat, double deltaTime);

    public abstract void render(PoseStack matrices, Camera camera);

}
