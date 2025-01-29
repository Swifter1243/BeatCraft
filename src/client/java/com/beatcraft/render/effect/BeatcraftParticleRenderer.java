package com.beatcraft.render.effect;

import com.beatcraft.utils.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

public class BeatcraftParticleRenderer {

    private static class SparkParticle implements Particle {

        public Vector3f position;
        public Vector3f velocity;
        public int color;
        public float size;
        public float lifetime;
        public double spawnTime;
        public float decay;

        public SparkParticle(Vector3f position, Vector3f velocity, int color, float size, float lifetime, float decay) {
            this.position = position;
            this.velocity = velocity;
            this.color = color;
            this.size = size;
            this.lifetime = lifetime;
            this.spawnTime = System.nanoTime() / 1_000_000_000d;
            this.decay = decay;
        }

        public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
            Vector3f normal = new Vector3f(position).sub(cameraPos).normalize();

            Vector3f[] vertices = MathUtil.generateCircle(normal, size/2f, 4, position.sub(cameraPos, new Vector3f()));
            position.add(velocity);
            velocity.mul(decay);
            velocity.y -= 0.000002f;

            velocity = applyVariance(velocity, 0.1f, 0.01f);

            int opacity = (int) ((1 - MathUtil.inverseLerp(this.spawnTime, this.spawnTime+this.lifetime, System.nanoTime() / 1_000_000_000d)) * 255) << 24;

            if (opacity <= 0) return;

            for (Vector3f vert : vertices) {
                buffer.vertex(vert.x, vert.y, vert.z).color(color + opacity);
            }

        }

        public boolean shouldRemove() {
            return (this.spawnTime + this.lifetime) < System.nanoTime() / 1_000_000_000d;
        }

    }

    private static final ArrayList<Particle> particles = new ArrayList<>();

    private static double lastUpdateTime = 0d;
    private static final Random random = new Random();


    public static Vector3f applyVariance(Vector3f velocity, float variance, float magnitudeVariance) {

        magnitudeVariance = Math.abs(magnitudeVariance);

        float var = magnitudeVariance == 0 ? 0 : random.nextFloat(-magnitudeVariance, magnitudeVariance);

        if (velocity.equals(0, 0, 0)) {
            velocity = new Vector3f(0, var, 0);
            variance = 1.0f;
        }

        if (variance <= 0.0f) {
            return new Vector3f(velocity).normalize().mul(velocity.length() * (1+var));
        }

        if (variance >= 1.0f) {
            return randomDirection(velocity.length());
        }

        Vector3f normalized = new Vector3f(velocity).normalize();

        Vector3f randomAxis = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();

        float maxAngle = variance * (float) Math.PI;
        float randomAngle = (random.nextFloat() - 0.5f) * 2 * maxAngle;

        Vector3f result = new Vector3f(normalized).rotateAxis(randomAngle, randomAxis.x, randomAxis.y, randomAxis.z);

        result.mul(velocity.length() * (1f + var));

        return result;
    }

    private static Vector3f randomDirection(float magnitude) {
        float theta = (float) (2 * Math.PI * random.nextFloat());
        float phi = (float) Math.acos(2 * random.nextFloat() - 1);

        float x = (float) (Math.sin(phi) * Math.cos(theta));
        float y = (float) (Math.sin(phi) * Math.sin(theta));
        float z = (float) Math.cos(phi);

        return new Vector3f(x, y, z).mul(magnitude);
    }


    public static void spawnSparkParticles(Vector3f position, Vector3f velocity, float velocityVariance, float magnitudeVariance, int particleCount, int color, float size) {
        for (int i = 0; i < particleCount; i++) {

            SparkParticle particle = new SparkParticle(new Vector3f(position), applyVariance(velocity.mul(0.075f), velocityVariance, magnitudeVariance), color, size, random.nextFloat(0.15f, 0.45f), 0.92f);
            particles.add(particle);
        }
    }

    public static void addParticle(Particle particle) {
        particles.add(particle);
    }

    public static void renderParticles() {
        if (particles.isEmpty()) return;

        double t = System.nanoTime() / 1_000_000_000d;
        float dt = (float) (t - lastUpdateTime);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        particles.forEach(p -> p.update(dt, buffer, cameraPos));
        lastUpdateTime = t;
        particles.removeIf(Particle::shouldRemove);

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        BufferRenderer.drawWithGlobalProgram(buff);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();


    }

}
