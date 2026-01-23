package com.beatcraft.client.render.effect;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.common.memory.MemoryPool;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

public class ObstacleGlowRenderer {

    public static ShaderInstance distortionShader;
    public static ShaderInstance blitShader;
    public static TextureTarget framebuffer = new TextureTarget(1920, 1080, true, true);

    public static void init() {
        try {
            distortionShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "col_distortion", DefaultVertexFormat.POSITION_TEX_COLOR);
            blitShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "blit_screen", DefaultVertexFormat.POSITION_TEX);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void grabScreen() {
        var scene = Minecraft.getInstance().getMainRenderTarget();

        scene.unbindWrite();
        scene.bindRead();
        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.bindWrite(true);
        framebuffer.clear(true);

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = framebuffer;

        ObstacleGlowRenderer.distortionShader.setSampler("DiffuseSampler", scene.getColorTextureId());
        RenderSystem.setShaderTexture(0, scene.getColorTextureId());

        RenderSystem.setShader(() -> blitShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.addVertex(-1, -1, 0).setUv(0, 0);
        buffer.addVertex(-1, 1, 0).setUv(0, 1);
        buffer.addVertex(1, 1, 0).setUv(1, 1);
        buffer.addVertex(1, -1, 0).setUv(1, 0);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        scene.unbindRead();
        framebuffer.unbindWrite();
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    private static Vector3f[] buildEdge(Vector3f pos1, Vector3f pos2, Vector3f cameraPos) {
        Vector3f lineNormal = MemoryPool.newVector3f(pos1).sub(pos2).normalize();

        Vector3f p1normal = MemoryPool.newVector3f(pos1).sub(cameraPos).normalize();
        Vector3f p2normal = MemoryPool.newVector3f(pos2).sub(cameraPos).normalize();

        Vector3f p1offset = MemoryPool.newVector3f(lineNormal).cross(p1normal).normalize().mul(0.05f);
        Vector3f p2offset = lineNormal.cross(p2normal).normalize().mul(0.05f); // lineNormal ownership transfer

        var out = new Vector3f[]{
            MemoryPool.newVector3f(pos1).add(p1offset),
            MemoryPool.newVector3f(pos1).sub(p1offset),
            MemoryPool.newVector3f(pos2).sub(p2offset),
            MemoryPool.newVector3f(pos2).add(p2offset)
        };

        MemoryPool.release(p1normal, p2normal, p1offset, p2offset);

        return out;
    }

    public static void render(BeatmapController map, Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        if (distortionShader == null) init();
        map.recordLaserRenderCall((buffer, camera) -> _render(position, orientation, bounds, color, buffer, camera, false));
        var p = MemoryPool.newVector3f(position);
        var o = MemoryPool.newQuaternionf(orientation);
        map.recordLaserPreRenderCall((buffer, camera) -> _render(p, o, bounds, color, buffer, camera, false));

    }

    public static void renderMirrored(BeatmapController map, Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        Vector3f flippedPos = position.mul(1, -1, 1);
        Quaternionf flippedOrientation = MemoryPool.newQuaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);
        MemoryPool.release(orientation);
        // map.recordMirrorLaserRenderCall((buffer, camera) -> _render(flippedPos, flippedOrientation, bounds, color, buffer, camera, true));
    }

    public static void _render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color, BufferBuilder buffer, Vector3f cameraPos, boolean mirrored) {
        var edges = BeatcraftRenderer.getCubeEdges(bounds.min, bounds.max);

        var e0 = MemoryPool.newVector3f();
        var e1 = MemoryPool.newVector3f();

        for (Vector3f[] edge : edges) {

            e0.set(edge[0]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(position);
            e1.set(edge[1]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(position);

            var mesh = buildEdge(e0, e1, cameraPos);

            int fadeColor = (0x00FFFFFF & color) | 0x01000000;

            buffer.addVertex(e0.x - cameraPos.x, e0.y - cameraPos.y, e0.z - cameraPos.z).setColor(0xFFFFFFFF);
            buffer.addVertex(e1.x - cameraPos.x, e1.y - cameraPos.y, e1.z - cameraPos.z).setColor(0xFFFFFFFF);
            buffer.addVertex(mesh[3].x - cameraPos.x, mesh[3].y - cameraPos.y, mesh[3].z - cameraPos.z).setColor(fadeColor);
            buffer.addVertex(mesh[0].x - cameraPos.x, mesh[0].y - cameraPos.y, mesh[0].z - cameraPos.z).setColor(fadeColor);

            buffer.addVertex(e0.x - cameraPos.x, e0.y - cameraPos.y, e0.z - cameraPos.z).setColor(0xFFFFFFFF);
            buffer.addVertex(e1.x - cameraPos.x, e1.y - cameraPos.y, e1.z - cameraPos.z).setColor(0xFFFFFFFF);
            buffer.addVertex(mesh[2].x - cameraPos.x, mesh[2].y - cameraPos.y, mesh[2].z - cameraPos.z).setColor(fadeColor);
            buffer.addVertex(mesh[1].x - cameraPos.x, mesh[1].y - cameraPos.y, mesh[1].z - cameraPos.z).setColor(fadeColor);

            MemoryPool.release(mesh);
        }
        MemoryPool.release(e0, e1);

        MemoryPool.release(position);
        //MemoryPool.release(orientation);

    }

}