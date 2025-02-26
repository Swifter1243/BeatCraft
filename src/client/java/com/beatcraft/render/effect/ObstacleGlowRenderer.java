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

    private static final ArrayList<BiConsumer<BufferBuilder, Vector3f>> renderCalls = new ArrayList<>();

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
        renderCalls.add((buffer, camera) -> _render(position, orientation, bounds, color, buffer, camera));
    }

    public static void _render(Vector3f position, Quaternionf orientation, Hitbox bounds, int color, BufferBuilder buffer, Vector3f cameraPos) {
        var edges = BeatcraftRenderer.getCubeEdges(bounds.min.add(position, new Vector3f()).rotate(orientation), bounds.max.add(position, new Vector3f()).rotate(orientation));

        //Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        //Tessellator tessellator = Tessellator.getInstance();
        //BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Vector3f[] edge : edges) {
            var mesh = buildEdge(edge[0], edge[1], cameraPos);

            int fadeColor = (0x00FFFFFF & color) | 0x01000000;

            buffer.vertex(edge[0].x - cameraPos.x, edge[0].y - cameraPos.y, edge[0].z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(edge[1].x - cameraPos.x, edge[1].y - cameraPos.y, edge[1].z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(mesh[3].x - cameraPos.x, mesh[3].y - cameraPos.y, mesh[3].z - cameraPos.z).color(fadeColor);
            buffer.vertex(mesh[0].x - cameraPos.x, mesh[0].y - cameraPos.y, mesh[0].z - cameraPos.z).color(fadeColor);

            buffer.vertex(edge[0].x - cameraPos.x, edge[0].y - cameraPos.y, edge[0].z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(edge[1].x - cameraPos.x, edge[1].y - cameraPos.y, edge[1].z - cameraPos.z).color(0xFFFFFFFF);
            buffer.vertex(mesh[2].x - cameraPos.x, mesh[2].y - cameraPos.y, mesh[2].z - cameraPos.z).color(fadeColor);
            buffer.vertex(mesh[1].x - cameraPos.x, mesh[1].y - cameraPos.y, mesh[1].z - cameraPos.z).color(fadeColor);

        }

    }


    public static void renderAll() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        for (var call : renderCalls) {
            call.accept(buffer, cameraPos);
        }

        renderCalls.clear();

        var buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);

    }

}
