package com.beatcraft.render.mesh;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface Mesh {
    BufferBuilder createBuffer();
    void drawToBuffer(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos);
    void render(Vector3f position, Quaternionf orientation, boolean sortBuffer);
}
