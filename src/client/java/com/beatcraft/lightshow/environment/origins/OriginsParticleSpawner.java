package com.beatcraft.lightshow.environment.origins;

import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.lights.ParticleCloudLight;
import com.beatcraft.render.particle.Particle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class OriginsParticleSpawner implements ParticleCloudLight.CloudParticleSpawner {

    protected static final Random random = Random.create();

    public static class BarParticle implements Particle {

        private Vector3f position;
        private Quaternionf orientation;
        private Vector3f velocity;
        private Vector2f dimensions;
        private LightState sharedLightState;
        private double spawnTime;

        private List<Vector3f[]> tris;

        public BarParticle(Vector3f position, Quaternionf orientation, Vector3f velocity, LightState lightState) {
            this.velocity = velocity;
            this.position = position;
            this.orientation = orientation;
            sharedLightState = lightState;
            this.dimensions = new Vector2f(
                (random.nextBetween(1, 8) / 64f),
                (random.nextBetween(2, 7) / 2f)
            );
            this.spawnTime = System.nanoTime() / 1_000_000_000d;

            tris = BeatCraftRenderer.getGlowingQuadAsTris(dimensions, 0.025f);
        }

        @Override
        public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
            if (HUDRenderer.scene != HUDRenderer.MenuScene.InGame) return;

            var t = System.nanoTime() / 1_000_000_000d;
            var dt = t - spawnTime;

            float a = 0;
            if (dt <= 1) {
                a = (float) dt;           // fade in
            } else if (dt <= 4) {
                a = 1;                    // full visible
            } else if (dt <= 5) {
                a = 1 - ((float) dt - 4); // fade out
            }

            int baseColor = sharedLightState.getEffectiveColor();
            int originalAlpha = (baseColor >> 24) & 0xFF;
            int scaledAlpha = Math.round(a * originalAlpha);
            int c = (scaledAlpha << 24) | (baseColor & 0x00FFFFFF);
            int c2 = (c & 0x00FFFFFF) | (1 << 24);  // dim fallback color for glow border


            for (var tri : tris) {
                var v0 = MemoryPool.newVector3f(tri[0].x, 0, tri[0].y).rotate(orientation).add(position).sub(cameraPos);
                var v1 = MemoryPool.newVector3f(tri[1].x, 0, tri[1].y).rotate(orientation).add(position).sub(cameraPos);
                var v2 = MemoryPool.newVector3f(tri[2].x, 0, tri[2].y).rotate(orientation).add(position).sub(cameraPos);

                buffer.vertex(v0).color(tri[0].z < 0.5f ? c2 : c);
                buffer.vertex(v1).color(tri[1].z < 0.5f ? c2 : c);
                buffer.vertex(v2).color(tri[2].z < 0.5f ? c2 : c);


                MemoryPool.release(v0, v1, v2);
            }


            var v = velocity.mul(deltaTime, MemoryPool.newVector3f());
            position.add(v);
            MemoryPool.release(v);
        }

        @Override
        public boolean shouldRemove() {
            var t = System.nanoTime() / 1_000_000_000d;
            var dt = t - spawnTime;
            return dt >= 5;
        }
    }

    @Override
    public Particle spawn(Vector3f position, Quaternionf orientation, Vector3f delta, float lifetime, LightState sharedLightState) {
        return new BarParticle(
            position, orientation, delta.mul((random.nextBetween(-100, 100) / 100f), new Vector3f()), sharedLightState
        );
    }
}
