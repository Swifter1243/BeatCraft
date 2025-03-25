package com.beatcraft.utils;

import com.beatcraft.data.types.Color;
import com.beatcraft.memory.MemoryPool;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathUtil {
    public static final float DEG2RAD = (float)Math.PI / 180f;
    public static final float RAD2DEG = 180f / (float)Math.PI;

    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }
    public static double inverseLerp(double a, double b, double t) {
        return (t - a) / (b - a);
    }
    public static float clamp01(float x) {
        return Math.clamp(0, 1, x);
    }
    public static float secondsToBeats(float seconds, float bpm) {
        return seconds * (bpm / 60);
    }
    public static float beatsToSeconds(float beats, float bpm) {
        return beats * (60 / bpm);
    }
    public static Quaternionf eulerToQuaternion(Vector3f euler) {
        Quaternionf q = new Quaternionf();
        q.rotateY(euler.y * DEG2RAD);
        q.rotateX(euler.x * DEG2RAD);
        q.rotateZ(euler.z * DEG2RAD);
        return q;
    }
    public static float normalizeAngle(float angle) {
        angle = angle % 360;
        return angle < 0 ? (angle + 360) : angle;
    }
    public static float degreesBetween(float a, float b) {
        a = normalizeAngle(a);
        b = normalizeAngle(b);
        float difference = Math.abs(a - b);
        return Math.min(difference, 360 - difference);
    }
    public static float getVectorAngleDegrees(Vector2f v) {
        float radians = Math.atan2(v.y, v.x);
        return (float) Math.toDegrees(radians);
    }
    public static void reflectMatrixAcrossX(Matrix4f matrix) {
        // reflect position (x * -1)
        matrix.m30(matrix.m30() * -1);
        // reflect rotation (R * M * R)
        matrix.m10(matrix.m10() * -1);
        matrix.m20(matrix.m20() * -1);
        matrix.m01(matrix.m01() * -1);
        matrix.m02(matrix.m02() * -1);
    }
    public static Vector3f matrixTransformPoint3D(Matrix4f matrix, Vector3f point) {
        Vector4f newPoint = new Vector4f(point, 1);
        newPoint.mul(matrix);
        return new Vector3f(newPoint.x, newPoint.y, newPoint.z);
    }

    public static Vector2f lerpVector2(Vector2f a, Vector2f b, float time) {
        return new Vector2f(a).lerp(b, time);
    }

    public static Vector3f lerpVector3(Vector3f a, Vector3f b, float time) {
        return new Vector3f(a).lerp(b, time);
    }

    public static Vec3i lerpVec3i(Vec3i a, Vec3i b, float time) {
        return new Vec3i(
                (int) Math.lerp(a.getX(), b.getX(), time),
                (int) Math.lerp(a.getY(), b.getY(), time),
                (int) Math.lerp(a.getZ(), b.getZ(), time)
        );
    }

    public static float inverseLerpVector3(Vector3f a, Vector3f b, Vector3f t) {
        if (a.x != b.x) {
            return inverseLerp(a.x, b.x, t.x);
        } else if (a.y != b.y) {
            return inverseLerp(a.y, b.y, t.y);
        } else {
            return inverseLerp(a.z, b.z, t.z);
        }
    }

    public static Vector4f lerpVector4(Vector4f a, Vector4f b, float time) {
        return new Vector4f(a).lerp(b, time);
    }

    public static Quaternionf lerpQuaternion(Quaternionf a, Quaternionf b, float time) {
        return new Quaternionf(a).slerp(b, time);
    }

    public static Vector3f[] generateCircle(Vector3f normal, float radius, int pointsCount, Vector3f offset) {
        return generateCircle(normal, radius, pointsCount, offset, 360f, 0);
    }

    public static Vector3f[] generateCircle(Vector3f normal, float radius, int pointsCount, Vector3f offset, float arcDegrees, float angleOffset) {
        normal.normalize();

        Vector3f startPoint = new Vector3f(1, 0, 0);
        if (startPoint.dot(normal) > 0.99f) {
            startPoint.set(0, 1, 0);
        }
        startPoint.cross(normal).normalize().mul(radius);

        Vector3f[] points = new Vector3f[pointsCount+1];
        Quaternionf rotation = new Quaternionf();

        for (int i = 0; i <= pointsCount; i++) {
            float angle = ((arcDegrees * MathHelper.RADIANS_PER_DEGREE) * i / pointsCount) + (angleOffset * MathHelper.RADIANS_PER_DEGREE);
            rotation.fromAxisAngleRad(normal.x, normal.y, normal.z, angle);
            points[i] = MemoryPool.newVector3f(startPoint).rotate(rotation).add(offset);
        }

        return points;
    }

    public static Pair<Float, Vector3f> getLineDistance(Vector3f startA, Vector3f endA, Vector3f startB, Vector3f endB) {
        Vector3f distA = new Vector3f(endA).sub(startA);
        Vector3f distB = new Vector3f(endB).sub(startB);
        Vector3f startDiff = new Vector3f(startA).sub(startB);

        float distA2 = distA.dot(distA);
        float distB2 = distB.dot(distB);
        float f = distB.dot(startDiff);

        float modA;
        float modB;

        if (distA2 <= Float.MIN_VALUE && distB2 <= Float.MIN_VALUE) {
            return new Pair<>(startDiff.length(), new Vector3f());
        }

        if (distA2 <= Float.MIN_VALUE) {
            modA = 0.0f;
            modB = Math.max(0.0f, Math.min(1.0f, f / distB2));
        } else {
            float dotA = distA.dot(startDiff);
            if (distB2 <= Float.MIN_VALUE) {
                modB = 0.0f;
                modA = Math.max(0.0f, Math.min(1.0f, -dotA / distA2));
            } else {
                float dotAB = distA.dot(distB);
                float denominator = distA2 * distB2 - dotAB * dotAB;

                if (denominator != 0.0f) {
                    modA = Math.max(0.0f, Math.min(1.0f, (dotAB * f - dotA * distB2) / denominator));
                } else {
                    modA = 0.0f;
                }

                modB = (dotAB * modA + f) / distB2;

                if (modB < 0.0f) {
                    modB = 0.0f;
                    modA = Math.max(0.0f, Math.min(1.0f, -dotA / distA2));
                } else if (modB > 1.0f) {
                    modB = 1.0f;
                    modA = Math.max(0.0f, Math.min(1.0f, (dotAB - dotA) / distA2));
                }
            }
        }

        Vector3f closestA = new Vector3f(distA).mul(modA).add(startA);
        Vector3f closestB = new Vector3f(distB).mul(modB).add(startB);

        return new Pair<>(closestA.distance(closestB), lerpVector3(closestA, closestB, 0.5f));
    }

    public static String timeToString(int t) {
        int minutes = t / 60;
        int seconds = t % 60;

        return String.format("%s:%02d", minutes, seconds);
    }

    public static Color lerpColor(Color a, Color b, float time) {
        return new Color(
                Math.clamp(Math.lerp(a.getRed(), b.getRed(), time), 0, 255),
                Math.clamp(Math.lerp(a.getGreen(), b.getGreen(), time), 0, 255),
                Math.clamp(Math.lerp(a.getBlue(), b.getBlue(), time), 0, 255),
                Math.clamp(Math.lerp(a.getAlpha(), b.getAlpha(), time), 0, 255)
        );
    }

    public static Pair<Vector3f, Vector2f> raycastPlane(Vector3f raycastOrigin, Quaternionf raycastOrientation, Vector3f planeCenter, Quaternionf planeOrientation, Vector2f planeSize) {
        Vector3f planeNormal = new Vector3f(0, 0, 1).rotate(planeOrientation);

        Vector3f rayDirection = new Vector3f(0, 1, 0).rotate(raycastOrientation);

        float denominator = rayDirection.dot(planeNormal);
        if (Math.abs(denominator) < 0.000001) {
            return null;
        }

        float t = planeCenter.sub(raycastOrigin, new Vector3f()).dot(planeNormal) / denominator;
        if (t < 0) {
            return null;
        }

        Vector3f intersection = new Vector3f(raycastOrigin).fma(t, rayDirection);

        Vector3f localPoint = intersection.sub(planeCenter, new Vector3f()).rotate(new Quaternionf(planeOrientation).invert());

        if (Math.abs(localPoint.x) > planeSize.x / 2 || Math.abs(localPoint.y) > planeSize.y / 2) {
            return null;
        }

        return new Pair<>(intersection, new Vector2f(localPoint.x, localPoint.y));
    }

    public static boolean check2DPointCollision(Vector2f point, Vector2f center, Vector2f size) {
        Vector2f minCorner = center.sub(size.mul(0.5f, new Vector2f()), new Vector2f());
        Vector2f maxCorner = center.add(size.mul(0.5f, new Vector2f()), new Vector2f());

        return (minCorner.x <= point.x && point.x <= maxCorner.x && minCorner.y <= point.y && point.y <= maxCorner.y);
    }

    /// Takes an array of vertices and generates a list of triangles that fills the closed loop formed by the vertices.
    /// this method is super scuffed and makes really weird meshes so don't use for non-convex or non-flat shapes
    public static List<Vector3f[]> fillMesh(Vector3f[] vertices) {
        if (vertices.length < 3) {
            return List.of();
        }

        ArrayList<Vector3f> points = new ArrayList<>(Arrays.stream(vertices).toList());

        var p0 = points.removeFirst();
        var p1 = points.removeFirst();

        ArrayList<Vector3f[]> tris = new ArrayList<>();

        for (Vector3f p2 : points) {
            tris.add(new Vector3f[]{p0, p1, p2});
            p1 = p2;
        }
        return tris;
    }

}
