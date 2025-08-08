package com.beatcraft.client.render.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Vector3f;

public interface Particle {
    void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos);
    boolean shouldRemove();
}
