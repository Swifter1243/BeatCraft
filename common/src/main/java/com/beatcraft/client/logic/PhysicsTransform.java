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
    private final Quaternionf q0 = new Quaternionf();
    private final Quaternionf q1 = new Quaternionf();
    private static final Vector3f ZERO = new Vector3f();

    public PhysicsTransform(float x, float y, float z) {
        v0.set(x, y, z);
        currentTransform.translate(v0);
        previousTransform.translate(v0);
    }

    protected float turnaroundVelocityThreshold = 0.05f;

    public void update(Matrix4f newTransform, float dt) {
        Vector3f prevTip = new Vector3f(
            previousTransform.m30() + previousTransform.m10(),
            previousTransform.m31() + previousTransform.m11(),
            previousTransform.m32() + previousTransform.m12()
        );
        Vector3f currTip = new Vector3f(
            newTransform.m30() + newTransform.m10(),
            newTransform.m31() + newTransform.m11(),
            newTransform.m32() + newTransform.m12()
        );

        Vector3f tipDelta = currTip.sub(prevTip, new Vector3f());
        float tipSpeed = tipDelta.length() / dt;

        if (tipSpeed < turnaroundVelocityThreshold) {
            turnaround.set(newTransform);
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
