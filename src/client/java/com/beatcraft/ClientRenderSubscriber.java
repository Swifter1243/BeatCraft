package com.beatcraft;

import com.beatcraft.render.TestRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class ClientRenderSubscriber {
    static final TestRenderer testRenderer = new TestRenderer();

    public static void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        testRenderer.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);
    }
}
