package com.beatcraft.client.lightshow.environment.triangle;

import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    public InnerRing cloneOffset(Vector3f offset) {
        return this;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        resetVertices();

        var pos = new Vector3f(position);
        var off = new Vector3f(offset);
        var ori = new Quaternionf(orientation);
        var rot = new Quaternionf(rotation);

        BeatCraftRenderer.recordBloomfogPosColCall((b, c) ->
            _render(b, c, pos, off, ori, rot, bloomfog)
        );
    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot, Vector3f camera) {
        return new Vector3f(base)
            .rotate(ori).add(pos)
            .rotate(rot).add(off)
            .sub(camera);
    }

    private static final float ringRadius = 11;
    private static final float ringWidth = 1f;
    private static final float ringDepth = 0.1f;
    private static final int color = 0xFF000000;

    private static final float rotationOffsetRad = 105f * MathHelper.RADIANS_PER_DEGREE;

    private static Vector3f dir(float angleRad) {
        return new Vector3f(
            (float) Math.cos(angleRad),
            (float) Math.sin(angleRad),
            0
        );
    }



    private static Vector3f offset(Vector3f dir, float dist, float z) {
        return new Vector3f(
            dir.x * dist,
            dir.y * dist,
            z
        ).rotateZ(45 * MathHelper.RADIANS_PER_DEGREE).add(
            new Vector3f(
                (float) (Math.cos(105 * MathHelper.RADIANS_PER_DEGREE) * ringRadius),
                (float) -(Math.sin(-15 * MathHelper.RADIANS_PER_DEGREE) * ringRadius),
                0
            ).rotateZ(-45 * MathHelper.RADIANS_PER_DEGREE)
        );
    }

    private static final Vector3f[] dirs = new Vector3f[] {
        dir(rotationOffsetRad),
        dir(rotationOffsetRad + (float)(2 * Math.PI / 3)),
        dir(rotationOffsetRad + (float)(4 * Math.PI / 3))
    };

    private static Vector3f[] vertices = new Vector3f[] {
        offset(dirs[0], ringRadius + ringWidth, -ringDepth),
        offset(dirs[1], ringRadius + ringWidth, -ringDepth),
        offset(dirs[2], ringRadius + ringWidth, -ringDepth),

        offset(dirs[0], ringRadius, 0),
        offset(dirs[1], ringRadius, 0),
        offset(dirs[2], ringRadius, 0),

        offset(dirs[0], ringRadius, ringDepth),
        offset(dirs[1], ringRadius, ringDepth),
        offset(dirs[2], ringRadius, ringDepth),
    };

    public static void resetVertices() {
        vertices = new Vector3f[] {
            offset(dirs[0], ringRadius + ringWidth, -ringDepth),
            offset(dirs[1], ringRadius + ringWidth, -ringDepth),
            offset(dirs[2], ringRadius + ringWidth, -ringDepth),

            offset(dirs[0], ringRadius, 0),
            offset(dirs[1], ringRadius, 0),
            offset(dirs[2], ringRadius, 0),

            offset(dirs[0], ringRadius, ringDepth),
            offset(dirs[1], ringRadius, ringDepth),
            offset(dirs[2], ringRadius, ringDepth),
        };
    }

    // 6 quads: 3 front, 3 inner
    private static final Integer[] quads = new Integer[] {
        0, 1, 4, 3,
        1, 2, 5, 4,
        2, 0, 3, 5,

        3, 4, 7, 6,
        4, 5, 8, 7,
        5, 3, 6, 8,
    };

    private void _render(BufferBuilder buffer, Vector3f cameraPos, Vector3f position, Vector3f offset, Quaternionf orientation, Quaternionf rotation, Bloomfog bloomfog) {
        for (var vertex : quads) {
            buffer.vertex(processVertex(vertices[vertex], position, offset, orientation, rotation, cameraPos)).color(color);
        }


    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
