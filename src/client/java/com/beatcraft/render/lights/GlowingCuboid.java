package com.beatcraft.render.lights;

import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.RenderUtil;
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
    private List<Vector3f[]> lines;

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
        lines = BeatcraftRenderer.getCubeEdges(dimensions.min, dimensions.max);
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

        Vector3f pos = new Vector3f(position);
        Vector3f off = new Vector3f(offset);
        Quaternionf ori = new Quaternionf(orientation);
        Quaternionf rot = new Quaternionf(rotation);
        Quaternionf wrot = new Quaternionf(worldRotation);
        LightState state = lightState.copy();

        //DebugRenderer.renderHitbox(dimensions, new Vector3f(pos).rotate(rot).add(off), new Quaternionf(ori).mul(rot), 0xFFFF0000);

        if (bloomfog != null) bloomfog.record(
            (b, c, r) -> _render(
                b, c, true, r,
                ori, rot, wrot, pos, off, state
            )
        );

        BeatcraftRenderer.recordLightRenderCall(
            (b, c) -> _render(
                b, c, false, null,
                ori, rot, wrot, pos, off, state
            )
        );

    }

    private Vector3f processVertex(Vector3f basePos, Vector3f cameraPos, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset) {
        return basePos
            .rotate(orientation, new Vector3f())
            .rotate(rotation)
            .add(position)
            .rotate(worldRotation)
            .add(offset)
            .sub(cameraPos);
    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, boolean isBloomfog, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState) {
        var color = isBloomfog ? lightState.getBloomColor() : lightState.getEffectiveColor();

        if ((color & 0xFF000000) == 0) {
            return;
        }

        if (isBloomfog) { // line buffer

            for (var line : lines) {


                var v0 = processVertex(line[0], cameraPos, orientation, rotation, worldRotation, position, offset);
                var v1 = processVertex(line[1], cameraPos, orientation, rotation, worldRotation, position, offset);
                v0.rotate(cameraRotation);
                v1.rotate(cameraRotation);
                var n = v1.sub(v0, new Vector3f());

                List<Vector3f[]> segments = RenderUtil.chopEdge(v0, v1);

                for (var segment : segments) {
                    buffer.vertex(segment[0]).color(color).normal(n.x, n.y, n.z);
                    buffer.vertex(segment[1]).color(color).normal(-n.x, -n.y, -n.z);
                }

            }

        } else { // quad buffer
            for (var face : faces) {

                var v0 = processVertex(face[0], cameraPos, orientation, rotation, worldRotation, position, offset);
                var v1 = processVertex(face[1], cameraPos, orientation, rotation, worldRotation, position, offset);
                var v2 = processVertex(face[2], cameraPos, orientation, rotation, worldRotation, position, offset);
                var v3 = processVertex(face[3], cameraPos, orientation, rotation, worldRotation, position, offset);

                List<Vector3f[]> sections = RenderUtil.sliceQuad(v0, v1, v2, v3, 10);

                for (var quad : sections) {
                    buffer.vertex(quad[0]).color(color);
                    buffer.vertex(quad[1]).color(color);
                    buffer.vertex(quad[2]).color(color);
                    buffer.vertex(quad[3]).color(color);
                }

            }
        }

    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }
}
