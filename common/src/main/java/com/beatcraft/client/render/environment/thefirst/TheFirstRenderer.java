package com.beatcraft.client.render.environment.thefirst;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.render.environment.EnvironmentRenderer;
import com.beatcraft.common.memory.MemoryPool;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TheFirstRenderer implements EnvironmentRenderer {

    @Override
    public void render(PoseStack matrices, Camera camera, BeatmapController map, float alpha) {
        var matrices2 = new PoseStack();
        matrices2.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
        matrices2.mulPose(matrices.last().pose());
        map.mirrorHandler.recordCall((buffer, cameraPos, invCameraRotation) -> {
            this.renderMirror(buffer, matrices2, cameraPos, invCameraRotation, camera, alpha, map);
        });
        map.mirrorHandler.recordPlainCall((buffer, cameraPos) -> {
            this.renderBlocks(buffer, matrices2, cameraPos, camera, alpha, map);
        });
    }

    private void renderMirror(BufferBuilder buffer, PoseStack matrices, Vector3f cameraPos, Quaternionf invCameraRotation, Camera camera, float alpha, BeatmapController map) {
        var mat4 = matrices.last();

        buffer.addVertex(mat4, new Vector3f(-2, 0, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, 0, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, 0, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, 0, 8)).setColor(0xFF000000);

    }

    private void renderBlocks(BufferBuilder buffer, PoseStack matrices, Vector3f cameraPos, Camera camera, float alpha, BeatmapController map) {
        var mat4 = matrices.last();

        // Runway faces
        buffer.addVertex(mat4, new Vector3f(-2, -1, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -1, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(2, -1, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, 0, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, 0, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(-2, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, 0, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, 0, 800)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -1, 800)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(-2, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, 0, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, 0, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 8)).setColor(0xFF000000);



        // runway pillars
        buffer.addVertex(mat4, new Vector3f(-2, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-1, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-1, -100, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -100, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(-2, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -1, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -100, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-2, -100, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(-1, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-1, -1, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-1, -100, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(-1, -100, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(2, -100, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(1, -100, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(1, -1, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(2, -100, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -100, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(2, -1, 8)).setColor(0xFF000000);

        buffer.addVertex(mat4, new Vector3f(1, -100, 8)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(1, -100, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(1, -1, 9)).setColor(0xFF000000);
        buffer.addVertex(mat4, new Vector3f(1, -1, 8)).setColor(0xFF000000);
    }

}
