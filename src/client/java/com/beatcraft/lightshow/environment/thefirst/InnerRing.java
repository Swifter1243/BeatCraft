package com.beatcraft.lightshow.environment.thefirst;

import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.effect.MirrorHandler;
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
    public void render(MatrixStack matrices, Camera camera) {

        var pos = new Vector3f(position);
        var off = new Vector3f(offset);
        var ori = new Quaternionf(orientation);
        var rot = new Quaternionf(rotation);

        BeatcraftRenderer.recordBloomfogPosColCall((b, c) ->
            _render(b, c, pos, off, ori, rot, false)
        );
        MirrorHandler.recordBloomfogPosColCall((b, c) ->
            _render(b, c, pos, off, ori, rot, true)
        );

    }

    private Quaternionf mirrorQuaternion(Quaternionf quat, boolean mirrored) {
        return mirrored ? new Quaternionf(-quat.x, quat.y, -quat.z, quat.w) : quat;
    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot, Vector3f camera, boolean mirrored) {
        return new Vector3f(base).mul(1, mirrored ? -1 : 1, 1)
            .rotate(mirrorQuaternion(ori, mirrored)).add(pos.mul(1, mirrored ? -1 : 1, 1, new Vector3f()))
            .rotate(mirrorQuaternion(rot, mirrored)).add(off.mul(1, mirrored ? -1 : 1, 1, new Vector3f()))
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

    private void _render(BufferBuilder buffer, Vector3f cameraPos, Vector3f position, Vector3f offset, Quaternionf orientation, Quaternionf rotation, boolean mirrored) {
        for (Vector3f mod : modifiers) {
            for (Vector3f vertex : vertices) {
                buffer.vertex(processVertex(vertex.mul(mod, new Vector3f()), position, offset, orientation, rotation, cameraPos, mirrored)).color(color);
            }
        }
    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
