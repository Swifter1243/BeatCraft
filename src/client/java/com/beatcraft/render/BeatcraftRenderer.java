package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class BeatcraftRenderer {

    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();

    public static void onRender(MatrixStack matrices, Camera camera) {
        BeatmapPlayer.onRender(matrices, camera);
    }

    public static void recordRenderCall(Runnable call) {
        renderCalls.add(call);
    }

    public static void render() {
        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }
        renderCalls.clear();
    }
}
