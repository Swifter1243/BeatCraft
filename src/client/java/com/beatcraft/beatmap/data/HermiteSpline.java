package com.beatcraft.beatmap.data;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class HermiteSpline {

    private Vector3f startPoint;
    private Vector3f endPoint;
    private Vector3f baseTangent;

    public HermiteSpline(Vector3f startPoint, Vector3f endPoint, Vector3f tangent) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.baseTangent = tangent;
    }

    public Vector3f evaluate(float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        float h1 = 2 * t3 - 3 * t2 + 1;
        float h2 = -2 * t3 + 3 * t2;
        float h3 = t3 - 2 * t2 + t;

        Vector3f result = new Vector3f();
        result.add(new Vector3f(startPoint).mul(h1));
        result.add(new Vector3f(endPoint).mul(h2));
        result.add(new Vector3f(baseTangent).mul(h3));
        return result;
    }

    public Vector3f getTangent(float t) {
        return evaluate(t-0.01f).sub(evaluate(t));
        //float t2 = t * t;
        //
        //float dh1 = 6 * t2 - 6 * t;
        //float dh2 = -6 * t2 + 6 * t;
        //float dh3 = 3 * t2 - 4 * t + 1;
        //
        //Vector3f result = new Vector3f();
        //result.add(new Vector3f(startPoint).mul(dh1));
        //result.add(new Vector3f(endPoint).mul(dh2));
        //result.add(new Vector3f(baseTangent).mul(dh3));
        //
        //return result;
    }

    public void debugRender(int color) {
        debugRender(color, 0.02f);
    }

    public void debugRender(int color, float stepFactor) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        Vector3f start = this.startPoint;
        for (float i = stepFactor; i <= 1; i += stepFactor) {
            Vector3f current = evaluate(i);

            var normal = current.sub(start, new Vector3f());

            buffer.vertex((float) (start.x - cam.x), (float) (start.y - cam.y), (float) (start.z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);
            buffer.vertex((float) (current.x - cam.x), (float) (current.y - cam.y), (float) (current.z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);

            start = current;

        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);


        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }

}
