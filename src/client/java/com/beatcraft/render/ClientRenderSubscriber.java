package com.beatcraft.render;

import com.beatcraft.beatmap.BeatmapPlayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class ClientRenderSubscriber {
    static final PhysicalColorNote colorNote = new PhysicalColorNote();

    public static void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        BeatmapPlayer.onFrame();
        colorNote.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
    }
}
