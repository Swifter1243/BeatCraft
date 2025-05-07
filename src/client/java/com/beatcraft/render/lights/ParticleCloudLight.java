package com.beatcraft.render.lights;

import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class ParticleCloudLight extends LightObject {

    public interface CloudParticleSpawner {
        Particle spawn(Vector3f position, Quaternionf orientation, Vector3f delta, float lifetime, LightState sharedLightState);
    }

    private final Random random = Random.create();

    private Hitbox spawnRegion;
    private float density; // 0-1
    private float minParticleLifetime;
    private float maxParticleLifetime;
    private Vector3f particleDelta;
    private final ArrayList<CloudParticleSpawner> particleSpawners = new ArrayList<>();

    private float regionVolume;
    private float averageLifetime;

    public ParticleCloudLight(Vector3f position, Quaternionf orientation, Hitbox spawnRegion, float density, float minParticleLifetime, float maxParticleLifetime, Vector3f particleDelta) {
        this.position = position;
        this.orientation = orientation;
        this.spawnRegion = spawnRegion;
        this.density = density;
        this.minParticleLifetime = minParticleLifetime;
        this.maxParticleLifetime = maxParticleLifetime;
        this.particleDelta = particleDelta;

        regionVolume = spawnRegion.getVolume();
        averageLifetime = (minParticleLifetime + maxParticleLifetime) / 2f;
    }

    public void addParticleSpawners(CloudParticleSpawner... spawners) {
        particleSpawners.addAll(Arrays.asList(spawners));
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        var cloud = new ParticleCloudLight(position, orientation, spawnRegion, density, minParticleLifetime, maxParticleLifetime, particleDelta);
        cloud.particleSpawners.addAll(particleSpawners);
        cloud.position.add(offset);
        return cloud;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        float t = (System.nanoTime() / 1_000_000_000f);

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
