package com.beatcraft.client.lightshow.environment.thefirst;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.effect.Bloomfog;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class InnerRing extends LightObject {


    public InnerRing(BeatmapController map) {
        super(map);
        orientation = new Quaternionf().rotationZ(45 * Mth.DEG_TO_RAD);
    }

    @Override
    public InnerRing cloneOffset(Vector3f offset) {
        return this;
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {

        var pos = new Vector3f(position);
        var off = new Vector3f(offset);
        var ori = new Quaternionf(orientation);
        var rot = new Quaternionf(rotation);

        mapController.recordBloomfogPosColCall((t, b, c) ->
            _render(t, b, c, pos, off, ori, rot, bloomfog)
        );
    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot) {
        return new Vector3f(base)
            .rotate(ori).add(pos)
            .rotate(rot).add(off);
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

    private void _render(Matrix4f transform, BufferBuilder buffer, Vector3f cameraPos, Vector3f position, Vector3f offset, Quaternionf orientation, Quaternionf rotation, Bloomfog bloomfog) {
        // manual mesh building since loading over-sized json model doesn't work >:(
        for (Vector3f mod : modifiers) {
            for (Vector3f vertex : vertices) {
                buffer.addVertex(cameraPos.negate(new Vector3f()).add(transform.transformPosition(processVertex(vertex.mul(mod, new Vector3f()), position, offset, orientation, rotation)))).setColor(color);
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
