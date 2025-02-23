package com.beatcraft.render.mesh;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Quad implements Geometry {

    private final int[] indices;
    private final Vector2f[] uvs;

    public Quad(int a, int b, int c, int d, Vector2f uvA, Vector2f uvB, Vector2f uvC, Vector2f uvD) {
        indices = new int[]{a, b, c, d};
        uvs = new Vector2f[]{uvA, uvB, uvC, uvD};
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

    public int d() {
        return indices[3];
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

    public Vector2f uvD() {
        return uvs[3];
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector2f[] getUvs() {
        return uvs;
    }


    public Triangle[] toTriangles(QuadMesh quadMesh, TriangleMesh triMesh) {
        int ta, tb, tc, td;

        Vector3f v1 = quadMesh.vertices.get(indices[0]);
        Vector3f v2 = quadMesh.vertices.get(indices[1]);
        Vector3f v3 = quadMesh.vertices.get(indices[2]);
        Vector3f v4 = quadMesh.vertices.get(indices[3]);

        if (!triMesh.vertices.contains(v1)) triMesh.vertices.add(v1);
        if (!triMesh.vertices.contains(v2)) triMesh.vertices.add(v2);
        if (!triMesh.vertices.contains(v3)) triMesh.vertices.add(v3);
        if (!triMesh.vertices.contains(v4)) triMesh.vertices.add(v4);

        ta = triMesh.vertices.indexOf(v1);
        tb = triMesh.vertices.indexOf(v2);
        tc = triMesh.vertices.indexOf(v3);
        td = triMesh.vertices.indexOf(v4);

        return new Triangle[]{
            new Triangle(ta, tb, tc, uvs[0], uvs[1], uvs[2]),
            new Triangle(tc, ta, td, uvs[2], uvs[0], uvs[3])
        };
    }

    public void draw(BufferBuilder buffer, int color, QuadMesh mesh, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        Vector3f v0 = mesh.vertices.get(indices[0]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
        Vector3f v1 = mesh.vertices.get(indices[1]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
        Vector3f v2 = mesh.vertices.get(indices[2]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);
        Vector3f v3 = mesh.vertices.get(indices[3]).rotate(orientation, new Vector3f()).add(position).sub(cameraPos);

        buffer.vertex(v0.x, v0.y, v0.z).texture(uvs[0].x, uvs[0].y).color(color);
        buffer.vertex(v1.x, v1.y, v1.z).texture(uvs[1].x, uvs[1].y).color(color);
        buffer.vertex(v2.x, v2.y, v2.z).texture(uvs[2].x, uvs[2].y).color(color);
        buffer.vertex(v3.x, v3.y, v3.z).texture(uvs[3].x, uvs[3].y).color(color);

    }

}
