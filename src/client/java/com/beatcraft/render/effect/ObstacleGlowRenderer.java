package com.beatcraft.render.effect;

import com.beatcraft.logic.Hitbox;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.BeatcraftRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ObstacleGlowRenderer {

    public static ShaderProgram distortionShader;
    public static ShaderProgram blitShader;
    public static SimpleFramebuffer framebuffer = new SimpleFramebuffer(1920, 1080, true, true);

    public static void init() {
        try {
            distortionShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "col_distortion", VertexFormats.POSITION_TEXTURE_COLOR);
            blitShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "blit_screen", VertexFormats.POSITION_TEXTURE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void grabScreen() {
        var scene = MinecraftClient.getInstance().getFramebuffer();
;
        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(true);
        framebuffer.beginWrite(true);

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = framebuffer;

        ObstacleGlowRenderer.distortionShader.addSampler("DiffuseSampler", scene.getColorAttachment());
        RenderSystem.setShaderTexture(0, scene.getColorAttachment());

        RenderSystem.setShader(() -> blitShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        buffer.vertex(-1, -1, 0).texture(0, 0);
        buffer.vertex(-1, 1, 0).texture(0, 1);
        buffer.vertex(1, 1, 0).texture(1, 1);
        buffer.vertex(1, -1, 0).texture(1, 0);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        framebuffer.endWrite();
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    private static Vector3f[] buildEdge(Vector3f pos1, Vector3f pos2, Vector3f cameraPos) {
        Vector3f lineNormal = pos1.sub(pos2, new Vector3f()).normalize();

        Vector3f p1normal = pos1.sub(cameraPos, new Vector3f()).normalize();
        Vector3f p2normal = pos2.sub(cameraPos, new Vector3f()).normalize();

        Vector3f p1offset = lineNormal.cross(p1normal, new Vector3f()).normalize().mul(0.05f);
        Vector3f p2offset = lineNormal.cross(p2normal, new Vector3f()).normalize().mul(0.05f);

        return new Vector3f[]{
            new Vector3f(pos1).add(p1offset),
            new Vector3f(pos1).sub(p1offset),
            new Vector3f(pos2).sub(p2offset),
            new Vector3f(pos2).add(p2offset)
        };

    }

    public static void render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        if (distortionShader == null) init();
        BeatcraftRenderer.recordLaserRenderCall((buffer, camera) -> _render(position, orientation, bounds, color, buffer, camera));
    }

    public static void _render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color, BufferBuilder buffer, Vector3f cameraPos) {
        var edges = BeatcraftRenderer.getCubeEdges(bounds.min, bounds.max);

        for (Vector3f[] edge : edges) {

            var e0 = edge[0].rotate(orientation, new Vector3f()).add(position);
            var e1 = edge[1].rotate(orientation, new Vector3f()).add(position);
            
            var mesh = buildEdge(e0, e1, cameraPos);

            int fadeColor = (0x00FFFFFF & color) | 0x01000000;

            buffer.vertex(e0.x - cameraPos.x, e0.y - cameraPos.y, e0.z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(e1.x - cameraPos.x, e1.y - cameraPos.y, e1.z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(mesh[3].x - cameraPos.x, mesh[3].y - cameraPos.y, mesh[3].z - cameraPos.z).color(fadeColor);
            buffer.vertex(mesh[0].x - cameraPos.x, mesh[0].y - cameraPos.y, mesh[0].z - cameraPos.z).color(fadeColor);

            buffer.vertex(e0.x - cameraPos.x, e0.y - cameraPos.y, e0.z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(e1.x - cameraPos.x, e1.y - cameraPos.y, e1.z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(mesh[2].x - cameraPos.x, mesh[2].y - cameraPos.y, mesh[2].z - cameraPos.z).color(fadeColor);
            buffer.vertex(mesh[1].x - cameraPos.x, mesh[1].y - cameraPos.y, mesh[1].z - cameraPos.z).color(fadeColor);

        }

    }

}
