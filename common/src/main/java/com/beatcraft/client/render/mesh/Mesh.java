package com.beatcraft.client.render.mesh;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface Mesh {
    void drawToBuffer(BufferBuilder buffer, Vector3f position, Quaternionf orientation, Vector3f cameraPos);
}