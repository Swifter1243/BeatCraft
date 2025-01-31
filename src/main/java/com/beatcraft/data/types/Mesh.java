package com.beatcraft.data.types;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Mesh {

    public ArrayList<Vector3f> vertices;
    public ArrayList<int[]> quads;

    public Mesh(List<Vector3f> vertices, List<int[]> quads) {
        this.vertices = new ArrayList<>(vertices);
        this.quads = new ArrayList<>(quads);
    }

    public Mesh() {
        this.vertices = new ArrayList<>();
        this.quads = new ArrayList<>();
    }

    public List<Vector3f[]> getPositionedQuads() {
        ArrayList<Vector3f[]> out = new ArrayList<>();

        for (int[] quad : quads) {
            out.add(new Vector3f[]{
                this.vertices.get(quad[0]),
                this.vertices.get(quad[1]),
                this.vertices.get(quad[2]),
                this.vertices.get(quad[3])
            });
        }

        return out;
    }

}
