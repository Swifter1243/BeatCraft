package com.beatcraft.render.particle;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Vector3f;

public interface Particle {
    void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos);
    boolean shouldRemove();
}
