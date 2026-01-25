package com.beatcraft.client.render.environment;

import com.beatcraft.client.beatmap.BeatmapController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public class OriginsRenderer implements EnvironmentRenderer {

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

    private static final float CROSSBEAM_HEIGHT = 0.4f;

    private static final float PILLAR_WIDTH = 2;
    private static final float PILLAR_DEPTH = 3;
    private static final float PILLAR_HEIGHT = 400;
    private static final float PILLAR0 = 2.5f;
    private static final float PILLAR1 = -4.5f;

    private static final float ROW0 = 1.1f;
    private static final float ROW1 = 0.1f;
    private static final float ROW2 = -0.9f;
    private static final float ROW3 = -1.9f;

    private static final float ROW4 = 5.1f;
    private static final float ROW5 = -5.9f;

    private static final float FAR = 1000;

    private static final float[][][] mirrorMesh = new float[][][]{
        { // Primary runway
            {ROW0, RUNWAY_TOP, RUNWAY_START},
            {ROW0, RUNWAY_TOP, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW1, RUNWAY_TOP, RUNWAY_START},
            {ROW1, RUNWAY_TOP, RUNWAY_END},
            {ROW1+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW1+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW2, RUNWAY_TOP, RUNWAY_START},
            {ROW2, RUNWAY_TOP, RUNWAY_END},
            {ROW2+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW2+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW3, RUNWAY_TOP, RUNWAY_START},
            {ROW3, RUNWAY_TOP, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

        },
        { // Side strips
            {ROW4, RUNWAY_TOP, RUNWAY_START},
            {ROW4, RUNWAY_TOP, RUNWAY_END},
            {ROW4+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW4+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

            {ROW5, RUNWAY_TOP, RUNWAY_START},
            {ROW5, RUNWAY_TOP, RUNWAY_END},
            {ROW5+LANE_WIDTH, RUNWAY_TOP, RUNWAY_END},
            {ROW5+LANE_WIDTH, RUNWAY_TOP, RUNWAY_START},

        }
    };

    private static final float[][][] blocksMesh = new float[][][]{
        { // Primary runway
            // Bottom faces
            {ROW0, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW0, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW1, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW1, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW1+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW1+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW2, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW2, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW2+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW2+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW3, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW3, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            // Left faces
            {ROW0, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW0, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW0, RUNWAY_TOP,    RUNWAY_END},
            {ROW0, RUNWAY_TOP,    RUNWAY_START},

            {ROW1, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW1, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW1, RUNWAY_TOP,    RUNWAY_END},
            {ROW1, RUNWAY_TOP,    RUNWAY_START},

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

            {ROW1+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW1+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW1+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW1+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {ROW2+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW2+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW2+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW2+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW3+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW3+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW3+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
        },
        { // Side strips
            // Bottom faces
            {ROW4, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW4, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW4+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW4+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {ROW5, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW5, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW5+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW5+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            // Left faces
            {ROW4, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW4, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW4, RUNWAY_TOP,    RUNWAY_END},
            {ROW4, RUNWAY_TOP,    RUNWAY_START},

            {ROW5, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW5, RUNWAY_BOTTOM, RUNWAY_END},
            {ROW5, RUNWAY_TOP,    RUNWAY_END},
            {ROW5, RUNWAY_TOP,    RUNWAY_START},

            // Right faces
            {ROW4+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW4+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW4+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW4+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {ROW5+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {ROW5+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {ROW5+LANE_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {ROW5+LANE_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

        },
        { // Pillars
            // Pillar 0
            // Top face
            {PILLAR0, RUNWAY_TOP, RUNWAY_START},                   // front-left
            {PILLAR0, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},    // back-left
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH}, // back-right
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},    // front-right

            // Front face (aligned to RUNWAY_START)
            {PILLAR0, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},   // bottom-left
            {PILLAR0, RUNWAY_TOP, RUNWAY_START},                  // top-left
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},   // top-right
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START}, // bottom-right

            // Left face
            {PILLAR0, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},   // bottom-front
            {PILLAR0, RUNWAY_TOP, RUNWAY_START},                  // top-front
            {PILLAR0, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},   // top-back
            {PILLAR0, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START + PILLAR_DEPTH}, // bottom-back

            // Right face
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},   // bottom-front
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},                  // top-front
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},   // top-back
            {PILLAR0 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START + PILLAR_DEPTH}, // bottom-back

            // Pillar 1
            // Top face
            {PILLAR1, RUNWAY_TOP, RUNWAY_START},
            {PILLAR1, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},

            // Front face
            {PILLAR1, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},
            {PILLAR1, RUNWAY_TOP, RUNWAY_START},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},

            // Left face
            {PILLAR1, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},
            {PILLAR1, RUNWAY_TOP, RUNWAY_START},
            {PILLAR1, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},
            {PILLAR1, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START + PILLAR_DEPTH},

            // Right face
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP, RUNWAY_START + PILLAR_DEPTH},
            {PILLAR1 + PILLAR_WIDTH, RUNWAY_TOP - PILLAR_HEIGHT, RUNWAY_START + PILLAR_DEPTH},
        },
        { // crossbeam
            {FAR, RUNWAY_TOP, RUNWAY_START},
            {FAR, RUNWAY_TOP-CROSSBEAM_HEIGHT, RUNWAY_START},
            {-FAR, RUNWAY_TOP-CROSSBEAM_HEIGHT, RUNWAY_START},
            {-FAR, RUNWAY_TOP, RUNWAY_START},

        }
    };

}
