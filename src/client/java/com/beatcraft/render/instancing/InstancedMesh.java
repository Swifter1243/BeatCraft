package com.beatcraft.render.instancing;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstancedMesh<I extends InstancedMesh.InstanceData> {

    public interface InstanceData {
        Matrix4f getTransform();
        void putData(FloatBuffer buffer);
        int getFrameSize();
        void init();
    }

    private static final ArrayList<InstancedMesh<? extends InstanceData>> meshes = new ArrayList<>();

    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int VECTOR3F_SIZE_BYTES = 3 * FLOAT_SIZE_BYTES;
    public static final int VECTOR2F_SIZE_BYTES = 2 * FLOAT_SIZE_BYTES;
    public static final int MATRIX4F_SIZE_BYTES = 16 * FLOAT_SIZE_BYTES;
    public static final int VEC4_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

    private static final int POSITION_LOCATION = 0;
    private static final int TEXCOORD_LOCATION = 1;
    private static final int NORMAL_LOCATION = 2;

    private final Identifier shaderName;
    private final Identifier texture;
    private final Triplet<Vector3f, Vector2f, Vector3f>[] vertices;

    private int vao;
    private int vertexVbo;
    private int uvVbo;
    private int normalVbo;
    private int instanceVbo;
    private int indicesVbo;
    private int[] indices;
    private final int vertexCount;
    private int instanceCount;

    private final List<I> instanceDataList;
    private boolean initialized;

    private static final Map<Identifier, Integer> shaderProgramCache = new HashMap<>();

    private int createShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {

        int program = GL20.glCreateProgram();

        try {
            int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderLoc);
            int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderLoc);

            GL20.glAttachShader(program, vertexShader);
            GL20.glAttachShader(program, fragmentShader);

            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to link shader program: " + log);
            }

            GL20.glValidateProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program);
                throw new RuntimeException("Failed to validate shader program: " + log);
            }

            GL20.glDetachShader(program, vertexShader);
            GL20.glDetachShader(program, fragmentShader);
            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);

            return program;
        } catch (Exception e) {
            GL20.glDeleteProgram(program);
            throw new RuntimeException("Failed to create shader program", e);
        }
    }

    private int compileShader(int type, Identifier shaderLoc) throws IOException {
        var shader = GL20.glCreateShader(type);

        var resourceManager = MinecraftClient.getInstance().getResourceManager();
        var source = resourceManager.getResource(shaderLoc)
            .orElseThrow(() -> new IOException("Could not find shader: " + shaderLoc))
            .getReader()
            .lines()
            .reduce("", (a, b) -> a + b + "\n");

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader: " + log);
        }

        return shader;
    }

    private int getOrCreateShaderProgram(Identifier vertexShaderLoc, Identifier fragmentShaderLoc) {
        var cacheKey = Identifier.of(
            vertexShaderLoc.getNamespace(),
            vertexShaderLoc.getPath() + "_" + fragmentShaderLoc.getPath()
        );

        return shaderProgramCache.computeIfAbsent(cacheKey,
            k -> createShaderProgram(vertexShaderLoc, fragmentShaderLoc));
    }


    public InstancedMesh(Identifier shaderName, Identifier texture, Triplet<Vector3f, Vector2f, Vector3f>[] vertices) {
        this.shaderName = shaderName;
        this.texture = texture;
        this.vertices = vertices;
        this.vertexCount = vertices.length;
        this.instanceDataList = new ArrayList<>();
        this.instanceCount = 0;
        this.initialized = false;

        generateIndices();

        meshes.add(this);
    }

    private void generateIndices() {
        var indexList = new ArrayList<Integer>();

        for (int i = 0; i < vertexCount; i += 3) {
            indexList.add(i);
            indexList.add(i + 1);
            indexList.add(i + 2);
        }

        indices = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }
    }

    public void init(I setupFrame) {
        if (initialized) {
            return;
        }

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vertexVbo = GL15.glGenBuffers();
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
        for (Triplet<Vector3f, Vector2f, Vector3f> vertex : vertices) {
            Vector3f pos = vertex.getA();
            posBuffer.put(pos.x).put(pos.y).put(pos.z);
        }
        posBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(POSITION_LOCATION, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(POSITION_LOCATION);
        MemoryUtil.memFree(posBuffer);

        uvVbo = GL15.glGenBuffers();
        FloatBuffer uvBuffer = MemoryUtil.memAllocFloat(vertices.length * 2);
        for (Triplet<Vector3f, Vector2f, Vector3f> vertex : vertices) {
            Vector2f uv = vertex.getB();
            uvBuffer.put(uv.x).put(uv.y);
        }
        uvBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(TEXCOORD_LOCATION, 2, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(TEXCOORD_LOCATION);
        MemoryUtil.memFree(uvBuffer);

        normalVbo = GL15.glGenBuffers();
        FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
        for (Triplet<Vector3f, Vector2f, Vector3f> vertex : vertices) {
            Vector3f normal = vertex.getC();
            normalBuffer.put(normal.x).put(normal.y).put(normal.z);
        }
        normalBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(NORMAL_LOCATION, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(NORMAL_LOCATION);
        MemoryUtil.memFree(normalBuffer);

        indicesVbo = GL15.glGenBuffers();
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        instanceVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);

        setupFrame.init();

        GL30.glBindVertexArray(0);

        initialized = true;
    }

    public void draw(I data) {
        instanceDataList.add(data);
    }

    public void render(Vector3f cameraPos) {
        if (instanceDataList.isEmpty()) {
            return;
        }
        if (!initialized) init(instanceDataList.getFirst());

        instanceCount = instanceDataList.size();

        instanceDataList.sort((a, b) -> {
            var ta = a.getTransform();
            var tb = b.getTransform();
            var posA = new Vector3f(ta.m30(), ta.m31(), ta.m32());
            var posB = new Vector3f(tb.m30(), tb.m31(), tb.m32());

            var distA = posA.distanceSquared(cameraPos);
            var distB = posB.distanceSquared(cameraPos);

            return Float.compare(distB, distA);
        });

        activateShaderAndTexture();

        GL30.glBindVertexArray(vao);

        // mat4 + vec4 + float
        FloatBuffer instanceDataBuffer = MemoryUtil.memAllocFloat(instanceCount * instanceDataList.getFirst().getFrameSize());

        for (InstanceData data : instanceDataList) {
            data.putData(instanceDataBuffer);
        }
        instanceDataBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceDataBuffer, GL15.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(instanceDataBuffer);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        GL31.glDrawElementsInstanced(
            GL11.GL_TRIANGLES,
            indices.length,
            GL11.GL_UNSIGNED_INT,
            0,
            instanceCount);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        GL30.glBindVertexArray(0);

        deactivateShaderAndTexture();

        instanceDataList.clear();
    }

    private void setMat4f(int shaderProgram, String uni, Matrix4f mat4) {
        int uniLoc = GL20.glGetUniformLocation(shaderProgram, uni);
        GL20.glUniformMatrix4fv(uniLoc, false, mat4.get(new float[16]));
    }

    private void activateShaderAndTexture() {
        var vertexShaderLoc = Identifier.of(shaderName.getNamespace(), "shaders/" + shaderName.getPath() + ".vsh");
        var fragmentShaderLoc = Identifier.of(shaderName.getNamespace(), "shaders/" + shaderName.getPath() + ".fsh");

        int shaderProgram = getOrCreateShaderProgram(vertexShaderLoc, fragmentShaderLoc);
        GL20.glUseProgram(shaderProgram);

        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.bindTexture(0);

        var projMat = RenderSystem.getProjectionMatrix();
        var viewMat = RenderSystem.getModelViewMatrix();
        setMat4f(shaderProgram, "u_projection", projMat);
        setMat4f(shaderProgram, "u_view", viewMat);

    }

    private void deactivateShaderAndTexture() {
        GL20.glUseProgram(0);
    }

    public void cleanup() {
        GL15.glDeleteBuffers(vertexVbo);
        GL15.glDeleteBuffers(uvVbo);
        GL15.glDeleteBuffers(normalVbo);
        GL15.glDeleteBuffers(instanceVbo);
        GL15.glDeleteBuffers(indicesVbo);
        GL30.glDeleteVertexArrays(vao);
    }

    public static void cleanupAll() {
        meshes.forEach(InstancedMesh::cleanup);
        meshes.clear();
    }

}