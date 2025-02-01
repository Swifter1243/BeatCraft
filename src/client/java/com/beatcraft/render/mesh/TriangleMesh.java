package com.beatcraft.render.mesh;

import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriangleMesh implements Mesh {

    public ArrayList<Vector3f> vertices;
    public ArrayList<Triangle> tris;
    public int color;
    public Identifier texture;

    public TriangleMesh() {
        vertices = new ArrayList<>();
        tris = new ArrayList<>();
    }

    public TriangleMesh(List<Vector3f> vertices, List<Triangle> tris) {
        this.vertices = new ArrayList<>(vertices);
        this.tris = new ArrayList<>(tris);
    }


    @Override
    public BufferBuilder createBuffer() {
        Tessellator tessellator = Tessellator.getInstance();
        return tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    public void drawToBuffer(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        tris.forEach(tri -> {
            tri.draw(buffer, color, this, position, orientation, cameraPos);
        });
    }

    public void addTris(Triangle[] tris) {
        this.tris.addAll(Arrays.asList(tris));
    }

    @Override
    public void render(Vector3f position, Quaternionf orientation, boolean sortBuffer) {
        BufferBuilder buffer = createBuffer();
        drawToBuffer(buffer, position, orientation, MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f());

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        if (sortBuffer) {
            buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
        }

        int oldTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.setShaderTexture(0, oldTexture);

    }
}
