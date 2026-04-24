package com.beatcraft.client.logic;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicsTransform {
    private final Matrix4f turnaround = new Matrix4f();
    private final Matrix4f currentTransform = new Matrix4f();
    private final Matrix4f previousTransform = new Matrix4f();
    private final Vector3f v0 = new Vector3f();
    private final Vector3f v1 = new Vector3f();
    private static final Vector3f ZERO = new Vector3f();

    public PhysicsTransform(float x, float y, float z) {
        v0.set(x, y, z);
        currentTransform.translate(v0);
        previousTransform.translate(v0);
        for (int i = 0; i < WINDOW; ++i) {
            deltaBuffer[i] = new Vector3f();
        }
    }

    private static final int WINDOW = 8;
    private final Vector3f[] deltaBuffer = new Vector3f[WINDOW];
    private int currentDelta = 0;
    private final Vector3f committedAxis = new Vector3f(0, 1, 0);

    private static final float MIN_SWING_SPEED = 1.0f;
    private static final float AXIS_CHANGE_COS = 0.5f;

    private final Quaternionf qPrev = new Quaternionf();
    private final Quaternionf qCurr = new Quaternionf();
    private final Quaternionf qDelta = new Quaternionf();
    private final Vector3f delta = new Vector3f();
    private final Vector3f meanDelta = new Vector3f();
    private final Vector3f axis = new Vector3f();

    public void update(Matrix4f newTransform, float dt) {
        currentTransform.getUnnormalizedRotation(qPrev).normalize();
        newTransform.getUnnormalizedRotation(qCurr).normalize();
        qPrev.conjugate(qDelta);
        qCurr.mul(qDelta, qDelta);

        float halfAngle = (float) Math.acos(Math.max(-1f, Math.min(1f, qDelta.w)));
        float sinHalf = (float) Math.sin(halfAngle);
        delta.set(0);
        if (sinHalf > 1e-6f) {
            float angle = 2f * halfAngle;
            delta.set(qDelta.x / sinHalf, qDelta.y / sinHalf, qDelta.z / sinHalf)
                .normalize().mul(angle / dt);
        }

        deltaBuffer[currentDelta].set(delta);
        currentDelta = (currentDelta + 1) % WINDOW;
        meanDelta.set(0);
        for (var o : deltaBuffer) {
            meanDelta.add(o);
        }
        meanDelta.div(WINDOW);

        float speed = meanDelta.length();
        if (speed < MIN_SWING_SPEED) {
            previousTransform.set(currentTransform);
            currentTransform.set(newTransform);
            return;
        }

        meanDelta.normalize(axis);

        if (axis.dot(committedAxis) < AXIS_CHANGE_COS) {
            turnaround.set(currentTransform);
            committedAxis.set(axis);
        }

        previousTransform.set(currentTransform);
        currentTransform.set(newTransform);
    }

    public Vector3f getPositionalVelocity(float deltaTime, Vector3f offset, Vector3f dest) {
        previousTransform.transformPosition(offset, v0);
        currentTransform.transformPosition(offset, v1);
        return v1.sub(v0, dest).div(deltaTime);
    }

    public void copy(Matrix4f lastMat, Matrix4f currentMat) {
        lastMat.set(previousTransform);
        currentMat.set(currentTransform);
    }

    public Matrix4f copyCurrent(Matrix4f dest) {
        dest.set(currentTransform);
        return dest;
    }

    public Matrix4f getTurnaround(Matrix4f dest) {
        dest.set(turnaround);
        return dest;
    }

    public Vector3f getPosition(Vector3f offset, Vector3f dest) {
        return currentTransform.transformPosition(offset, dest);
    }

    public Vector3f getPosition(Vector3f dest) {
        return getPosition(ZERO, dest);
    }

    public Quaternionf getRotation(Quaternionf dest) {
        return currentTransform.getUnnormalizedRotation(dest);
    }

}
