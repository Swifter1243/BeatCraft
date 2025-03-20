package com.beatcraft.render.effect;

import com.beatcraft.logic.Hitbox;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.BeatcraftRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ObstacleGlowRenderer {

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
        BeatcraftRenderer.recordLaserRenderCall((buffer, camera) -> _render(position, orientation, bounds, color, buffer, camera, false));
    }

    public static void renderMirrored(Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        Vector3f flippedPos = position.mul(1, -1, 1, new Vector3f());
        Quaternionf flippedOrientation = new Quaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);
        MirrorHandler.recordMirrorLaserRenderCall((buffer, camera) -> _render(flippedPos, flippedOrientation, bounds, color, buffer, camera, true));
    }

    public static void _render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color, BufferBuilder buffer, Vector3f cameraPos, boolean mirrored) {
        var edges = BeatcraftRenderer.getCubeEdges(bounds.min, bounds.max);

        for (Vector3f[] edge : edges) {

            var e0 = edge[0].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(position);
            var e1 = edge[1].mul(1, mirrored ? -1 : 1, 1, new Vector3f()).rotate(orientation).add(position);
            
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
