package com.beatcraft.lightshow.environment.thefirst;

import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.function.BiFunction;

public class InnerRing extends LightObject {


    private InnerRing() {
        orientation = new Quaternionf().rotationZ(45 * MathHelper.RADIANS_PER_DEGREE);
    }

    private static InnerRing INSTANCE;
    public static InnerRing getInstance(BiFunction<Vector3f, Quaternionf, LightObject> ignored) {
        if (INSTANCE == null) {
            INSTANCE = new InnerRing();
        }
        return INSTANCE;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

        var pos = new Vector3f(position);
        var off = new Vector3f(offset);
        var ori = new Quaternionf(orientation);
        var rot = new Quaternionf(rotation);

        BeatcraftRenderer.recordEarlyRenderCall((vcp) ->
            _render(pos, off, ori, rot, bloomfog)
        );
    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot, Vector3f camera) {
        return new Vector3f(base)
            .rotate(ori).add(pos)
            .rotate(rot).add(off)
            .sub(camera);
    }

    private static final float ringRadius = 7;
    private static final float ringWidth = 0.3f;
    private static final float ringDepth = 0.1f;
    private static final float ringGap = 4;
    private static final int color = 0xFF000000;

    private static final Vector3f[] vertices = new Vector3f[]{
        // front vertical face
        new Vector3f(ringRadius, 0, 0),
        new Vector3f(ringRadius+ringWidth, 0, 0),
        new Vector3f(ringRadius+ringWidth, ringRadius+ringWidth, 0),
        new Vector3f(ringRadius, ringRadius, 0),

        // front vertical inner face
        new Vector3f(ringRadius, 0, 0),
        new Vector3f(ringRadius, ringRadius, 0),
        new Vector3f(ringRadius, ringRadius, ringDepth),
        new Vector3f(ringRadius, 0, ringDepth),

        // front horizontal face
        new Vector3f(ringRadius+ringWidth, ringRadius+ringWidth, 0),
        new Vector3f(ringGap/2, ringRadius+ringWidth, 0),
        new Vector3f(ringGap/2, ringRadius, 0),
        new Vector3f(ringRadius, ringRadius, 0),

        // gap end face
        new Vector3f(ringGap/2, ringRadius, 0),
        new Vector3f(ringGap/2, ringRadius+ringWidth, 0),
        new Vector3f(ringGap/2, ringRadius+ringWidth, ringDepth),
        new Vector3f(ringGap/2, ringRadius, ringDepth),

        // front horizontal inner face
        new Vector3f(ringRadius, ringRadius, 0),
        new Vector3f(ringGap/2, ringRadius, 0),
        new Vector3f(ringGap/2, ringRadius, ringDepth),
        new Vector3f(ringRadius, ringRadius, ringDepth)
    };

    private static final Vector3f[] modifiers = new Vector3f[]{
            new Vector3f(1, 1, 1),
            new Vector3f(-1, 1, 1),
            new Vector3f(-1, -1, 1),
            new Vector3f(1, -1, 1)
    };

    private void _render(Vector3f position, Vector3f offset, Quaternionf orientation, Quaternionf rotation, Bloomfog bloomfog) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        // manual mesh building since loading over-sized json model doesn't work >:(

        for (Vector3f mod : modifiers) {
            for (Vector3f vertex : vertices) {
                buffer.vertex(processVertex(vertex.mul(mod, new Vector3f()), position, offset, orientation, rotation, cameraPos)).color(color);
            }
        }



        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
