package com.beatcraft.render.lights;

import com.beatcraft.BeatCraft;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.RenderUtil;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FloodLight extends LightObject {

    private final float startOffset;
    private final float width;
    private final float length;
    private final float fadeLength;
    private final float spread;
    private List<Pair<Vector3f, Integer>[]> faces;
    private List<Pair<Vector3f, Integer>[]> fadeFaces;
    private List<Pair<Vector3f, Integer>[]> lines;

    public FloodLight cloneOffset(Vector3f offset) {
        return new FloodLight(startOffset, width, length, fadeLength, spread, position.add(offset, new Vector3f()), new Quaternionf(orientation));
    }

    public FloodLight(float startOffset, float width, float length, float fadeLength, float spread, Vector3f pos, Quaternionf rot) {

        this.startOffset = startOffset;
        this.width = width;
        this.length = length;
        this.fadeLength = fadeLength;
        this.spread = spread;
        position = pos;
        orientation = rot;
        setDimensions(startOffset, width, length, fadeLength, spread);
        lightState = new LightState(new Color(0, 0, 0, 0), 0);
    }

    private List<Pair<Vector3f, Integer>[]> getFaces(Hitbox bounds, float spread) {
        return List.of(
            new Pair[] {
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1),
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1),
            },
            new Pair[] {
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1),
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.max.z+spread), 0),
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.min.z-spread), 0),
            },
            new Pair[] {
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.max.z+spread), 0),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.min.z-spread), 0),
            },
            new Pair[] {
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.min.z-spread), 0),
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.min.z-spread), 0),
            },
            new Pair[] {
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.max.z+spread), 0),
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.max.z+spread), 0),
            },
            new Pair[] {
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.min.z-spread), 0),
                new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.max.z+spread), 0),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.max.z+spread), 0),
                new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.min.z-spread), 0),
            }
        );
    }

    private List<Pair<Vector3f, Integer>[]> getLines(Hitbox bounds, float spread) {
        return List.of(
            // Bottom face edges
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1), new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1), new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1), new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1), new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1) },

            // Top face edges (spread)
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0) },

            // Vertical connecting edges
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0) }
        );
    }

    public void setDimensions(float startOffset, float width, float length, float fadeLength, float spread) {
        var maxY = startOffset + length;
        var fadeY = startOffset + fadeLength;
        var delta = width / 2f;

        var baseDimensions = new Hitbox(
            new Vector3f(-delta, startOffset, -delta),
            new Vector3f(delta, maxY, delta)
        );

        var fadeDimensions = new Hitbox(
            new Vector3f(-delta, startOffset, -delta),
            new Vector3f(delta, fadeY, delta)
        );

        var midSpread = spread * fadeLength/length;

        faces = getFaces(baseDimensions, spread);
        fadeFaces = getFaces(fadeDimensions, midSpread);
        lines = getLines(baseDimensions, spread);


    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

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
        BeatCraftRenderer.recordLightRenderCall(
            (b, c) -> _render(
                b, c, 0, null,
                ori4, rot4, wrot4, pos4, off4, state, false
            )
        );

    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }

    private void _renderBloom(BufferBuilder buffer, Vector3f cameraPos, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState) {
        var color = lightState.getBloomColor();

        if (((color >> 24) & 0xFF) <= 16) {
            return;
        }

        for (var face : fadeFaces) {

            var v0 = processVertex(face[0].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
            var v1 = processVertex(face[1].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
            var v2 = processVertex(face[2].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
            var v3 = processVertex(face[3].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
            v0.rotate(cameraRotation);
            v1.rotate(cameraRotation);
            v2.rotate(cameraRotation);
            v3.rotate(cameraRotation);

            buffer.vertex(v0).color(face[0].getRight() * color);
            buffer.vertex(v1).color(face[1].getRight() * color);
            buffer.vertex(v2).color(face[2].getRight() * color);

            buffer.vertex(v0).color(face[0].getRight() * color);
            buffer.vertex(v2).color(face[2].getRight() * color);
            buffer.vertex(v3).color(face[3].getRight() * color);

            //List<Vector3f[]> sections = RenderUtil.sliceQuad(v0, v1, v2, v3, 10);
            //
            //for (var quad : sections) {
            //    buffer.vertex(quad[0]).color(color);
            //    buffer.vertex(quad[1]).color(color);
            //    buffer.vertex(quad[2]).color(color);
            //
            //    buffer.vertex(quad[0]).color(color);
            //    buffer.vertex(quad[2]).color(color);
            //    buffer.vertex(quad[3]).color(color);
            //}
        }
    }

    private void _render(BufferBuilder buffer, Vector3f cameraPos, int isBloomfog, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState, boolean mirrorDraw) {
        var color = isBloomfog > 0 ? lightState.getBloomColor() : lightState.getEffectiveColor();

        if (((color >> 24) & 0xFF) <= 16) {
            return;
        }

        if (isBloomfog == 1 && !mirrorDraw) {
            for (var line : lines) {
                var v0 = processVertex(line[0].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
                var v1 = processVertex(line[1].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, false);
                v0.rotate(cameraRotation);
                v1.rotate(cameraRotation);
                var n = v1.sub(v0, new Vector3f());

                List<Pair<Vector3f, Float>[]> segments = RenderUtil.chopEdgeLerp(v0, v1, 5, line[0].getRight(), line[1].getRight());

                for (var segment : segments) {
                    buffer.vertex(segment[0].getLeft()).color((int) (color * segment[0].getRight())).normal(n.x, n.y, n.z);
                    buffer.vertex(segment[1].getLeft()).color((int) (color * segment[1].getRight())).normal(-n.x, -n.y, -n.z);
                }
            }
        } else {
            var iterFaces = isBloomfog > 0 ? faces : fadeFaces;

            for (var face : iterFaces) {

                var v0 = processVertex(face[0].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v1 = processVertex(face[1].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v2 = processVertex(face[2].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);
                var v3 = processVertex(face[3].getLeft(), cameraPos, orientation, rotation, worldRotation, position, offset, mirrorDraw);

                if (isBloomfog > 0) {
                    v0.rotate(cameraRotation);
                    v1.rotate(cameraRotation);
                    v2.rotate(cameraRotation);
                    v3.rotate(cameraRotation);
                }

                buffer.vertex(v0).color(color * face[0].getRight());
                buffer.vertex(v1).color(color * face[1].getRight());
                buffer.vertex(v2).color(color * face[2].getRight());
                buffer.vertex(v3).color(color * face[3].getRight());

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

}
