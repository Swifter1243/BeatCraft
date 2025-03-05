package com.beatcraft.render.lights;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.effect.Bloomfog;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class GlowingCuboid extends LightObject {

    private Hitbox dimensions;
    private List<Vector3f[]> faces;

    public GlowingCuboid(Hitbox dimensions, Vector3f pos, Quaternionf rot) {
        position = pos;
        orientation = rot;
        setDimensions(dimensions);
        lightState = new LightState(new Color(0, 0, 0, 0), 0);
    }

    public GlowingCuboid cloneOffset(Vector3f offset) {
        return new GlowingCuboid(new Hitbox(new Vector3f(dimensions.min), new Vector3f(dimensions.max)), position.add(offset, new Vector3f()), new Quaternionf(orientation));
    }

    public void setDimensions(Hitbox dimensions) {
        this.dimensions = dimensions;
        faces = BeatcraftRenderer.getCubeFaces(dimensions.min, dimensions.max);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

        if (bloomfog != null) bloomfog.record((b, c, r) -> _render(b, c, true, r));

        BeatcraftRenderer.recordLightRenderCall((b, c) -> _render(b, c, false, null));

    }

    private Vector3f processVertex(Vector3f basePos, Vector3f cameraPos, boolean isBloomfog) {
        return basePos
            .rotate(orientation, new Vector3f())
            .rotate(rotation)
            .add(position)
            .add(offset)
            .sub(cameraPos);
    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, boolean isBloomfog, Quaternionf cameraRotation) {
        var color = isBloomfog ? lightState.getColor() : lightState.getEffectiveColor();

        for (var face : faces) {

            if ((color & 0xFF000000) == 0) {
                continue;
            }

            var v0 = processVertex(face[0], cameraPos, isBloomfog);
            var v1 = processVertex(face[1], cameraPos, isBloomfog);
            var v2 = processVertex(face[2], cameraPos, isBloomfog);
            var v3 = processVertex(face[3], cameraPos, isBloomfog);

            if (isBloomfog) {
                v0.rotate(cameraRotation);
                v1.rotate(cameraRotation);
                v2.rotate(cameraRotation);
                v3.rotate(cameraRotation);
            }

            buffer.vertex(v0).color(color);
            buffer.vertex(v1).color(color);
            buffer.vertex(v2).color(color);
            buffer.vertex(v3).color(color);

        }


    }

    @Override
    public void setValue(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }
}
