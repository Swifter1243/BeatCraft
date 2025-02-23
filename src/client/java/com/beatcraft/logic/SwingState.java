package com.beatcraft.logic;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.object.PhysicalScorableObject;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Math;

import java.util.ArrayDeque;
import java.util.Deque;


public class SwingState {

    private final Deque<Pair<Double, PhysicalScorableObject>> followThroughNotes = new ArrayDeque<>();
    private final Deque<Pair<Double, PhysicalScorableObject>> followThoughTemp = new ArrayDeque<>();

    private final Vector3f lastPosition = new Vector3f();
    private final Quaternionf lastRotation = new Quaternionf();

    private final Vector3f endPoint = new Vector3f(0, 1, 0);
    private final Vector3f lastVelocity = new Vector3f();
    private float swingAngle = 0f;

    private double cutTime = 0f;
    private boolean recentCut = false;

    private NoteType color;

    public SwingState(NoteType color) {
        this.color = color;
    }

    public Vector3f getVelocity(Vector3f currentPosition, Quaternionf currentRotation, double deltaTime) {

        Vector3f a = new Vector3f(0, 1, 0).rotate(currentRotation).add(currentPosition);
        endPoint.set(a);
        Vector3f b = new Vector3f(0, 1, 0).rotate(lastRotation).add(lastPosition);

        Vector3f velocity = new Vector3f(a).sub(b).mul((float) deltaTime);

        if (DebugRenderer.doDebugRendering && DebugRenderer.debugSaberRendering) {
            DebugRenderer.renderLine(a, a.add(velocity, new Vector3f()), 0xFFFF0000, 0xFF0000FF);
        }

        return velocity;
    }

    private void processFollowThrough() {
        double t = (double) System.nanoTime() / 1_000_000_000d;

        while (!followThroughNotes.isEmpty()) {
            var pair = followThroughNotes.pop();

            if (t - pair.getLeft() > 0.3 ) {
                pair.getRight().score$getCutResult().setFollowThroughAngle((int) swingAngle - pair.getRight().score$getCutResult().getPreSwingAngle());
                pair.getRight().score$getCutResult().finalizeScore();
                pair.getRight().score$cutNote();
                continue;
            }

            if (swingAngle - pair.getRight().score$getCutResult().getPreSwingAngle() >= 60) {
                pair.getRight().score$getCutResult().setFollowThroughAngle(60);
                pair.getRight().score$getCutResult().finalizeScore();
                pair.getRight().score$cutNote();
            }

            followThoughTemp.add(pair);
        }

        followThroughNotes.addAll(followThoughTemp);
        followThoughTemp.clear();

    }

    private void createSparks(Vector3f pos, Vector3f velocity) {
        //BeatCraft.LOGGER.info("Making sparks at {} {}", pos, velocity);
        int col = this.color == NoteType.RED ? BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB() : BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        BeatcraftParticleRenderer.spawnSparkParticles(pos, velocity.mul(0.1f, new Vector3f()), 0.2f, 0.03f, GameLogicHandler.random.nextInt(5, 15), col, 0.02f);
    }

    public void updateSaber(Vector3f position, Quaternionf orientation, double deltaTime) {
        Vector3f currentVelocity = getVelocity(position, orientation, deltaTime);

        if (recentCut) {
            double t = System.nanoTime() / 1_000_000_000d;
            if (t-0.15 < cutTime) {
                createSparks(endPoint, currentVelocity);
            } else {
                recentCut = false;
            }
        }

        Quaternionf cv = new Quaternionf().lookAlong(currentVelocity, new Vector3f(0, 0, 1)).normalize();
        Quaternionf lv = new Quaternionf().lookAlong(lastVelocity, new Vector3f(0, 0, 1)).normalize();

        float diff = (2.0f * Math.safeAcos(Math.min(Math.abs(cv.dot(lv)), 1f))) * MathHelper.DEGREES_PER_RADIAN;

        swingAngle += angleBetween(orientation, lastRotation);

        if (diff > 46) {
            swingAngle = 0;
        }

        lastVelocity.set(currentVelocity);
        lastPosition.set(position);
        lastRotation.set(orientation);

        processFollowThrough();

    }

    public void startSparkEffect() {
        cutTime = System.nanoTime() / 1_000_000_000d;
        recentCut = true;
    }

    // returns the swing angle in degrees
    public static float angleBetween(Quaternionf q1, Quaternionf q2) {
        Quaternionf delta = new Quaternionf(q2).add(q1.invert(new Quaternionf()));
        return (float) Math.abs(Math.toDegrees(delta.angle()));
    }

    public float getSwingAngle() {
        return swingAngle;
    }

    public void followThrough(PhysicalScorableObject colorNote) {
        followThroughNotes.add(new Pair<>((double) System.nanoTime()/1_000_000_000d, colorNote));
    }

}
