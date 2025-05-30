package com.beatcraft.render.particle;

import com.beatcraft.render.instancing.SmokeInstanceData;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SmokeParticle implements Particle {

    private final Quaternionf spin;
    private static final float LIFETIME = 16;
    public static final float SPAWN_INTERVAL = 5;
    private static final float SPIN_SPEED = 0.05f * MathHelper.RADIANS_PER_DEGREE;

    private final double spawnTime;
    private final Quaternionf orientation = new Quaternionf();

    public SmokeParticle(Random random) {
        var axis = new Vector3f(
            random.nextBetween(-100, 100) / 100f,
            random.nextBetween(-100, 100) / 100f,
            random.nextBetween(-100, 100) / 100f
        ).normalize();
        orientation.rotationAxis(
            random.nextBetween(-100, 100) / 100f,
            new Vector3f(random.nextBetween(-100, 100) / 100f,
            random.nextBetween(-100, 100) / 100f,
            random.nextBetween(-100, 100) / 100f).normalize()
        );
        spawnTime = System.nanoTime() / 1_000_000_000d;
        this.spin = new Quaternionf().rotationAxis(SPIN_SPEED, axis);
    }


    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
        orientation.mul(spin.scale(deltaTime, new Quaternionf()).normalize());
        double t = System.nanoTime() / 1_000_000_000d;
        float delta = (float) MathUtil.inverseLerp(spawnTime, spawnTime+LIFETIME, t);
        MeshLoader.SMOKE_INSTANCED_MESH.draw(new SmokeInstanceData(orientation, cameraPos, delta));
    }

    @Override
    public boolean shouldRemove() {
        double t = System.nanoTime() / 1_000_000_000d;
        return (t - spawnTime) > LIFETIME;
    }
}
