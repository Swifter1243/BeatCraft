package com.beatcraft.logic;

import com.beatcraft.render.object.PhysicalColorNote;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Math;

import java.util.ArrayDeque;
import java.util.Deque;


public class SwingState {

    private final Deque<Pair<Float, PhysicalColorNote>> followThroughNotes = new ArrayDeque<>();
    private final Deque<Pair<Float, PhysicalColorNote>> followThoughTemp = new ArrayDeque<>();

    private final Vector3f lastPosition = new Vector3f();
    private final Quaternionf lastRotation = new Quaternionf();

    private final Vector3f lastVelocity = new Vector3f();
    private float swingAngle = 0f;

    public SwingState() {

    }

    public Vector3f getVelocity(Vector3f currentPosition, Quaternionf currentRotation, double deltaTime) {
        Vector3f a = new Vector3f(0, 1, 0).rotate(currentRotation).add(currentPosition);
        Vector3f b = new Vector3f(0, 1, 0).rotate(lastRotation).add(lastPosition);

        return new Vector3f(a).sub(b).mul(1/(float) deltaTime).normalize();
    }

    private void processFollowThrough() {
        float t = (float) System.nanoTime() / 1_000_000_000f;

        while (!followThroughNotes.isEmpty()) {
            var pair = followThroughNotes.pop();

            if (t - pair.getLeft() > 0.3 ) {
                pair.getRight().getCutResult().setFollowThroughAngle((int) swingAngle - pair.getRight().getCutResult().getPreSwingAngle());
                continue;
            }

            if (swingAngle - pair.getRight().getCutResult().getPreSwingAngle() >= 60) {
                pair.getRight().getCutResult().setFollowThroughAngle(60);
            }

            followThoughTemp.add(pair);
        }

        followThroughNotes.addAll(followThoughTemp);
        followThoughTemp.clear();

    }

    public void updateSaber(Vector3f position, Quaternionf orientation, double deltaTime) {
        Vector3f currentVelocity = getVelocity(position, orientation, deltaTime);

        Quaternionf cv = new Quaternionf().lookAlong(currentVelocity, new Vector3f(0, 0, 1)).normalize();
        Quaternionf lv = new Quaternionf().lookAlong(lastVelocity, new Vector3f(0, 0, 1)).normalize();

        float diff = (2.0f * Math.safeAcos(Math.min(Math.abs(cv.dot(lv)), 1f))) * MathHelper.DEGREES_PER_RADIAN;

        swingAngle += angleBetween(orientation, lastRotation);

        if (diff > 46) {
            swingAngle = 0;
        }

        lastVelocity.set(currentVelocity);

        processFollowThrough();

    }

    // returns the swing angle in degrees
    public static float angleBetween(Quaternionf q1, Quaternionf q2) {
        Quaternionf delta = new Quaternionf(q2).add(q1.invert(new Quaternionf()));
        return (float) Math.abs(Math.toDegrees(delta.angle()));
    }

    public float getSwingAngle() {
        return swingAngle;
    }

    public void followThrough(PhysicalColorNote colorNote) {
        followThroughNotes.add(new Pair<>((float) System.nanoTime()/1_000_000_000f, colorNote));
    }

}
