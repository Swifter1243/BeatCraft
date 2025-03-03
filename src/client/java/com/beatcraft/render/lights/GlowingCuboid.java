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

        bloomfog.record((b, c) -> _render(b, c, true));

        BeatcraftRenderer.recordLightRenderCall((b, c) -> _render(b, c, false));

    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, boolean isBloomfog) {
        var color = isBloomfog ? lightState.getColor() : lightState.getEffectiveColor();
        var posMod = isBloomfog ? lightState.getBrightness()+0.5f : 1;

        for (var face : faces) {


            if ((color & 0xFF000000) == 0) {
                continue;
            }


            var v0 = face[0].mul(posMod, new Vector3f()).rotate(orientation).rotate(rotation).add(position).add(offset).sub(cameraPos);
            var v1 = face[1].mul(posMod, new Vector3f()).rotate(orientation).rotate(rotation).add(position).add(offset).sub(cameraPos);
            var v2 = face[2].mul(posMod, new Vector3f()).rotate(orientation).rotate(rotation).add(position).add(offset).sub(cameraPos);
            var v3 = face[3].mul(posMod, new Vector3f()).rotate(orientation).rotate(rotation).add(position).add(offset).sub(cameraPos);

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
