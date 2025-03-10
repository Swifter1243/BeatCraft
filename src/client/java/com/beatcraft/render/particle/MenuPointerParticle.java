package com.beatcraft.render.particle;

import com.beatcraft.utils.MathUtil;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Vector3f;

import java.util.List;

public class MenuPointerParticle implements Particle {

    private boolean shouldRemove = false;
    private final Vector3f position;
    private final Vector3f normal;

    public MenuPointerParticle(Vector3f position, Vector3f normal) {
        this.position = position;
        this.normal = normal;
    }

    @Override
    public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {
        shouldRemove = true;

        List<Vector3f[]> circle = MathUtil.fillMesh(MathUtil.generateCircle(normal, 0.05f, 8, position.sub(cameraPos, new Vector3f())));

        for (Vector3f[] tri : circle) {
            buffer.vertex(tri[0].x, tri[0].y, tri[0].z).color(0xFFFFFFFF);
            buffer.vertex(tri[1].x, tri[1].y, tri[1].z).color(0xFFFFFFFF);
            buffer.vertex(tri[2].x, tri[2].y, tri[2].z).color(0xFFFFFFFF);
        }

    }

    @Override
    public boolean shouldRemove() {
        return shouldRemove;
    }
}
