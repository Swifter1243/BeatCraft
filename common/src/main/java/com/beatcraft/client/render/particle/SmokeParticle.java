package com.beatcraft.client.render.particle;

import com.beatcraft.client.render.instancing.SmokeInstanceData;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.common.utils.MathUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SmokeParticle implements Particle {

    private final Quaternionf spin;
    private static final float LIFETIME = 16;
    public static final float SPAWN_INTERVAL = 5;
    private static final float SPIN_SPEED = 0.05f * Mth.DEG_TO_RAD;

    private final double spawnTime;
    private final Quaternionf orientation = new Quaternionf();

    public SmokeParticle(RandomSource random) {
        var axis = new Vector3f(
            random.nextInt(-100, 100) / 100f,
            random.nextInt(-100, 100) / 100f,
            random.nextInt(-100, 100) / 100f
        ).normalize();
        orientation.rotationAxis(
            random.nextInt(-100, 100) / 100f,
            new Vector3f(random.nextInt(-100, 100) / 100f,
            random.nextInt(-100, 100) / 100f,
            random.nextInt(-100, 100) / 100f).normalize()
        );
        spawnTime = System.nanoTime() / 1_000_000_000d;
        this.spin = new Quaternionf().rotationAxis(SPIN_SPEED, axis);
    }


    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
        orientation.mul(spin.scale(deltaTime, new Quaternionf()).normalize());
        double t = System.nanoTime() / 1_000_000_000d;
        float delta = (float) MathUtil.inverseLerp(spawnTime, spawnTime+LIFETIME, t);
        MeshLoader.SMOKE_INSTANCED_MESH.draw(SmokeInstanceData.create(orientation, cameraPos, delta));
    }

    @Override
    public boolean shouldRemove() {
        double t = System.nanoTime() / 1_000_000_000d;
        return (t - spawnTime) > LIFETIME;
    }
}
