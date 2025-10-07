package com.beatcraft.client.lightshow.environment.triangle;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.effect.Bloomfog;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class OuterRing extends LightObject {

    private List<LightObject> lights;

    private static final float ringRadius = 27;
    private static final float ringWidth = 1;
    private static final float ringDepth = 0.5f;
    private static final int color = 0xFF000000;
    private static final float lightSize = 0.2f;
    private static final float lightOffset = 0.001f;
    protected boolean lightsOnly = false;

    public static OuterRing getLightsOnly(BeatmapController map, TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject> lightFactory) {
        var a = new OuterRing(map, lightFactory);
        a.lightsOnly = true;
        return a;
    }

    public OuterRing(BeatmapController map, TriFunction<BeatmapController, Vector3f, Quaternionf, LightObject> lightFactory) {
        super(map);
        orientation = new Quaternionf().rotationZ(45 * Mth.DEG_TO_RAD);
        lights = List.of(
            lightFactory.apply(map, new Vector3f( 0                           ,  ringRadius-(lightSize+0.01f), lightSize+lightOffset), new Quaternionf()),
            lightFactory.apply(map, new Vector3f( ringRadius-(lightSize+0.01f),  0                           , lightSize+lightOffset), new Quaternionf().rotationZ(90 * Mth.DEG_TO_RAD)),
            lightFactory.apply(map, new Vector3f( 0                           , -ringRadius+(lightSize+0.01f), lightSize+lightOffset), new Quaternionf().rotationZ(180 * Mth.DEG_TO_RAD)),
            lightFactory.apply(map, new Vector3f(-ringRadius+(lightSize+0.01f),  0                           , lightSize+lightOffset), new Quaternionf().rotationZ(-90 * Mth.DEG_TO_RAD))
        );
    }

    @Override
    public OuterRing cloneOffset(Vector3f offset) {
        return this;
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {

        var pos = new Vector3f(position);
        var off = new Vector3f(offset);
        var ori = new Quaternionf(orientation);
        var rot = new Quaternionf(rotation);

        if (!lightsOnly) {
            mapController.recordBloomfogPosColCall((t, b, c) ->
                _render(t, b, c, pos, off, ori, rot, bloomfog)
            );
        }

        for (var light : lights) {
            light.setWorldRotation(new Quaternionf(orientation).mul(rotation));
            light.setOffset(new Vector3f(position).rotate(rotation).add(offset).rotate(worldRotation));
            light.render(matrices, camera, alpha, bloomfog);
        }

    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot) {
        return new Vector3f(base)
            .rotate(ori).add(pos)
            .rotate(rot).add(off);
    }


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
        //light.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        //light.setColor(color);
    }

    @Override
    public void setLightState(LightState state) {
        //lightState = state;
        //light.setLightState(lightState);
    }
}
