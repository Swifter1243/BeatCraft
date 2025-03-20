package com.beatcraft.render.mesh;

import com.beatcraft.BeatCraft;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Triangle implements Geometry {

    private final int[] indices;
    private final Vector2f[] uvs;

    public Triangle(List<Integer> indices, List<Vector2f> uvs) {
        this.indices = new int[]{indices.getFirst(), indices.get(1), indices.get(2)};
        this.uvs = uvs.toArray(new Vector2f[0]);
    }

    public Triangle(int a, int b, int c, Vector2f uvA, Vector2f uvB, Vector2f uvC) {
        indices = new int[]{a, b, c};
        uvs = new Vector2f[]{uvA, uvB, uvC};
    }

    public int a() {
        return indices[0];
    }

    public int b() {
        return indices[1];
    }

    public int c() {
        return indices[2];
    }

    public Vector2f uvA() {
        return uvs[0];
    }

    public Vector2f uvB() {
        return uvs[1];
    }

    public Vector2f uvC() {
        return uvs[2];
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector2f[] getUvs() {
        return uvs;
    }

    public void draw(BufferBuilder buffer, int color, TriangleMesh mesh, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        Vector3f v0 = mesh.vertices.get(indices[0]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
        Vector3f v1 = mesh.vertices.get(indices[1]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
        Vector3f v2 = mesh.vertices.get(indices[2]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);

        buffer.vertex(v0.x, v0.y, v0.z).texture(uvs[0].x, uvs[0].y).color(color);
        buffer.vertex(v1.x, v1.y, v1.z).texture(uvs[1].x, uvs[1].y).color(color);
        buffer.vertex(v2.x, v2.y, v2.z).texture(uvs[2].x, uvs[2].y).color(color);

    }

    public void drawMirrored(BufferBuilder buffer, int color, TriangleMesh mesh, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        Vector3f v0 = mesh.vertices.get(indices[0]).mul(1, -1, 1, new Vector3f()).rotate(orientation).add(position).sub(cameraPos);
        Vector3f v1 = mesh.vertices.get(indices[1]).mul(1, -1, 1, new Vector3f()).rotate(orientation).add(position).sub(cameraPos);
        Vector3f v2 = mesh.vertices.get(indices[2]).mul(1, -1, 1, new Vector3f()).rotate(orientation).add(position).sub(cameraPos);

        buffer.vertex(v0.x, v0.y, v0.z).texture(uvs[0].x, uvs[0].y).color(color);
        buffer.vertex(v1.x, v1.y, v1.z).texture(uvs[1].x, uvs[1].y).color(color);
        buffer.vertex(v2.x, v2.y, v2.z).texture(uvs[2].x, uvs[2].y).color(color);
    }

    ///  this function requires the vertices list to be passed for proper indexing. the vertices list will be modified by this method
    public static List<Triangle> fromList(ArrayList<Vector3f> vertices, List<List<Pair<Vector3f, Vector2f>>> triData) {
        ArrayList<Triangle> tris = new ArrayList<>();

        for (var tri : triData) {
            ArrayList<Integer> indices = new ArrayList<>();
            ArrayList<Vector2f> uvs = new ArrayList<>();

            for (var vert : tri) {
                int i;
                if (vertices.contains(vert.getLeft())) {
                    i = vertices.indexOf(vert.getLeft());
                } else {
                    i = vertices.size();
                    vertices.add(new Vector3f(vert.getLeft()));
                }

                indices.add(i);
                uvs.add(vert.getRight());
            }

            tris.add(new Triangle(indices, uvs));
        }

        return tris;
    }

}
