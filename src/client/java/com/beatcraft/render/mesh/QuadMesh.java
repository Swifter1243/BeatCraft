package com.beatcraft.render.mesh;

import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class QuadMesh implements Mesh {

    public ArrayList<Vector3f> vertices;
    public ArrayList<Quad> quads;
    public Identifier texture;
    public int color;

    public QuadMesh(List<Vector3f> vertices, List<Quad> quads) {
        this.vertices = new ArrayList<>(vertices);
        this.quads = new ArrayList<>(quads);
        this.color = 0xFFFFFFFF;
    }

    public QuadMesh() {
        this.vertices = new ArrayList<>();
        this.quads = new ArrayList<>();
        this.color = 0xFFFFFFFF;
    }

    public TriangleMesh toTriangleMesh() {

        TriangleMesh newMesh = new TriangleMesh();

        for (Quad quad : quads) {
            newMesh.addTris(quad.toTriangles(this, newMesh));
        }

        newMesh.texture = texture;
        newMesh.color = color;

        return newMesh;
    }

    public List<Pair<Vector3f, Vector2f>> getQuadVerticesAndUvs(Quad quad) {
        return List.of(
            new Pair<>(vertices.get(quad.a()), quad.uvA()),
            new Pair<>(vertices.get(quad.b()), quad.uvB()),
            new Pair<>(vertices.get(quad.c()), quad.uvC()),
            new Pair<>(vertices.get(quad.d()), quad.uvD())
        );
    }

    public int vertIdx(Vector3f vec) {
        return vertices.indexOf(vec);
    }

    public void addPermutedVertices(Vector3f min, Vector3f max) {
        addIfNotPresent(new Vector3f(min.x, min.y, min.z));
        addIfNotPresent(new Vector3f(min.x, min.y, max.z));
        addIfNotPresent(new Vector3f(min.x, max.y, min.z));
        addIfNotPresent(new Vector3f(min.x, max.y, max.z));
        addIfNotPresent(new Vector3f(max.x, min.y, min.z));
        addIfNotPresent(new Vector3f(max.x, min.y, max.z));
        addIfNotPresent(new Vector3f(max.x, max.y, min.z));
        addIfNotPresent(new Vector3f(max.x, max.y, max.z));
    }

    public void addUniquePermutedVertices(Vector3f min, Vector3f max) {
        addUnique(new Vector3f(min.x, min.y, min.z));
        addUnique(new Vector3f(min.x, min.y, max.z));
        addUnique(new Vector3f(min.x, max.y, min.z));
        addUnique(new Vector3f(min.x, max.y, max.z));
        addUnique(new Vector3f(max.x, min.y, min.z));
        addUnique(new Vector3f(max.x, min.y, max.z));
        addUnique(new Vector3f(max.x, max.y, min.z));
        addUnique(new Vector3f(max.x, max.y, max.z));
    }

    public void addIfNotPresent(Vector3f vec) {
        if (!vertices.contains(vec)) vertices.add(vec);
    }

    public void addUnique(Vector3f vec) {
        vertices.add(vec);
    }

    /// transformer modifies vertices in-place for simplicity
    public void transformVertices(int start, int end, Consumer<Vector3f> transformer) {
        for (var vert : vertices.subList(start, end)) {
            transformer.accept(vert);
        }
    }

    @Override
    public void drawToBuffer(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        quads.forEach(quad -> {
            quad.draw(buffer, color, this, position, orientation, cameraPos);
        });
    }

}
