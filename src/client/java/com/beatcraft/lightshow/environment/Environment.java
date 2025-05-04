package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.data.types.Color;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Environment {
    public static Color DEFAULT_FOG_COLOR = new Color(0.18823f, 0.5960f, 1);

    public abstract float getVersion();

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

    public Color getFogColor() {
        return DEFAULT_FOG_COLOR;
    }

    public abstract void loadLightshow(Difficulty difficulty, JsonObject json);
}
