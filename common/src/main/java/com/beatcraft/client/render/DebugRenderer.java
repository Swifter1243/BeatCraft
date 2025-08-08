package com.beatcraft.client.render;

import com.beatcraft.Beatcraft;
import com.beatcraft.common.data.types.ISplinePath;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.dynamic_loader.DynamicTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DebugRenderer {

    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();

    public static final ParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(1, 0, 0), 0.5f);
    public static final ParticleOptions ORANGE_DUST = new DustParticleOptions(new Vector3f(1, 0.5f, 0), 0.5f);
    public static final ParticleOptions YELLOW_DUST = new DustParticleOptions(new Vector3f(1, 1, 0), 0.5f);
    public static final ParticleOptions GREEN_DUST = new DustParticleOptions(new Vector3f(0, 1, 0), 0.5f);
    public static final ParticleOptions BLUE_DUST = new DustParticleOptions(new Vector3f(0, 0, 1), 0.5f);
    public static final ParticleOptions MAGENTA_DUST = new DustParticleOptions(new Vector3f(1, 0, 1), 0.5f);

    public static DynamicTexture dynamicTexture;

    public static boolean doDebugRendering = false;
    public static boolean debugSaberRendering = false;
    public static boolean renderHitboxes = false;
    public static boolean renderArcDebugLines = false;

    public static void renderLine(Vector3f origin, Vector3f end, int colorA, int colorB) {
        renderCalls.add(() -> _renderLine(origin, end, colorA, colorB));
    }

    public static void renderParticle(Vector3f point, ParticleOptions particle) {

        Minecraft.getInstance().level.addParticle(
            particle,
            point.x, point.y, point.z, 0, 0, 0
        );
    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest) {
        renderCalls.add(() -> _renderHitbox(hitbox, position, orientation, color, doDepthTest, 2));
    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest, int lineWidth) {
        renderCalls.add(() -> _renderHitbox(hitbox, position, orientation, color, doDepthTest, lineWidth));
    }

    public static void renderPath(ISplinePath path, Vector3f offset, int segments, int color) {
        renderCalls.add(() -> _renderPath(path, offset, segments, color));
    }

    public static void _renderLine(Vector3f origin, Vector3f endPoint, int colorA, int colorB) {

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vector3f cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        Vector3f normal = endPoint.sub(origin, new Vector3f());

        origin = origin.sub(cam, new Vector3f());
        endPoint = endPoint.sub(cam, new Vector3f());

        buffer.addVertex(origin.x, origin.y, origin.z).setColor(colorA).setNormal(normal.x, normal.y, normal.z);
        buffer.addVertex(endPoint.x, endPoint.y, endPoint.z).setColor(colorB).setNormal(normal.x, normal.y, normal.z);

        var buff = buffer.build();
        if (buff == null) return;

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferUploader.drawWithShader(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }

    public static void _renderPath(ISplinePath path, Vector3f offset, int segments, int color) {

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vector3f cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();


        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(offset).sub(cam);
            Vector3f p2 = path.evaluate(f2).add(offset).sub(cam);

            //renderParticle(p.add(cam, new Vector3f()), ParticleTypes.BUBBLE);

            Vector3f normal = p2.sub(p, new Vector3f()).normalize();

            buffer.addVertex(p.x, p.y, p.z).setColor(color).setNormal(normal.x, normal.y, normal.z);
            buffer.addVertex(p2.x, p2.y, p2.z).setColor(color).setNormal(normal.x, normal.y, normal.z);

        }

        List<Vector3f> controlPoints = path.getControlPoints();

        if (controlPoints.size() >= 2) {
            for (int i = 0; i < controlPoints.size()-1; i++) {
                Vector3f p = controlPoints.get(i).add(offset, new Vector3f()).sub(cam);
                Vector3f p2 = controlPoints.get(i+1).add(offset, new Vector3f()).sub(cam);

                Vector3f normal = p2.sub(p, new Vector3f()).normalize();

                buffer.addVertex(p.x, p.y, p.z).setColor(0xFF0000).setNormal(normal.x, normal.y, normal.z);
                buffer.addVertex(p2.x, p2.y, p2.z).setColor(0xFF0000).setNormal(normal.x, normal.y, normal.z);

            }
        }

        var buff = buffer.build();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferUploader.drawWithShader(buff);

        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();

    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color) {
        renderHitbox(hitbox, position, orientation, color, false);
    }



    private static void _renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest, int lineWidth) {

        var edges = BeatcraftRenderer.getCubeEdges(hitbox.min, hitbox.max);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.depthMask(doDepthTest);
        if (doDepthTest) RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(lineWidth);

        for (Vector3f[] edge : edges) {
            var c1 = edge[0].rotate(orientation, new Vector3f());
            var c2 = edge[1].rotate(orientation, new Vector3f());
            c1.add(position);
            c2.add(position);

            var normal = c2.sub(c1, new Vector3f()).normalize();

            for (Vector3f[] segments : RenderUtil.chopEdge(c1, c2)) {
                buffer.addVertex((float) (segments[0].x - cam.x), (float) (segments[0].y - cam.y), (float) (segments[0].z - cam.z)).setColor(color).setNormal(normal.x, normal.y, normal.z);
                buffer.addVertex((float) (segments[1].x - cam.x), (float) (segments[1].y - cam.y), (float) (segments[1].z - cam.z)).setColor(color).setNormal(normal.x, normal.y, normal.z);
            }


        }

        var buff = buffer.build();
        if (buff == null) return;

        BufferUploader.drawWithShader(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }

    public static void render() {

        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                Beatcraft.LOGGER.error("Render call failed! ", e);
            }
        }

        renderCalls.clear();

    }

}
