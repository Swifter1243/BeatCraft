package com.beatcraft.render.particle;

import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.mesh.TriangleMesh;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Debris implements Particle {

    private final TriangleMesh mesh;
    private final Vector3f position;
    private final Quaternionf orientation;
    private final Vector3f velocity;
    private final Vector3f decay;
    private final Quaternionf spin;
    private final double spawnTime;

    public Debris(Vector3f position, Quaternionf orientation, Vector3f velocity, Quaternionf spin, TriangleMesh mesh) {
        this.mesh = mesh;
        this.position = position;
        this.velocity = velocity;
        this.orientation = orientation;
        this.spin = spin;
        this.decay = new Vector3f(0.99f, 0.99f, 0.99f);
        this.spawnTime = System.nanoTime() / 1_000_000_000d;
    }

    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
        orientation.add(spin.mul(deltaTime, new Quaternionf()));
        position.add(velocity.mul(deltaTime, new Vector3f()));
        velocity.add(new Vector3f(0, -9.81f, 0).mul(deltaTime));
        velocity.mul(decay);

        BeatcraftRenderer.recordNoteRenderCall((tri, quad, cam) -> {
            if (tri == null) return;
            mesh.drawToBuffer(tri, position, orientation, cam);
        });

    }

    @Override
    public boolean shouldRemove() {
        return MathUtil.inverseLerp(spawnTime, spawnTime+5f, System.nanoTime()/1_000_000_000d) >= 1;
    }
}
