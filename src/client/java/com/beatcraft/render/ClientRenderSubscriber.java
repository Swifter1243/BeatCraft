package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.BeatmapPlayer;
import com.beatcraft.data.ColorNote;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class ClientRenderSubscriber {
    public static ArrayList<WorldRenderer> physicalObjects = new ArrayList<>();

    public static void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        BeatmapPlayer.onFrame();

        for (var obj : physicalObjects) {
            obj.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
        }
    }
}
