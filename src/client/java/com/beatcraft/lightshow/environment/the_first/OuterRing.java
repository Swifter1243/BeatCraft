package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.BeatCraft;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.lights.GlowingCuboid;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OuterRing extends LightObject {


    private OuterRing() {
        orientation = new Quaternionf().rotationZ(45 * MathHelper.RADIANS_PER_DEGREE);
    }

    private static OuterRing INSTANCE;
    public static OuterRing getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OuterRing();
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

        light.setWorldRotation(new Quaternionf(orientation).mul(rotation));
        light.setOffset(new Vector3f(position).rotate(rotation).add(offset));
        light.render(matrices, camera, bloomfog);

        light.setWorldRotation(new Quaternionf(orientation).mul(rotation).rotateZ(-90*MathHelper.RADIANS_PER_DEGREE));
        light.setOffset(new Vector3f(position).rotateZ(-90*MathHelper.RADIANS_PER_DEGREE).rotate(orientation).rotate(rotation).add(offset));
        light.render(matrices, camera, bloomfog);

        light.setWorldRotation(new Quaternionf(orientation).mul(rotation).rotateZ(90*MathHelper.RADIANS_PER_DEGREE));
        light.setOffset(new Vector3f(position).rotateZ(-90*MathHelper.RADIANS_PER_DEGREE).rotate(orientation).rotate(rotation).add(offset));
        light.render(matrices, camera, bloomfog);

        light.setWorldRotation(new Quaternionf(orientation).mul(rotation).rotateZ(180*MathHelper.RADIANS_PER_DEGREE));
        light.setOffset(new Vector3f(position).rotateZ(-90*MathHelper.RADIANS_PER_DEGREE).rotate(orientation).rotate(rotation).add(offset));
        light.render(matrices, camera, bloomfog);

    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot, Vector3f camera) {
        return new Vector3f(base)
            .rotate(ori).add(pos)
            .rotate(rot).add(off)
            .sub(camera);
    }

    private static final float ringRadius = 27;
    private static final float ringWidth = 1;
    private static final float ringDepth = 0.5f;
    private static final int color = 0xFF000000;
    private static final float lightLength = 6;
    private static final float lightSize = 0.2f;

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
        new Vector3f(0, ringRadius+ringWidth, 0),
        new Vector3f(0, ringRadius, 0),
        new Vector3f(ringRadius, ringRadius, 0),

        // front horizontal inner face
        new Vector3f(ringRadius, ringRadius, 0),
        new Vector3f(0, ringRadius, 0),
        new Vector3f(0, ringRadius, ringDepth),
        new Vector3f(ringRadius, ringRadius, ringDepth)
    };

    private final GlowingCuboid light = new GlowingCuboid(
        new Hitbox(
            new Vector3f(-lightLength/2, -lightSize, -lightSize),
            new Vector3f(lightLength/2, lightSize, lightSize)
        ),
        new Vector3f(0, ringRadius-(lightSize+0.01f), lightSize),
        new Quaternionf()
    );


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
        RenderSystem.setShader(() -> Bloomfog.bloomfogPositionColor);
        bloomfog.loadTex();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    @Override
    public void setBrightness(float value) {
        light.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        light.setColor(color);
    }

    @Override
    public void setLightState(LightState state) {
        lightState = state;
        light.setLightState(lightState);
    }
}
