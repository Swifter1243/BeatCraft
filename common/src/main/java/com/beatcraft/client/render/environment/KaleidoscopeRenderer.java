package com.beatcraft.client.render.environment;

import com.beatcraft.client.beatmap.BeatmapController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public class KaleidoscopeRenderer implements EnvironmentRenderer {

    @Override
    public void render(PoseStack matrices, Camera camera, BeatmapController map, float alpha) {
        var matrices2 = new PoseStack();
        matrices2.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        matrices2.mulPose(matrices.last().pose());
        map.mirrorHandler.recordCall((buffer, cameraPos, invCameraRotation) -> {
            this.renderMesh(buffer, matrices2, alpha, mirrorMesh);
        });
        map.mirrorHandler.recordPlainCall((buffer, cameraPos) -> {
            this.renderMesh(buffer, matrices2, alpha, blocksMesh);
        });
    }


    private static final float RUNWAY_START = 8;
    private static final float RUNWAY_END = 300;
    private static final float RUNWAY_TOP = 0;
    private static final float RUNWAY_BOTTOM = -0.25f;

    private static final float LANE_WIDTH = 0.8f;
    private static final float MID_LANE_WIDTH = 1.8f;

    private static final float ROW0 = 1.1f;
    private static final float ROW2 = -0.9f;
    private static final float ROW3 = -1.9f;


    private static final float[][][] mirrorMesh = new float[][][]{
        { // Primary runway
            {ROW0, RUNWAY_TOP, RUNWAY_START},
            {ROW0, RUNWAY_TOP, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW2, RUNWAY_TOP, RUNWAY_START},
            {ROW2, RUNWAY_TOP, RUNWAY_END},
            {ROW2+MID_LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW2+MID_LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW3, RUNWAY_TOP, RUNWAY_START},
            {ROW3, RUNWAY_TOP, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

        },
    };

    private static final float[][][] blocksMesh = new float[][][]{
        { // Primary runway
            // Bottom faces
            {ROW0, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW0, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW2, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW2, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW2+MID_LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW2+MID_LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW3, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW3, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            // Left faces
            {ROW0, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW0, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0, RUNWAY_TOP,    RUNWAY_END},
            {ROW0, RUNWAY_TOP,    RUNWAY_START},

            {ROW2, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW2, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW2, RUNWAY_TOP,    RUNWAY_END},
            {ROW2, RUNWAY_TOP,    RUNWAY_START},

            {ROW3, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW3, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW3, RUNWAY_TOP,    RUNWAY_END},
            {ROW3, RUNWAY_TOP,    RUNWAY_START},

            // Right faces
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW0+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW0+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {ROW2+MID_LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW2+MID_LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW2+MID_LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW2+MID_LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW3+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW3+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
        },
    };

}
