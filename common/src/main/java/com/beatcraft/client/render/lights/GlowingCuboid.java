package com.beatcraft.client.render.lights;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.RenderUtil;
import com.beatcraft.client.render.effect.Bloomfog;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class GlowingCuboid extends LightObject {

    private Hitbox dimensions;
    private List<Vector3f[]> faces;
    private List<Vector3f[]> lines;

    public GlowingCuboid(BeatmapPlayer map, Hitbox dimensions, Vector3f pos, Quaternionf rot) {
        super(map);
        position = pos;
        orientation = rot;
        setDimensions(dimensions);
        lightState = new LightState(new Color(0), 0);
    }

    public GlowingCuboid cloneOffset(Vector3f offset) {
        return new GlowingCuboid(
            mapController,
            new Hitbox(
                new Vector3f(dimensions.min),
                new Vector3f(dimensions.max)
            ),
            position.add(offset, new Vector3f()),
            new Quaternionf(orientation)
        );
    }

    public void setDimensions(Hitbox dimensions) {
        this.dimensions = dimensions;
        faces = BeatcraftRenderer.getCubeFaces(dimensions.min, dimensions.max);
        lines = BeatcraftRenderer.getCubeEdges(dimensions.min, dimensions.max);
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {

        Vector3f pos = MemoryPool.newVector3f(position);
        Vector3f off = MemoryPool.newVector3f(offset);
        Quaternionf ori = MemoryPool.newQuaternionf(orientation);
        Quaternionf rot = MemoryPool.newQuaternionf(rotation);
        Quaternionf wrot = MemoryPool.newQuaternionf(worldRotation);

        Vector3f pos3 = MemoryPool.newVector3f(position);
        Vector3f off3 = MemoryPool.newVector3f(offset);
        Quaternionf ori3 = MemoryPool.newQuaternionf(orientation);
        Quaternionf rot3 = MemoryPool.newQuaternionf(rotation);
        Quaternionf wrot3 = MemoryPool.newQuaternionf(worldRotation);

        Vector3f pos4 = MemoryPool.newVector3f(position);
        Vector3f off4 = MemoryPool.newVector3f(offset);
        Quaternionf ori4 = MemoryPool.newQuaternionf(orientation);
        Quaternionf rot4 = MemoryPool.newQuaternionf(rotation);
        Quaternionf wrot4 = MemoryPool.newQuaternionf(worldRotation);
        LightState state = lightState.copy();
        state.clampAlpha();

        //DebugRenderer.renderHitbox(dimensions, new Vector3f(pos).rotate(rot).add(off), new Quaternionf(ori).mul(rot), 0xFFFF0000);

        if (bloomfog != null) {
            bloomfog.record(
                (b, c, r, m) -> _render(
                    b, c, 1, r,
                    ori, rot, wrot, pos, off, state, m
                )
            );
            bloomfog.recordBloomCall((b, v, q) -> {
                _renderBloom(b, v, q, ori3, rot3, wrot3, pos3, off3, state);
            });
        }
        mapController.recordLightRenderCall(
            (b, c) -> _render(
                b, c, 0, null,
                ori4, rot4, wrot4, pos4, off4, state, false
            )
        );

    }

    private void _renderBloom(BufferBuilder buffer, Vector3f cameraPos, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState) {
        var color = lightState.getBloomColor();

        if (((color >> 24) & 0xFF) <= 1) {
            return;
        }
        var mat = createTransformMatrix(false, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);

        for (var face : faces) {

            var v0 = face[0].mulPosition(mat, new Vector3f());
            var v1 = face[1].mulPosition(mat, new Vector3f());
            var v2 = face[2].mulPosition(mat, new Vector3f());
            var v3 = face[3].mulPosition(mat, new Vector3f());
            v0.rotate(cameraRotation);
            v1.rotate(cameraRotation);
            v2.rotate(cameraRotation);
            v3.rotate(cameraRotation);

            buffer.addVertex(v0).setColor(color);
            buffer.addVertex(v1).setColor(color);
            buffer.addVertex(v2).setColor(color);

            buffer.addVertex(v0).setColor(color);
            buffer.addVertex(v2).setColor(color);
            buffer.addVertex(v3).setColor(color);

        }

    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, int isBloomfog, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState, boolean mirrorDraw) {
        var color = isBloomfog > 0 ? lightState.getBloomColor() : lightState.getEffectiveColor();

        if (((color >> 24) & 0xFF) <= 1) {
            return;
        }
        var mat = createTransformMatrix(mirrorDraw, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);

        if (isBloomfog == 1 && !mirrorDraw) {

            for (var line : lines) {
                var v0 = line[0].mulPosition(mat, new Vector3f());
                var v1 = line[1].mulPosition(mat, new Vector3f());
                v0.rotate(cameraRotation);
                v1.rotate(cameraRotation);
                var n = v1.sub(v0, new Vector3f());

                List<Vector3f[]> segments = RenderUtil.chopEdge(v0, v1);

                for (var segment : segments) {
                    buffer.addVertex(segment[0]).setColor(color).setNormal(n.x, n.y, n.z);
                    buffer.addVertex(segment[1]).setColor(color).setNormal(-n.x, -n.y, -n.z);
                }
            }
        } else {

            for (var face : faces) {

                var v0 = face[0].mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat); // processVertex(face[0].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v1 = face[1].mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat); // processVertex(face[1].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v2 = face[2].mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat); // processVertex(face[2].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v3 = face[3].mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat); // processVertex(face[3].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);

                if (isBloomfog > 0) {
                    v0.rotate(cameraRotation);
                    v1.rotate(cameraRotation);
                    v2.rotate(cameraRotation);
                    v3.rotate(cameraRotation);
                }

                buffer.addVertex(v0).setColor(color);
                buffer.addVertex(v1).setColor(color);
                buffer.addVertex(v2).setColor(color);
                buffer.addVertex(v3).setColor(color);

                //List<Vector3f[]> sections = RenderUtil.sliceQuad(v0, v1, v2, v3, 10);
                //
                //for (var quad : sections) {
                //    buffer.vertex(quad[0]).color(color);
                //    buffer.vertex(quad[1]).color(color);
                //    buffer.vertex(quad[2]).color(color);
                //    buffer.vertex(quad[3]).color(color);
                //}
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
