package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.BeatmapPlayer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class ClientRenderSubscriber {
    static final PhysicalColorNote colorNote = new PhysicalColorNote(10, 20, 1);

    public static void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        colorNote.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
        BeatmapPlayer.onFrame();
    }
}
