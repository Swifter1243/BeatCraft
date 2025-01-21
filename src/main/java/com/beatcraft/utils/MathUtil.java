package com.beatcraft.utils;

import org.joml.*;
import org.joml.Math;

public class MathUtil {
    public static final float DEG2RAD = (float)Math.PI / 180f;
    public static final float RAD2DEG = 180f / (float)Math.PI;

    public static float inverseLerp(float a, float b, float t) {
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

    public static Vector3f lerpVector3(Vector3f a, Vector3f b, float time) {
        return new Vector3f(a).lerp(b, time);
    }

    public static Vector4f lerpVector4(Vector4f a, Vector4f b, float time) {
        return new Vector4f(a).lerp(b, time);
    }

    public static Quaternionf lerpQuaternion(Quaternionf a, Quaternionf b, float time) {
        return new Quaternionf(a).slerp(b, time);
    }

    public static Vector3f[] generateCircle(Vector3f normal, float radius, int pointsCount) {
        normal.normalize();

        Vector3f startPoint = new Vector3f(1, 0, 0);
        if (startPoint.dot(normal) > 0.99f) {
            startPoint.set(0, 1, 0);
        }
        startPoint.cross(normal).normalize().mul(radius);

        Vector3f[] points = new Vector3f[pointsCount];
        Quaternionf rotation = new Quaternionf();

        for (int i = 0; i < pointsCount; i++) {
            float angle = (float) (2 * Math.PI * i / pointsCount);
            rotation.fromAxisAngleRad(normal.x, normal.y, normal.z, angle);
            points[i] = new Vector3f(startPoint).rotate(rotation);
        }

        return points;
    }

}
