package com.beatcraft.render.particle;

import com.beatcraft.data.types.Color;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Vector3f;

import java.util.List;

public class SparkParticle implements Particle {

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

        List<Vector3f[]> vertices = MathUtil.fillMesh(
                MathUtil.generateCircle(normal, size / 2f, 3, position.sub(cameraPos, new Vector3f()), 270, 0)
        );
        position.add(velocity);
        velocity.mul(decay);
        velocity.y -= 0.000002f;

        velocity = BeatcraftParticleRenderer.applyVariance(velocity, 0.1f, 0.01f);

        float delta = (float) ((MathUtil.inverseLerp(this.spawnTime, this.spawnTime + this.lifetime, System.nanoTime() / 1_000_000_000d)));

        if (delta >= 1) return;

        int col = MathUtil.lerpColor(new Color(color), new Color(0x01FFFFFF), delta).toARGB();

        for (Vector3f[] vert : vertices) {
            buffer.vertex(vert[0].x, vert[0].y, vert[0].z).color(col);
            buffer.vertex(vert[1].x, vert[1].y, vert[1].z).color(col);
            buffer.vertex(vert[2].x, vert[2].y, vert[2].z).color(col);
        }

    }

    public boolean shouldRemove() {
        return (this.spawnTime + this.lifetime) < System.nanoTime() / 1_000_000_000d;
    }

}
