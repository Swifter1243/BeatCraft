package com.beatcraft.client.render.environment;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public interface EnvironmentRenderer {

    default void renderEnv(PoseStack matrices, Camera camera, BeatmapController map, float alpha) {
        if (BeatcraftClient.playerConfig.quality.renderEnvironment()) {
            render(matrices, camera, map, alpha);
        }
    }

    void render(PoseStack matrices, Camera camera, BeatmapController map, float alpha);


    Color BLACK = new Color(0, 0, 0, 1);

    default void renderMesh(BufferBuilder buffer, PoseStack matrices, float alpha, float[][][] mesh) {
        renderMesh(buffer, matrices, alpha, mesh, 0, 0, 0, 1, 1, 1);
    }

    default void renderMesh(BufferBuilder buffer, PoseStack matrices, float alpha, float[][][] mesh, float offX, float offY, float offZ, float modX, float modY, float modZ) {
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
