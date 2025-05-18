package com.beatcraft.render.instancing;

import com.beatcraft.data.types.Color;
import net.minecraft.util.Identifier;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import oshi.util.tuples.Triplet;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstancedMesh {
    private static final ArrayList<InstancedMesh> meshes = new ArrayList<>();

    private int vao;
    private int vertexVbo;
    private int instanceVbo;
    private final int vertexCount;

    private ShaderProgram shader;
    private final Identifier shaderName;

    private boolean initialized = false;

    private final Triplet<Vector3f, Vector2f, Vector3f>[] vertices;

    private final List<InstanceData> instances = new ArrayList<>();

    public InstancedMesh(Identifier shaderName, Triplet<Vector3f, Vector2f, Vector3f>[] vertices) {
        this.vertexCount = vertices.length;
        this.shaderName = shaderName;
        meshes.add(this);
        this.vertices = vertices;
    }

    private void init() {
        initialized = true;
        this.shader = new ShaderProgram(shaderName);

        vao = GL30.glGenVertexArrays();
        vertexVbo = GL15.glGenBuffers();
        instanceVbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexCount * 8); // 3 pos + 2 uv + 3 norm
        for (Triplet<Vector3f, Vector2f, Vector3f> v : vertices) {
            Vector3f pos = v.getA();
            Vector2f uv = v.getB();
            Vector3f normal = v.getC();

            vertexBuffer.put(pos.x).put(pos.y).put(pos.z);
            vertexBuffer.put(uv.x).put(uv.y);
            vertexBuffer.put(normal.x).put(normal.y).put(normal.z);
        }
        vertexBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // Vertex attributes: position, uv, normal
        int stride = 8 * Float.BYTES;
        int index = 0;

        // position (location = 0)
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index++, 3, GL11.GL_FLOAT, false, stride, 0L);

        // uv (location = 1)
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index++, 2, GL11.GL_FLOAT, false, stride, 3L * Float.BYTES);

        // normal (location = 2)
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index++, 3, GL11.GL_FLOAT, false, stride, 5L * Float.BYTES);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        int instanceStride = 16 * Float.BYTES + 4 * Float.BYTES; // mat4 + vec4

        // Matrix4f (4 vec4s) at location 3 (3-6)
        for (int i = 0; i < 4; i++) {
            GL20.glEnableVertexAttribArray(index);
            GL20.glVertexAttribPointer(index, 4, GL11.GL_FLOAT, false, instanceStride, (long) i * 16L);
            GL33.glVertexAttribDivisor(index++, 1);
        }

        // Color (vec4) at location 7
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, 4, GL11.GL_FLOAT, false, instanceStride, 64L);
        GL33.glVertexAttribDivisor(index++, 1);

        GL30.glBindVertexArray(0);

        int err = GL11.glGetError();
        if (err != GL11.GL_NO_ERROR) {
            System.err.println("OpenGL Error after init: " + err);
        }
    }


    public void draw(Matrix4f transform, Color color) {
        instances.add(new InstanceData(transform, color));
    }

    public void render(Vector3f cameraPos) {
        if (instances.isEmpty()) return;
        if (!initialized) init();

        shader.bind();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(instances.size() * (16 + 4));
        for (InstanceData instance : instances) {
            instance.transform.get(buffer);
            buffer.put(instance.color.getRed());
            buffer.put(instance.color.getGreen());
            buffer.put(instance.color.getBlue());
            buffer.put(instance.color.getAlpha());
        }
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);

        GL30.glBindVertexArray(vao);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, vertexCount, instances.size());
        GL30.glBindVertexArray(0);

        instances.clear();
    }

    private record InstanceData(Matrix4f transform, Color color) {
        private InstanceData(Matrix4f transform, Color color) {
            this.transform = new Matrix4f(transform);
            this.color = color;
        }
    }

    private void destroy() {
        shader.destroy();
        GL15.glDeleteBuffers(vertexVbo);
        GL15.glDeleteBuffers(instanceVbo);
        GL30.glDeleteVertexArrays(vao);
    }

    public static void destroyAll() {
        meshes.forEach(InstancedMesh::destroy);
        meshes.clear();
    }

}

