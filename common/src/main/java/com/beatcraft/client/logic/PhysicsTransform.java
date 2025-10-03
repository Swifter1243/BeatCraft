package com.beatcraft.client.logic;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PhysicsTransform {
    private final Matrix4f currentTransform = new Matrix4f();
    private final Matrix4f previousTransform = new Matrix4f();
    private final Vector3f v0 = new Vector3f();
    private final Vector3f v1 = new Vector3f();
    private final Quaternionf q0 = new Quaternionf();
    private final Quaternionf q1 = new Quaternionf();
    private static final Vector3f ZERO = new Vector3f();

    public PhysicsTransform() {

    }

    public void update(Matrix4f newTransform) {
        previousTransform.set(currentTransform);
        currentTransform.set(newTransform);
    }

    public Vector3f getPositionalVelocity(float deltaTime, Vector3f offset, Vector3f dest) {
        previousTransform.transformPosition(offset, v0);
        currentTransform.transformPosition(offset, v1);
        return v1.sub(v0, dest).div(deltaTime);
    }

    public Vector3f getPositionalVelocity(float deltaTime, Vector3f dest) {
        return getPositionalVelocity(deltaTime, ZERO, dest);
    }

    public Vector3f getAngularVelocity(float deltaTime, Vector3f dest) {
        previousTransform.getNormalizedRotation(q0);
        currentTransform.getNormalizedRotation(q1);

        q0.conjugate();
        q1.mul(q0, q1);

        float scale = 2.0f / deltaTime;
        dest.set(q1.x * scale, q1.y * scale, q1.z * scale);

        return dest;
    }


    public Vector3f getPosition(Vector3f offset, Vector3f dest) {
        return currentTransform.transformPosition(offset, dest);
    }

    public Quaternionf getRotation(Quaternionf dest) {
        return currentTransform.getUnnormalizedRotation(dest);
    }

}
