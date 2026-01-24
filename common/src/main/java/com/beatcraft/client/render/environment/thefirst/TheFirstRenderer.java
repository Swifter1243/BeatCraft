package com.beatcraft.client.render.environment.thefirst;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.render.environment.EnvironmentRenderer;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public class TheFirstRenderer implements EnvironmentRenderer {

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

    private static final float RUNWAY_TOP = 0;
    private static final float RUNWAY_START = 8;
    private static final float RUNWAY_WIDTH = 2;
    private static final float RUNWAY_END = 800;
    private static final float RUNWAY_BOTTOM = -0.5f;
    private static final float RUNWAY_PILLAR_WIDTH = 0.5f;
    private static final float RUNWAY_PILLAR_HEIGHT = 400;

    private static final float FAR = 1000;
    private static final float PARALLEL_STRIP_WIDTH = 0.5f;

    private static final float[][][] mirrorMesh = new float[][][]{
        { // Runway surface
            {-RUNWAY_WIDTH, RUNWAY_TOP, RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_TOP, RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_TOP, RUNWAY_START},
            {-RUNWAY_WIDTH, RUNWAY_TOP, RUNWAY_START},
        }
    };

    private static final float[][][] blocksMesh = new float[][][]{
        { // Runway Faces
            {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            { RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            { RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            { RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},

            {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {-RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            {-RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_END},
            {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_END},

            {-RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
            {-RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            { RUNWAY_WIDTH, RUNWAY_TOP,    RUNWAY_START},
            { RUNWAY_WIDTH, RUNWAY_BOTTOM, RUNWAY_START},
        },
        { // Runway pillars
            {-RUNWAY_WIDTH,                      RUNWAY_BOTTOM,        RUNWAY_START},
            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},
            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},
            {-RUNWAY_WIDTH,                     -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},

            {-RUNWAY_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},
            {-RUNWAY_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            {-RUNWAY_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            {-RUNWAY_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},

            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},
            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            {-RUNWAY_WIDTH+RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},

            { RUNWAY_WIDTH,                     -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},
            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},
            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},
            { RUNWAY_WIDTH,                      RUNWAY_BOTTOM,        RUNWAY_START},

            { RUNWAY_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},
            { RUNWAY_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            { RUNWAY_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            { RUNWAY_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},

            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START},
            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START+RUNWAY_PILLAR_WIDTH},
            { RUNWAY_WIDTH-RUNWAY_PILLAR_WIDTH,  RUNWAY_BOTTOM,        RUNWAY_START},
        },
        { // Runway parallel lines
            // Left side
            {14, 1, -FAR}, {14, 1, FAR},  {14, 1+PARALLEL_STRIP_WIDTH, FAR}, {14, 1+PARALLEL_STRIP_WIDTH, -FAR}, // right face
            {14, 1, -FAR}, {14+PARALLEL_STRIP_WIDTH, 1, -FAR}, {14+PARALLEL_STRIP_WIDTH, 1, FAR}, {14, 1, FAR}, // bottom face
            {14, 1+PARALLEL_STRIP_WIDTH, -FAR}, {14+PARALLEL_STRIP_WIDTH, 1+PARALLEL_STRIP_WIDTH, -FAR}, {14+PARALLEL_STRIP_WIDTH, 1+PARALLEL_STRIP_WIDTH, FAR}, {14, 1+PARALLEL_STRIP_WIDTH, FAR}, // top face

            // Right side
            {-14, 1, -FAR}, {-14, 1, FAR},  {-14, 1+PARALLEL_STRIP_WIDTH, FAR}, {-14, 1+PARALLEL_STRIP_WIDTH, -FAR}, // right face
            {-14, 1, -FAR}, {-14-PARALLEL_STRIP_WIDTH, 1, -FAR}, {-14-PARALLEL_STRIP_WIDTH, 1, FAR}, {-14, 1, FAR}, // bottom face
            {-14, 1+PARALLEL_STRIP_WIDTH, -FAR}, {-14-PARALLEL_STRIP_WIDTH, 1+PARALLEL_STRIP_WIDTH, -FAR}, {-14-PARALLEL_STRIP_WIDTH, 1+PARALLEL_STRIP_WIDTH, FAR}, {-14, 1+PARALLEL_STRIP_WIDTH, FAR}, // top face
        },
        { // Runway light supports
            // left side
            //   towers
            {3.4f, 0.1f, RUNWAY_START-0.15f}, {3.4f, 0.1f, RUNWAY_START}, {3.6f, 0.1f, RUNWAY_START}, {3.6f, 0.1f, RUNWAY_START-0.15f}, // top
            {3.4f, 0.1f, RUNWAY_START-0.15f}, {3.4f, 0.1f, RUNWAY_START}, {3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START}, {3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // right
            {3.4f, 0.1f, RUNWAY_START-0.15f}, {3.6f, 0.1f, RUNWAY_START-0.15f}, {3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, {3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // front
            {3.6f, 0.1f, RUNWAY_START-0.15f}, {3.6f, 0.1f, RUNWAY_START}, {3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START}, {3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // left
            //   top bar
            {3.5f, -0.69f, RUNWAY_START}, {3.5f, -0.63f, RUNWAY_START}, {3.5f, -0.63f, RUNWAY_END}, {3.5f, -0.69f, RUNWAY_END},
            //   bottom bar
            {3.5f, -1.39f, RUNWAY_START}, {3.5f, -1.33f, RUNWAY_START}, {3.5f, -1.33f, RUNWAY_END}, {3.5f, -1.39f, RUNWAY_END},
            //   embedded bar
            {3.5f, -0.03f, RUNWAY_START}, {3.5f, 0.03f, RUNWAY_START}, {3.5f, 0.03f, RUNWAY_END}, {3.5f, -0.03f, RUNWAY_END},

            // right side
            //   towers
            {-3.4f, 0.1f, RUNWAY_START-0.15f}, {-3.4f, 0.1f, RUNWAY_START}, {-3.6f, 0.1f, RUNWAY_START}, {-3.6f, 0.1f, RUNWAY_START-0.15f}, // top
            {-3.4f, 0.1f, RUNWAY_START-0.15f}, {-3.4f, 0.1f, RUNWAY_START}, {-3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START}, {-3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // right
            {-3.4f, 0.1f, RUNWAY_START-0.15f}, {-3.6f, 0.1f, RUNWAY_START-0.15f}, {-3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, {-3.4f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // front
            {-3.6f, 0.1f, RUNWAY_START-0.15f}, {-3.6f, 0.1f, RUNWAY_START}, {-3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START}, {-3.6f, -RUNWAY_PILLAR_HEIGHT, RUNWAY_START-0.15f}, // left
            //   top bar
            {-3.5f, -0.69f, RUNWAY_START}, {-3.5f, -0.63f, RUNWAY_START}, {-3.5f, -0.63f, RUNWAY_END}, {-3.5f, -0.69f, RUNWAY_END},
            //   bottom bar
            {-3.5f, -1.39f, RUNWAY_START}, {-3.5f, -1.33f, RUNWAY_START}, {-3.5f, -1.33f, RUNWAY_END}, {-3.5f, -1.39f, RUNWAY_END},
            //   embedded bar
            {-3.5f, -0.03f, RUNWAY_START}, {-3.5f, 0.03f, RUNWAY_START}, {-3.5f, 0.03f, RUNWAY_END}, {-3.5f, -0.03f, RUNWAY_END},

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

    private static final Color BLACK = new Color(0, 0, 0, 1);

    private void renderMesh(BufferBuilder buffer, PoseStack matrices, float alpha, float[][][] mesh) {
        renderMesh(buffer, matrices, alpha, mesh, 0, 0, 0, 1, 1, 1);
    }

    private void renderMesh(BufferBuilder buffer, PoseStack matrices, float alpha, float[][][] mesh, float offX, float offY, float offZ, float modX, float modY, float modZ) {
        var black = BLACK.toARGB(alpha);
        var mat4 = matrices.last();

        var vert = MemoryPool.newVector3f();

        for (var section : mesh) {
            for (var vertex : section) {
                mat4.pose().transformPosition(offX + vertex[0] * modX, offY + vertex[1] * modY, offZ + vertex[2] * modZ, vert);
                buffer.addVertex(vert.x, vert.y, vert.z).setColor(black);
            }
        }

        MemoryPool.release(vert);

    }

}
