package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.common.data.types.Color;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public abstract class Environment {

    public BeatmapPlayer mapController;

    public static Color DEFAULT_FOG_COLOR = new Color(0.18823f, 0.5960f, 1);

    public abstract float getVersion();

    public Environment(BeatmapPlayer map) {
        mapController = map;
        setup();
    }

    public String getID() {
        return "Default";
    }

    public void render(PoseStack matrices, Camera camera, float alpha) {

    }

    public void seek(float beat) {

    }

    public void update(float beat, double deltaTime) {

    }

    public abstract void setup();

    public abstract Environment reset();

    public Color getFogColor() {
        return DEFAULT_FOG_COLOR;
    }

    public abstract void loadLightshow(Difficulty difficulty, JsonObject json);

    private static final float[] DEFAULT_FOG_HEIGHTS = new float[]{-50, -30};
    public float[] getFogHeights() {
        return DEFAULT_FOG_HEIGHTS;
    }

}
