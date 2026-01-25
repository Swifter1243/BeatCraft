package com.beatcraft.client.render.environment;

import com.beatcraft.client.beatmap.BeatmapController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public class NiceRenderer implements EnvironmentRenderer {

    @Override
    public void render(PoseStack matrices, Camera camera, BeatmapController map, float alpha) {
        var matrices2 = new PoseStack();
        matrices2.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        matrices2.mulPose(matrices.last().pose());
        map.mirrorHandler.recordPlainCall((buffer, cameraPos) -> {
            this.renderMesh(buffer, matrices2, alpha, blocksMesh);

            this.renderMesh(buffer, matrices2, alpha, towerTopLeft, 10, -4, -15, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, towerTopLeft, 20, 2, 18, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, towerTopLeft, -10, -4, -15, -1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, towerTopLeft, -20, 2, 18, -1, 1, 1);

            this.renderMesh(buffer, matrices2, alpha, slantPillar, 3.5f, 0, 9.5f, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, slantPillar, 3.5f, 0, -2.5f, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, slantPillar, 3.5f, 0, -14.5f, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, slantPillar, -3.5f, 0, 9.5f, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, slantPillar, -3.5f, 0, -2.5f, 1, 1, 1);
            this.renderMesh(buffer, matrices2, alpha, slantPillar, -3.5f, 0, -14.5f, 1, 1, 1);
        });
    }

    private static final float RUNWAY_START = 8;
    private static final float RUNWAY_WIDTH = 3;
    private static final float RUNWAY_BOTTOM = -0.5f;
    private static final float RUNWAY_DEPTH = 0.25f;

    private static final float FAR = 1000;
    private static final float PARALLEL_STRIP_WIDTH = 0.5f;
    private static final float PARALLEL_X = 16;
    private static final float PARALLEL_Y = 1.25f;

    private static final float[][][] blocksMesh = new float[][][]{
        { // Runway Faces
            // runway block
            {RUNWAY_WIDTH, 0, RUNWAY_START}, {RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, 0, RUNWAY_START}, // top
            {RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, {RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, // front
            {RUNWAY_WIDTH, 0, RUNWAY_START}, {RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, {RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START-RUNWAY_DEPTH}, {RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START}, // left
            {-RUNWAY_WIDTH, 0, RUNWAY_START}, {-RUNWAY_WIDTH, 0, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START-RUNWAY_DEPTH}, {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START}, // right

            // crossbeam
            {FAR, 0, RUNWAY_START-(RUNWAY_DEPTH/2)}, {FAR, -RUNWAY_DEPTH, RUNWAY_START-(RUNWAY_DEPTH/2)}, {-FAR, -RUNWAY_DEPTH, RUNWAY_START-(RUNWAY_DEPTH/2)}, {-FAR, 0, RUNWAY_START-(RUNWAY_DEPTH/2)},
        },
        { // Runway parallel lines
            // Left side
            {PARALLEL_X, PARALLEL_Y, -FAR}, {PARALLEL_X, PARALLEL_Y, FAR},  {PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, {PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, // right face
            {PARALLEL_X, PARALLEL_Y, -FAR}, {PARALLEL_X+PARALLEL_STRIP_WIDTH, PARALLEL_Y, -FAR}, {PARALLEL_X+PARALLEL_STRIP_WIDTH, PARALLEL_Y, FAR}, {PARALLEL_X, PARALLEL_Y, FAR}, // bottom face
            {PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, {PARALLEL_X+PARALLEL_STRIP_WIDTH, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, {PARALLEL_X+PARALLEL_STRIP_WIDTH, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, {PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, // top face

            // Right side
            {-PARALLEL_X, PARALLEL_Y, -FAR}, {-PARALLEL_X, PARALLEL_Y, FAR},  {-PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, {-PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, // right face
            {-PARALLEL_X, PARALLEL_Y, -FAR}, {-PARALLEL_X-PARALLEL_STRIP_WIDTH, PARALLEL_Y, -FAR}, {-PARALLEL_X-PARALLEL_STRIP_WIDTH, PARALLEL_Y, FAR}, {-PARALLEL_X, PARALLEL_Y, FAR}, // bottom face
            {-PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, {-PARALLEL_X-PARALLEL_STRIP_WIDTH, PARALLEL_Y+PARALLEL_STRIP_WIDTH, -FAR}, {-PARALLEL_X-PARALLEL_STRIP_WIDTH, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, {-PARALLEL_X, PARALLEL_Y+PARALLEL_STRIP_WIDTH, FAR}, // top face
        }
    };

    private static final float[][][] slantPillar = new float[][][]{
        {
            {-0.5f, -64, -0.5f}, {0.5f, -64, -0.5f}, {0.5f, 108, -61.5f}, {-0.5f, 108, -61.5f}, // back/bottom
            {-0.5f, -64, 0.5f}, {0.5f, -64, 0.5f}, {0.5f, 108, -60.5f}, {-0.5f, 108, -60.5f}, // front/top
            {0.5f, -64, -0.5f}, {0.5f, -64, 0.5f}, {0.5f, 108, -60.5f}, {0.5f, 108, -61.5f}, // left
            {-0.5f, -64, -0.5f}, {-0.5f, -64, 0.5f}, {-0.5f, 108, -60.5f}, {-0.5f, 108, -61.5f}, // right
        }
    };

    private static final float[][][] towerTopLeft = new float[][][]{
        { // base towers
            // Tall section
            {0, -400, 0}, {3, -400, 0}, {3, 11, 0}, {0, 11, 0}, // front face
            {0, -400, 0}, {0, -400, 2}, {0, 11, 2}, {0, 11, 0}, // right face
            {0, -400, 2}, {3, -400, 2}, {3, 11, 2}, {0, 11, 2}, // back face
            // Short section
            {0, -400, -3}, {3, -400, -3}, {3, 7, -3}, {0, 7, -3}, // front face
            {0, -400, -3}, {0, -400, -1}, {0, 7, -1}, {0, 7, -3}, // right face
            {0, -400, -1}, {3, -400, -1}, {3, 7, -1}, {0, 7, -1}, // back face
        },
        { // Spire (17 tall, 16 blocks)
            {2, 11, 1}, {3, 11, 1}, {3, 28, 1}, {2, 28, 1}, // front face
            {2, 11, 1}, {2, 11, 2}, {2, 28, 2}, {2, 28, 1}, // right face
            {2, 11, 2}, {3, 11, 2}, {3, 28, 2}, {2, 28, 2}, // back face
        },
        { // Drop down
            // top block
            {0, 9, 3}, {5, 9, 3}, {5, 11, 3}, {0, 11, 3}, // front face
            {0, 9, 3}, {0, 9, 7}, {0, 11, 7}, {0, 11, 3}, // right face
            {0, 9, 3}, {5, 9, 3}, {5, 9, 7},  {0, 9, 7},  // bottom face
            {0, 9, 7}, {5, 9, 7}, {5, 11, 7}, {0, 11, 7}, // back face

            // drop
            {1, 0, 4}, {1, 9, 4}, {1, 9, 6}, {1, 0, 6}, // right face
            {1, 0, 4}, {2, 0, 4}, {2, 9, 4}, {1, 9, 4}, // front face
            {1, 0, 4}, {2, 0, 4}, {2, 0, 6}, {1, 0, 6}, // bottom face
            {1, 0, 6}, {2, 0, 6}, {2, 9, 6}, {1, 9, 6}, // back face

        },
        { // Crossbeams // 12 to -15
            // Bottom beam
            {1, 7, -15}, {1, 7, 12},  {1, 8, 12},  {1, 8, -15}, // right face
            {1, 7, -15}, {1, 7, 12},  {2, 7, 12},  {2, 7, -15}, // bottom face
            {1, 7, -15}, {2, 7, -15}, {2, 8, -15}, {1, 8, -15}, // front face
            {1, 7, 12},  {2, 7, 12},  {2, 8, 12},  {1, 8, 12}, // back face

            // top beam
            {1, 9, -15}, {1, 9, 12},  {1, 10, 12},  {1, 10, -15}, // right face
            {1, 9, -15}, {1, 9, 12},  {2, 9, 12},   {2, 9, -15}, // bottom face
            {1, 9, -15}, {2, 9, -15}, {2, 10, -15}, {1, 10, -15}, // front face
            {1, 9, 12},  {2, 9, 12},  {2, 10, 12},  {1, 10, 12}, // back face

        },
        { // Angle block
            {2, 4, -10}, {2, 7, -13}, {2, 14, -5}, {2, 11, -2}, // right face
            {2, 4, -10}, {2, 7, -13}, {3, 7, -13}, {3, 4, -10}, // front face
            {2, 7, -13}, {3, 7, -13}, {3, 14, -5}, {2, 14, -5}, // top face
            {2, 4, -10}, {2, 11, -2}, {3, 11, -2}, {3, 4, -10}, // bottom face
            {2, 14, -5}, {3, 14, -5}, {3, 11, -2}, {2, 11, -2}, // back face
        }
    };

}
