package com.beatcraft.render.mesh;

import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriangleMesh implements Mesh {

    public ArrayList<Vector3f> vertices;
    public ArrayList<Triangle> tris;
    public int color;
    public Identifier texture;

    public TriangleMesh() {
        vertices = new ArrayList<>();
        tris = new ArrayList<>();
    }

    public TriangleMesh(List<Vector3f> vertices, List<Triangle> tris) {
        this.vertices = new ArrayList<>(vertices);
        this.tris = new ArrayList<>(tris);
    }

    @Override
    public void drawToBuffer(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        tris.forEach(tri -> {
            tri.draw(buffer, color, this, position, orientation, cameraPos);
        });
    }

    public void drawToBufferMirrored(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos) {
        Vector3f flippedPosition = position.mul(1, -1, 1, new Vector3f());
        Quaternionf flippedOrientation = new Quaternionf(-orientation.x, orientation.y, -orientation.z, orientation.w);
        tris.forEach(tri -> {
            tri.drawMirrored(buffer, color, this, flippedPosition, flippedOrientation, cameraPos);
        });
    }

    public void addTris(Triangle[] tris) {
        this.tris.addAll(Arrays.asList(tris));
    }
}
