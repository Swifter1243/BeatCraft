package com.beatcraft.data.types;

import org.joml.Vector3f;

public class BezierCurve {

    private Vector3f p0;
    private Vector3f p1;
    private Vector3f p2;

    public BezierCurve(Vector3f p0, Vector3f p1, Vector3f p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Vector3f evaluate(float t) {
        float num = 1f - t;

        float x = num * num * p0.x + 2f * num * t * p1.x + t * t * p2.x;
        float y = num * num * p0.y + 2f * num * t * p1.y + t * t * p2.y;
        float z = num * num * p0.z + 2f * num * t * p1.z + t * t * p2.z;

        return new Vector3f(x, y, z);
    }

    public Vector3f getTangent(float t) {

        float x = 2f * (1f - t) * (p1.x - p0.x) + 2f * t * (p2.x - p1.x);
        float y = 2f * (1f - t) * (p1.y - p0.y) + 2f * t * (p2.y - p1.y);
        float z = 2f * (1f - t) * (p1.z - p0.z) + 2f * t * (p2.z - p1.z);

        return new Vector3f(x, y, z);
    }

}
