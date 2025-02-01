package com.beatcraft.render;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.types.ISplinePath;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.MeshSlicer;
import com.beatcraft.render.mesh.TriangleMesh;
import com.beatcraft.render.object.PhysicalColorNote;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DebugRenderer {

    private static final ArrayList<Runnable> renderCalls = new ArrayList<>();

    public static final ParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1, 0, 0), 0.5f);
    public static final ParticleEffect ORANGE_DUST = new DustParticleEffect(new Vector3f(1, 0.5f, 0), 0.5f);
    public static final ParticleEffect YELLOW_DUST = new DustParticleEffect(new Vector3f(1, 1, 0), 0.5f);
    public static final ParticleEffect GREEN_DUST = new DustParticleEffect(new Vector3f(0, 1, 0), 0.5f);
    public static final ParticleEffect BLUE_DUST = new DustParticleEffect(new Vector3f(0, 0, 1), 0.5f);
    public static final ParticleEffect MAGENTA_DUST = new DustParticleEffect(new Vector3f(1, 0, 1), 0.5f);


    public static boolean doDebugRendering = false;
    public static boolean debugSaberRendering = false;
    public static boolean renderHitboxes = false;
    public static boolean renderArcDebugLines = false;

    public static void renderLine(Vector3f origin, Vector3f end, int colorA, int colorB) {
        renderCalls.add(() -> _renderLine(origin, end, colorA, colorB));
    }

    public static void renderParticle(Vector3f point, ParticleEffect particle) {

        MinecraftClient.getInstance().world.addParticle(
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

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        Vector3f normal = endPoint.sub(origin, new Vector3f());

        origin = origin.sub(cam, new Vector3f());
        endPoint = endPoint.sub(cam, new Vector3f());

        buffer.vertex(origin.x, origin.y, origin.z).color(colorA).normal(normal.x, normal.y, normal.z);
        buffer.vertex(endPoint.x, endPoint.y, endPoint.z).color(colorB).normal(normal.x, normal.y, normal.z);

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }

    public static void _renderPath(ISplinePath path, Vector3f offset, int segments, int color) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vector3f cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();


        for (int i = 0; i < segments; i++) {
            float f = ((float) i) / ((float) segments);
            float f2 = ((float) (i + 1)) / ((float) segments);
            Vector3f p = path.evaluate(f).add(offset).sub(cam);
            Vector3f p2 = path.evaluate(f2).add(offset).sub(cam);

            //renderParticle(p.add(cam, new Vector3f()), ParticleTypes.BUBBLE);

            Vector3f normal = p2.sub(p, new Vector3f()).normalize();

            buffer.vertex(p.x, p.y, p.z).color(color).normal(normal.x, normal.y, normal.z);
            buffer.vertex(p2.x, p2.y, p2.z).color(color).normal(normal.x, normal.y, normal.z);

        }

        List<Vector3f> controlPoints = path.getControlPoints();

        if (controlPoints.size() >= 2) {
            for (int i = 0; i < controlPoints.size()-1; i++) {
                Vector3f p = controlPoints.get(i).add(offset, new Vector3f()).sub(cam);
                Vector3f p2 = controlPoints.get(i+1).add(offset, new Vector3f()).sub(cam);

                Vector3f normal = p2.sub(p, new Vector3f()).normalize();

                buffer.vertex(p.x, p.y, p.z).color(0xFF0000).normal(normal.x, normal.y, normal.z);
                buffer.vertex(p2.x, p2.y, p2.z).color(0xFF0000).normal(normal.x, normal.y, normal.z);

            }
        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(2);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();

    }

    public static void renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color) {
        renderHitbox(hitbox, position, orientation, color, false);
    }

    private static List<Vector3f[]> chopEdge(Vector3f a, Vector3f b) {
        ArrayList<Vector3f[]> segments = new ArrayList<>();

        Vector3f direction = b.sub(a, new Vector3f());
        direction.normalize();
        direction.mul(5);
        Vector3f c = a;
        while (a.distance(b) > 5) {
            c = new Vector3f(a).add(direction);
            segments.add(new Vector3f[]{a, c});
            a = c;
        }
        segments.add(new Vector3f[]{c, b});

        return segments;
    }

    private static void _renderHitbox(Hitbox hitbox, Vector3f position, Quaternionf orientation, int color, boolean doDepthTest, int lineWidth) {

        var edges = BeatcraftRenderer.getCubeEdges(hitbox.min, hitbox.max);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        for (Vector3f[] edge : edges) {
            var c1 = edge[0].rotate(orientation, new Vector3f());
            var c2 = edge[1].rotate(orientation, new Vector3f());
            c1.add(position);
            c2.add(position);

            var normal = c2.sub(c1, new Vector3f()).normalize();

            for (Vector3f[] segments : chopEdge(c1, c2)) {
                buffer.vertex((float) (segments[0].x - cam.x), (float) (segments[0].y - cam.y), (float) (segments[0].z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);
                buffer.vertex((float) (segments[1].x - cam.x), (float) (segments[1].y - cam.y), (float) (segments[1].z - cam.z)).color(color).normal(normal.x, normal.y, normal.z);
            }


        }

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        var oldShader = RenderSystem.getShader();

        RenderSystem.disableCull();
        RenderSystem.depthMask(doDepthTest);
        if (doDepthTest) RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        var oldLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.lineWidth(lineWidth);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(oldLineWidth);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

    }

    public static void renderSimpleQuads(List<Vector3f[]> quads, int color, Vector3f position, Quaternionf orientation) {
        renderCalls.add(() -> _renderSimpleQuads(quads, color, position, orientation));
    }

    private static void _renderSimpleQuads(List<Vector3f[]> quads, int color, Vector3f position, Quaternionf orientation) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        for (Vector3f[] quad : quads) {
            Vector3f a = quad[0].rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
            Vector3f b = quad[1].rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
            Vector3f c = quad[2].rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
            Vector3f d = quad[3].rotate(orientation, new Vector3f()).add(position).sub(cameraPos);

            buffer.vertex(a.x, a.y, a.z).color(color);
            buffer.vertex(b.x, b.y, b.z).color(color);
            buffer.vertex(c.x, c.y, c.z).color(color);
            buffer.vertex(d.x, d.y, d.z).color(color);

        }

        BuiltBuffer buff = buffer.endNullable();

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



    public static void render() {

        //DebugRenderer.renderPath(BeatCraftClient.TEST, new Vector3f(), 50, 0xFF0000);

        //if (MinecraftClient.getInstance().player != null) {
        //    Pair<TriangleMesh, TriangleMesh> slicedMeshes = MeshSlicer.sliceMesh(new Vector3f(), MinecraftClient.getInstance().player.getPos().toVector3f().normalize(), MeshLoader.COLOR_NOTE_MESH);
        //
        //    TriangleMesh left = slicedMeshes.getLeft();
        //    TriangleMesh right = slicedMeshes.getRight();
        //
        //    left.color = 0xFFFF0000;
        //    right.color = 0xFF0000FF;
        //    left.texture = Identifier.of(BeatCraft.MOD_ID, "textures/gameplay_objects/color_note.png");
        //    right.texture = Identifier.of(BeatCraft.MOD_ID, "textures/gameplay_objects/color_note.png");
        //
        //    RenderSystem.enableBlend();
        //    RenderSystem.defaultBlendFunc();
        //
        //    RenderSystem.disableCull();
        //    RenderSystem.enableDepthTest();
        //    left.render(new Vector3f(0, 0, 0), new Quaternionf(), false);
        //    right.render(new Vector3f(0, 0, 0), new Quaternionf(), false);
        //    RenderSystem.enableCull();
        //    RenderSystem.disableBlend();
        //    RenderSystem.depthMask(true);
        //
        //}

        for (Runnable renderCall : renderCalls) {
            try {
                renderCall.run();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Render call failed! ", e);
            }
        }

        renderCalls.clear();

    }

}
