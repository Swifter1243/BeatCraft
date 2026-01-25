package com.beatcraft.client.render.lights;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.client.render.particle.Particle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class ParticleCloudLight extends LightObject {

    public interface CloudParticleSpawner {
        Particle spawn(Vector3f position, Quaternionf orientation, Vector3f delta, float lifetime, LightState sharedLightState);
    }

    private final RandomSource random = RandomSource.create();

    private Hitbox spawnRegion;
    private float density; // 0-1
    private float minParticleLifetime;
    private float maxParticleLifetime;
    private Vector3f particleDelta;
    private final ArrayList<CloudParticleSpawner> particleSpawners = new ArrayList<>();

    private float regionVolume;
    private float averageLifetime;

    public ParticleCloudLight(BeatmapController map, Vector3f position, Quaternionf orientation, Hitbox spawnRegion, float density, float minParticleLifetime, float maxParticleLifetime, Vector3f particleDelta) {
        super(map);
        this.position = map.worldPosition.add(position.rotateY(map.worldAngle), position);
        this.orientation = new Quaternionf().rotationY(map.worldAngle).mul(orientation);
        this.spawnRegion = spawnRegion;
        this.density = density;
        this.minParticleLifetime = minParticleLifetime;
        this.maxParticleLifetime = maxParticleLifetime;
        this.particleDelta = particleDelta;

        regionVolume = spawnRegion.getVolume();
        averageLifetime = (minParticleLifetime + maxParticleLifetime) / 2f;
        pt = System.nanoTime() / 1_000_000_000f;
        t = pt;
    }

    public void addParticleSpawners(CloudParticleSpawner... spawners) {
        particleSpawners.addAll(Arrays.asList(spawners));
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        var cloud = new ParticleCloudLight(mapController, position, orientation, spawnRegion, density, minParticleLifetime, maxParticleLifetime, particleDelta);
        cloud.particleSpawners.addAll(particleSpawners);
        cloud.position.add(offset);
        return cloud;
    }

    float pt = 0;
    float t = 0;
    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
        pt = t;
        t = (System.nanoTime() / 1_000_000_000f);
        var dt = t - pt;

        if (dt > 0.35f) return;

        float expectedParticles = (density * regionVolume / averageLifetime) * dt;
        int count = (int) expectedParticles;

        if (random.nextFloat() < (expectedParticles - count)) {
            count += 1;
        }

        for (int i = 0; i < count; i++) {
            var index = random.nextInt(0, particleSpawners.size());
            var spawner = particleSpawners.get(index);

            var px = Mth.lerp(random.nextFloat(), spawnRegion.min.x, spawnRegion.max.x);
            var py = Mth.lerp(random.nextFloat(), spawnRegion.min.y, spawnRegion.max.y);
            var pz = Mth.lerp(random.nextFloat(), spawnRegion.min.z, spawnRegion.max.z);

            var p = spawner.spawn(
                new Vector3f(px, py, pz).rotate(orientation).add(position),
                new Quaternionf(orientation),
                particleDelta.rotate(orientation, new Vector3f()),
                Mth.lerp(random.nextFloat(), minParticleLifetime, maxParticleLifetime),
                lightState
            );

            BeatcraftParticleRenderer.addParticle(p);
        }


    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }

}
