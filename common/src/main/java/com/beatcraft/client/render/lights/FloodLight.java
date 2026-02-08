package com.beatcraft.client.render.lights;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.RenderUtil;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class FloodLight extends LightObject {

    private final float startOffset;
    private final float width;
    private final float length;
    private final float fadeLength;
    private final float spread;
    private final float[] segmentLengths;

    private List<Pair<Vector3f, Float>[]> facesInner;
    private List<Pair<Vector3f, Float>[]> fadeFacesInner;
    private List<Pair<Vector3f, Float>[]> linesInner;

    private List<Pair<Vector3f, Float>[]> facesMiddle;
    private List<Pair<Vector3f, Float>[]> fadeFacesMiddle;
    private List<Pair<Vector3f, Float>[]> linesMiddle;

    private List<Pair<Vector3f, Float>[]> facesOuter;
    private List<Pair<Vector3f, Float>[]> fadeFacesOuter;
    private List<Pair<Vector3f, Float>[]> linesOuter;

    private static final float INNER_WIDTH_SCALE = 0.5f;
    private static final float INNER_LENGTH_SCALE = 0.2f;
    private static final float INNER_SPREAD_MULTIPLIER = 1.0f;

    private static final float MIDDLE_WIDTH_SCALE = 0.75f;
    private static final float MIDDLE_LENGTH_SCALE = 0.85f;
    private static final float MIDDLE_SPREAD_MULTIPLIER = 1.3f;

    private static final float OUTER_WIDTH_SCALE = 1.0f;
    private static final float OUTER_LENGTH_SCALE = 1.0f;
    private static final float OUTER_SPREAD_MULTIPLIER = 1.0f;

    public FloodLight cloneOffset(Vector3f offset) {
        return (FloodLight) new FloodLight(mapController, startOffset, width, length, fadeLength, spread, segmentLengths, position.add(offset, new Vector3f()), new Quaternionf(orientation))
            .withRotation(new Quaternionf(rotation))
            .withTranslationSwizzle(translationSwizzle, translationPolarity)
            .withRotationSwizzle(rotationSwizzle, rotationPolarity, quaternionBuilder);
    }

    public FloodLight(BeatmapController map, float startOffset, float width, float length, float fadeLength, float spread, float[] segmentLengths, Vector3f pos, Quaternionf rot) {
        super(map);
        this.startOffset = startOffset;
        this.width = width;
        this.length = length;
        this.fadeLength = fadeLength;
        this.spread = spread;
        this.segmentLengths = segmentLengths;
        position = pos;
        orientation = rot;
        setDimensions(startOffset, width, length, fadeLength, spread, segmentLengths);
        lightState = new LightState(new Color(0), 0);
    }

    public FloodLight withRotation(Quaternionf rotation) {
        this.rotation = rotation;
        return this;
    }

    private List<Pair<Vector3f, Float>[]> getFaces(Hitbox bounds, float spread, boolean includeBottomFace, float low, float high) {
        var out = new ArrayList<Pair<Vector3f, Float>[]>();
        if (includeBottomFace) {
            out.add(new Pair[] {
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), high),
                new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), high),
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), high),
                new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), high),
            });
        }
        out.add(new Pair[] {
            new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), high),
            new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), high),
            new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.max.z+spread), low),
            new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.min.z-spread), low),
        });
        out.add(new Pair[] {
            new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.min.z-spread), low),
            new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.max.z+spread), low),
            new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), high),
            new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), high),
        });
        out.add(new Pair[] {
            new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.min.z-spread), low),
            new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.min.z-spread), low),
            new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), high),
            new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), high),
        });
        out.add(new Pair[] {
            new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), high),
            new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), high),
            new Pair<>(new Vector3f(bounds.max.x+spread, bounds.max.y, bounds.max.z+spread), low),
            new Pair<>(new Vector3f(bounds.min.x-spread, bounds.max.y, bounds.max.z+spread), low),
        });
        return out;
    }

    private List<Pair<Vector3f, Float>[]> getLines(Hitbox bounds, float spread) {
        return List.of(
            // Bottom face edges
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1f), new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1f), new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1f), new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1f), new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1f) },

            // Top face edges (spread)
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0f), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0f), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0f), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0f), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0f) },

            // Vertical connecting edges
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.min.z), 1f), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.min.z - spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.min.x, bounds.min.y, bounds.max.z), 1f), new Pair<>(new Vector3f(bounds.min.x - spread, bounds.max.y, bounds.max.z + spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.max.z), 1f), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.max.z + spread), 0f) },
            new Pair[]{ new Pair<>(new Vector3f(bounds.max.x, bounds.min.y, bounds.min.z), 1f), new Pair<>(new Vector3f(bounds.max.x + spread, bounds.max.y, bounds.min.z - spread), 0f) }
        );
    }

    public void setDimensions(float startOffset, float width, float length, float fadeLength, float spread, float[] segmentLengths) {
        setupLayer(startOffset, width * INNER_WIDTH_SCALE, length * INNER_LENGTH_SCALE,
            fadeLength * INNER_LENGTH_SCALE, spread * INNER_SPREAD_MULTIPLIER, segmentLengths,
            layer -> {
                facesInner = layer.faces;
                fadeFacesInner = layer.fadeFaces;
                linesInner = layer.lines;
            });

        setupLayer(startOffset, width * MIDDLE_WIDTH_SCALE, length * MIDDLE_LENGTH_SCALE,
            fadeLength * MIDDLE_LENGTH_SCALE, spread * MIDDLE_SPREAD_MULTIPLIER, segmentLengths,
            layer -> {
                facesMiddle = layer.faces;
                fadeFacesMiddle = layer.fadeFaces;
                linesMiddle = layer.lines;
            });

        setupLayer(startOffset, width * OUTER_WIDTH_SCALE, length * OUTER_LENGTH_SCALE,
            fadeLength * OUTER_LENGTH_SCALE, spread * OUTER_SPREAD_MULTIPLIER, segmentLengths,
            layer -> {
                facesOuter = layer.faces;
                fadeFacesOuter = layer.fadeFaces;
                linesOuter = layer.lines;
            });
    }

    private void setupLayer(float startOffset, float width, float length, float fadeLength, float spread,
                            float[] segmentLengths, java.util.function.Consumer<LayerGeometry> setter) {
        var maxY = startOffset + length;
        var fadeY = startOffset + fadeLength;
        var delta = width / 2f;

        var baseDimensions = new Hitbox(
            new Vector3f(-delta, startOffset, -delta),
            new Vector3f(delta, maxY, delta)
        );

        var midSpread = spread * fadeLength/length;

        var yVals = new ArrayList<Float>();
        for (var y : segmentLengths) {
            yVals.add(y);
        }
        yVals.add(fadeY);

        yVals.sort(Float::compare);

        var cy = startOffset;
        ArrayList<Pair<Vector3f, Float>[]> faces0 = new ArrayList<>();
        for (var y : yVals) {
            var dl0 = MathUtil.inverseLerp(startOffset, fadeY, cy);
            var dl1 = MathUtil.inverseLerp(startOffset, fadeY, y);

            var s0 = Mth.lerp(dl0, 0, midSpread);
            var s1 = Mth.lerp(dl1, 0, midSpread);

            var dim = new Hitbox(
                new Vector3f(-width - s0, cy, -width - s0),
                new Vector3f(width + s0, y, width + s0)
            );

            var subSection = getFaces(dim, s1-s0, cy == startOffset, 1-dl1, 1-dl0);
            faces0.addAll(subSection);

            cy = y;
        }

        LayerGeometry layer = new LayerGeometry();
        layer.faces = getFaces(baseDimensions, spread, true, 0, 1);
        layer.fadeFaces = List.copyOf(faces0);
        layer.lines = getLines(baseDimensions, spread);

        setter.accept(layer);
    }

    private static class LayerGeometry {
        List<Pair<Vector3f, Float>[]> faces;
        List<Pair<Vector3f, Float>[]> fadeFaces;
        List<Pair<Vector3f, Float>[]> lines;
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

        var mat = matrices.last().pose();

        if (bloomfog != null) {
            bloomfog.record(
                (b, c, r, m) -> _render(
                    mat,
                    b, c, 1, r,
                    ori, rot, wrot, pos, off, state, m
                )
            );
            bloomfog.recordBloomCall((b, v, q) -> {
                _renderBloom(mat, b, v, q, ori3, rot3, wrot3, pos3, off3, state);
            });
        }
        mapController.recordLightRenderCall(
            (b, c) -> _render(
                mat,
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

    private void _renderBloom(Matrix4f transform, BufferBuilder buffer, Vector3f cameraPos, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState) {
        var color = lightState.getBloomColor();

        if (((color >> 24) & 0xFF) <= 1) {
            return;
        }

        var c = new Color(color);

        var brightnessScale = (lightState.getBrightness() * 0.25f) + 0.75f;

        var mat = createTransformMatrix(transform, false, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);

        renderBloomLayer(buffer, cameraRotation, mat, fadeFacesOuter, c, brightnessScale);

        renderBloomLayer(buffer, cameraRotation, mat, fadeFacesMiddle, c, brightnessScale);

        renderBloomLayer(buffer, cameraRotation, mat, fadeFacesInner, c, brightnessScale);
    }

    private void renderBloomLayer(BufferBuilder buffer, Quaternionf cameraRotation, Matrix4f mat,
                                  List<Pair<Vector3f, Float>[]> faces, Color color, float brightnessScale) {
        for (var face : faces) {
            var v0 = face[0].getA().mul(1, brightnessScale, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            var v1 = face[1].getA().mul(1, brightnessScale, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            var v2 = face[2].getA().mul(1, brightnessScale, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            var v3 = face[3].getA().mul(1, brightnessScale, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            v0.rotate(cameraRotation);
            v1.rotate(cameraRotation);
            v2.rotate(cameraRotation);
            v3.rotate(cameraRotation);

            buffer.addVertex(v0).setColor(color.withAlpha(face[0].getB()).lerpBrightness(face[0].getB()));
            buffer.addVertex(v1).setColor(color.withAlpha(face[1].getB()).lerpBrightness(face[1].getB()));
            buffer.addVertex(v2).setColor(color.withAlpha(face[2].getB()).lerpBrightness(face[2].getB()));

            buffer.addVertex(v0).setColor(color.withAlpha(face[0].getB()).lerpBrightness(face[0].getB()));
            buffer.addVertex(v2).setColor(color.withAlpha(face[2].getB()).lerpBrightness(face[2].getB()));
            buffer.addVertex(v3).setColor(color.withAlpha(face[3].getB()).lerpBrightness(face[3].getB()));
        }
    }

    private void _render(Matrix4f transform, BufferBuilder buffer, Vector3f cameraPos, int isBloomfog, Quaternionf cameraRotation, Quaternionf orientation, Quaternionf rotation, Quaternionf worldRotation, Vector3f position, Vector3f offset, LightState lightState, boolean mirrorDraw) {
        var color = lightState.getBloomColor();

        if (((color >> 24) & 0xFF) <= 1) {
            return;
        }

        var mat = createTransformMatrix(transform, mirrorDraw, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);

        var c = new Color(color);

        if (isBloomfog == 1 && !mirrorDraw) {
            renderLines(buffer, cameraRotation, mat, linesOuter, color);
            renderLines(buffer, cameraRotation, mat, linesMiddle, color);
            renderLines(buffer, cameraRotation, mat, linesInner, color);
        } else {
            var iterFacesOuter = isBloomfog > 0 ? facesOuter : fadeFacesOuter;
            var iterFacesMiddle = isBloomfog > 0 ? facesMiddle : fadeFacesMiddle;
            var iterFacesInner = isBloomfog > 0 ? facesInner : fadeFacesInner;

            renderFaces(buffer, cameraRotation, mat, iterFacesOuter, c, isBloomfog, mirrorDraw);

            renderFaces(buffer, cameraRotation, mat, iterFacesMiddle, c, isBloomfog, mirrorDraw);

            renderFaces(buffer, cameraRotation, mat, iterFacesInner, c, isBloomfog, mirrorDraw);
        }
    }

    private void renderLines(BufferBuilder buffer, Quaternionf cameraRotation, Matrix4f mat,
                             List<Pair<Vector3f, Float>[]> lines, int color) {
        for (var line : lines) {
            var v0 = line[0].getA().mul(1, 1, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            var v1 = line[1].getA().mul(1, 1, 1, new Vector3f()).mulPosition(mat, new Vector3f());
            v0.rotate(cameraRotation);
            v1.rotate(cameraRotation);
            var n = v1.sub(v0, new Vector3f());

            List<Pair<Vector3f, Float>[]> segments = RenderUtil.chopEdgeLerp(v0, v1, 5, line[0].getB(), line[1].getB());

            for (var segment : segments) {
                buffer.addVertex(segment[0].getA()).setColor((new Color(color).withAlpha(segment[0].getB()).lerpBrightness(segment[0].getB()))).setNormal(n.x, n.y, n.z);
                buffer.addVertex(segment[1].getA()).setColor((new Color(color).withAlpha(segment[1].getB()).lerpBrightness(segment[1].getB()))).setNormal(-n.x, -n.y, -n.z);
            }
        }
    }

    private void renderFaces(BufferBuilder buffer, Quaternionf cameraRotation, Matrix4f mat,
                             List<Pair<Vector3f, Float>[]> faces, Color color, int isBloomfog,
                             boolean mirrorDraw) {
        for (var face : faces) {
            var v0 = face[0].getA().mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat);
            var v1 = face[1].getA().mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat);
            var v2 = face[2].getA().mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat);
            var v3 = face[3].getA().mul(1, mirrorDraw ? -1 : 1, 1, new Vector3f()).mulPosition(mat);

            if (isBloomfog > 0) {
                v0.rotate(cameraRotation);
                v1.rotate(cameraRotation);
                v2.rotate(cameraRotation);
                v3.rotate(cameraRotation);
            }

            buffer.addVertex(v0).setColor(color.withAlpha((face[0].getB())).lerpBrightness(face[0].getB()));
            buffer.addVertex(v1).setColor(color.withAlpha((face[1].getB())).lerpBrightness(face[1].getB()));
            buffer.addVertex(v2).setColor(color.withAlpha((face[2].getB())).lerpBrightness(face[2].getB()));
            buffer.addVertex(v3).setColor(color.withAlpha((face[3].getB())).lerpBrightness(face[3].getB()));
        }
    }

}