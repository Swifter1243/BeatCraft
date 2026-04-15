package com.beatcraft.client.logic;

import com.beatcraft.Beatcraft;
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

    protected float turnaroundAngleThreshold = -0.75f;
    protected float turnaroundRestThreshold = 0.0005f;
    protected float turnaroundSpeedUpFactor = 0.01f;

    public void update(Matrix4f newTransform, float dt) {
        Vector3f prevTip = previousTransform.transformPosition(new Vector3f(0, 1, 0));
        Vector3f currTip = currentTransform.transformPosition(new Vector3f(0, 1, 0));
        Vector3f newTip = newTransform.transformPosition(new Vector3f(0, 1, 0));

        Vector3f prevDelta = currTip.sub(prevTip, new Vector3f());
        Vector3f currDelta = currTip.sub(newTip, new Vector3f());

        float prevSpeed = prevDelta.length();
        float currSpeed = currDelta.length();

        // Beatcraft.LOGGER.info("dt, speeds: {}, {}, {}", dt, prevSpeed, currSpeed);
        if (currSpeed > 0.2f * dt && prevSpeed > 0.0001f * dt) {
            float dot = prevDelta.normalize(new Vector3f()).dot(currDelta.normalize(new Vector3f()));
            // Beatcraft.LOGGER.info("what");
            if (dot < turnaroundAngleThreshold) {
                // Beatcraft.LOGGER.info("update from turnaround");
                turnaround.set(currentTransform);
            }
        }

        if (currSpeed < 0.5f * dt) {
            boolean speedUpFromRest = prevSpeed < turnaroundRestThreshold * dt
                && currSpeed > turnaroundSpeedUpFactor;
            if (speedUpFromRest) {
                // Beatcraft.LOGGER.info("Speed up from rest! {} -> {} (dt={})", prevSpeed, currSpeed, dt);
                // turnaround.set(currentTransform);
            }
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
