package com.beatcraft.data.types;


import com.beatcraft.memory.MemoryPool;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BezierPath implements ISplinePath {

    private final ArrayList<Vector3f> points = new ArrayList<>();

    public BezierPath(List<Vector3f> points) {
        this.points.addAll(points);
    }

    public Vector3f evaluate(float t) {
        int n = points.size() - 1;
        float x = 0;
        float y = 0;
        float z = 0;

        for (int i = 0; i <= n; i++) {
            float coefficient = binomialCoefficient(n, i)
                * (float) Math.pow(1 - t, n - i)
                * (float) Math.pow(t, i);
            x += coefficient * points.get(i).x;
            y += coefficient * points.get(i).y;
            z += coefficient * points.get(i).z;
        }

        return MemoryPool.newVector3f(x, y, z);
    }

    private static int binomialCoefficient(int n, int k) {
        if (k < 0 || k > n) {
            return 0;
        }
        if (k == 0 || k == n) {
            return 1;
        }
        return binomialCoefficient(n - 1, k - 1) + binomialCoefficient(n - 1, k);
    }

    public Vector3f getTangent(float t) {
        int n = points.size() - 1;
        if (n == 0) {
            return MemoryPool.newVector3f(0, 0, 0);
        }
        float x = 0;
        float y = 0;
        float z = 0;

        for (int i = 0; i < n; i++) {
            float coefficient = n * binomialCoefficient(n - 1, i)
                * (float) Math.pow(1 - t, (n - 1) - i)
                * (float) Math.pow(t, i);
            x += coefficient * (points.get(i + 1).x - points.get(i).x);
            y += coefficient * (points.get(i + 1).y - points.get(i).y);
            z += coefficient * (points.get(i + 1).z - points.get(i).z);
        }

        return MemoryPool.newVector3f(x, y, z);
    }

    @Override
    public List<Vector3f> getControlPoints() {
        return points;
    }
}
