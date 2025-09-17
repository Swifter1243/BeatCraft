package com.beatcraft.client.render.instancing;

import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.client.render.gl.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class InstancedMesh<I extends InstancedMesh.InstanceData> {

    public interface InstanceData {
        Matrix4f getTransform();
        void putData(FloatBuffer buffer);
        int getFrameSize();
        void init();
        int[] getLocations();
        default void setup(int program) {}
        default void setup(int program, DrawPass pass) { setup(program); }
        void cleanup();
        void free();
        InstanceData copy();
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

    private final ResourceLocation shaderName;
    private final ResourceLocation texture;
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
    private int shaderProgram;

    private final ArrayList<I> instanceDataList;
    private final ArrayList<I> bloomCopyCalls;
    private boolean initialized;

    public static boolean isQuest3 = false;

    private ResourceLocation vertexShaderLoc;
    private ResourceLocation fragmentShaderLoc;


    public InstancedMesh(ResourceLocation shaderName, ResourceLocation texture, Triplet<Vector3f, Vector2f, Vector3f>[] vertices) {
        this.shaderName = shaderName;
        this.texture = texture;
        this.vertices = vertices;
        this.vertexCount = vertices.length;
        this.instanceDataList = new ArrayList<>();
        this.bloomCopyCalls = new ArrayList<>();
        this.instanceCount = 0;
        this.initialized = false;

        generateIndices();

        meshes.add(this);
    }

    public InstancedMesh<I> copy() {
        return new InstancedMesh<>(shaderName, texture, vertices);
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

        var vendor = GL31.glGetString(GL31.GL_VENDOR);
        isQuest3 = (vendor != null && vendor.contains("QuestCraft"));

        vertexShaderLoc = ResourceLocation.tryBuild(shaderName.getNamespace(), "shaders/" + shaderName.getPath() + ".vsh");
        fragmentShaderLoc = ResourceLocation.tryBuild(shaderName.getNamespace(), "shaders/" + shaderName.getPath() + ".fsh");

        vao = GL45C.glCreateVertexArrays();
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

        normalVbo = GL45C.glCreateBuffers();
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
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        initialized = true;
    }

    public void draw(I data) {
        instanceDataList.add(data);
    }

    public void copyDrawToBloom(Color color) {
        var draw = (I) instanceDataList.getLast().copy();
        if (draw instanceof ArrowInstanceData arrowDraw) {
            arrowDraw.setColor(color);
        }
        bloomCopyCalls.add(draw);
    }

    public void copyDrawToBloom() {
        copyDrawToBloom(null);
    }

    public void cancelDraws() {
        instanceDataList.clear();
        bloomCopyCalls.clear();
    }

    public void renderBloom(Vector3f cameraPos, Quaternionf cameraRotation) {
        render(cameraPos, bloomCopyCalls, cameraRotation, DrawPass.Bloom);
    }

    public void render(Vector3f cameraPos) {
        var q = MemoryPool.newQuaternionf();
        render(cameraPos, instanceDataList, q, DrawPass.Normal);
        MemoryPool.releaseSafe(q);
    }

    public void cancelBloomCalls() {
        bloomCopyCalls.clear();
    }

    protected void render(Vector3f cameraPos, ArrayList<I> dataList, Quaternionf cameraRotation, DrawPass pass) {

        if (dataList.isEmpty()) {
            return;
        }

        var first = dataList.getFirst();

        if (!initialized) init(first);

        var attrLocations = first.getLocations();

        instanceCount = dataList.size();

        dataList.sort((a, b) -> {
            var ta = a.getTransform();
            var tb = b.getTransform();
            var posA = new Vector3f(ta.m30(), ta.m31(), ta.m32());
            var posB = new Vector3f(tb.m30(), tb.m31(), tb.m32());

            var distA = posA.distanceSquared(cameraPos);
            var distB = posB.distanceSquared(cameraPos);

            return Float.compare(distB, distA);
        });

        IntBuffer vaoBuf = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, vaoBuf);
        int oldVAO = vaoBuf.get(0);

        IntBuffer vboBuf = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, vboBuf);
        int oldVBO = vboBuf.get(0);

        GL30.glBindVertexArray(vao);

        ARBInstancedArrays.glVertexAttribDivisorARB(POSITION_LOCATION, 0);
        ARBInstancedArrays.glVertexAttribDivisorARB(TEXCOORD_LOCATION, 0);
        ARBInstancedArrays.glVertexAttribDivisorARB(NORMAL_LOCATION, 0);

        for (var loc : attrLocations) {
            ARBInstancedArrays.glVertexAttribDivisorARB(loc, 1);
        }

        activateShaderAndTexture(cameraPos, cameraRotation);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();

        first.setup(shaderProgram, pass);

        // mat4 + vec4 + float
        FloatBuffer instanceDataBuffer = MemoryUtil.memAllocFloat(instanceCount * first.getFrameSize());

        for (InstanceData data : dataList) {
            data.putData(instanceDataBuffer);
        }

        instanceDataBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceDataBuffer, GL15.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(instanceDataBuffer);


        GL31.glDrawElementsInstanced(
            GL11.GL_TRIANGLES,
            indices.length,
            GL11.GL_UNSIGNED_INT,
            0,
            instanceCount
        );


        first.cleanup();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();

        GL20.glUseProgram(0);

        for (var loc : attrLocations) {
            ARBInstancedArrays.glVertexAttribDivisorARB(loc, 0);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, oldVBO);
        GL30.glBindVertexArray(oldVAO);

        dataList.forEach(InstancedMesh.InstanceData::free);
        dataList.clear();
    }


    private void activateShaderAndTexture(Vector3f cameraPos, Quaternionf cameraRotation) {

        shaderProgram = GlUtil.getOrCreateShaderProgram(vertexShaderLoc, fragmentShaderLoc);
        GlUtil.useProgram(shaderProgram);

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(texture);

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.bindTexture(0);
        GlUtil.setTex(shaderProgram, "u_texture", 0, abstractTexture.getId());

        var projMat = RenderSystem.getProjectionMatrix();
        var viewMat = new Matrix4f(RenderSystem.getModelViewMatrix()).rotate(cameraRotation).translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        GlUtil.uniformMat4f("u_projection", projMat);
        GlUtil.uniformMat4f("u_view", viewMat);

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