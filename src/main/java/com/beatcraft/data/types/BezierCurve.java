package com.beatcraft.data.types;

import com.beatcraft.memory.MemoryPool;
import org.joml.Vector3f;

import java.util.List;

public class BezierCurve implements ISplinePath {

    private final Vector3f p0;
    private final Vector3f p1;
    private final Vector3f p2;

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

        return MemoryPool.newVector3f(x, y, z);
    }

    public Vector3f getTangent(float t) {

        float x = 2f * (1f - t) * (p1.x - p0.x) + 2f * t * (p2.x - p1.x);
        float y = 2f * (1f - t) * (p1.y - p0.y) + 2f * t * (p2.y - p1.y);
        float z = 2f * (1f - t) * (p1.z - p0.z) + 2f * t * (p2.z - p1.z);

        return MemoryPool.newVector3f(x, y, z);
    }

    @Override
    public List<Vector3f> getControlPoints() {
        return List.of(p0, p1, p2);
    }
}
