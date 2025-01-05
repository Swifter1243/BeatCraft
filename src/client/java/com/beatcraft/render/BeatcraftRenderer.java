package com.beatcraft.render;

import com.beatcraft.BeatmapPlayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class BeatcraftRenderer {

    public static void onRender(MatrixStack matrices, Camera camera) {
        BeatmapPlayer.onRender(matrices, camera);
    }
}
