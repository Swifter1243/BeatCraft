package com.beatcraft.render.effect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class LaserRenderer {

    private static final ArrayList<Runnable> render_calls = new ArrayList<>();

    public void renderLaser(Vector3f position, Quaternionf rotation, int color, int brightness, float fadeLength) {

        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();



    }

}
