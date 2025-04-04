package com.beatcraft.render.effect;

import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import net.minecraft.client.render.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ObstacleGlowRenderer {

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

    public static void render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        BeatCraftRenderer.recordLaserRenderCall((buffer, camera) -> _render(position, orientation, bounds, color, buffer, camera, false));
    }

    public static void renderMirrored(Vector3f position, Quaternionf orientation, Hitbox bounds, int color) {
        Vector3f flippedPos = position.mul(1, -1, 1);
        Quaternionf flippedOrientation = MemoryPool.newQuaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);
        MemoryPool.release(orientation);
        MirrorHandler.recordMirrorLaserRenderCall((buffer, camera) -> _render(flippedPos, flippedOrientation, bounds, color, buffer, camera, true));
    }

    public static void _render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color, BufferBuilder buffer, Vector3f cameraPos, boolean mirrored) {
        var edges = BeatCraftRenderer.getCubeEdges(bounds.min, bounds.max);

        var e0 = MemoryPool.newVector3f();
        var e1 = MemoryPool.newVector3f();

        for (Vector3f[] edge : edges) {

            e0.set(edge[0]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(position);
            e1.set(edge[1]).mul(1, mirrored ? -1 : 1, 1).rotate(orientation).add(position);
            
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

            MemoryPool.release(mesh);
        }
        MemoryPool.release(e0, e1);

        MemoryPool.release(position);
        //MemoryPool.release(orientation);

    }

}
