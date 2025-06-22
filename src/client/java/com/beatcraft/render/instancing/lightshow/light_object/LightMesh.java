package com.beatcraft.render.instancing.lightshow.light_object;

/*
This mesh loads from json using a custom format.

the shader uses 8 color channels and a texture atlas

each mesh vertex must have data to determine:
position, uv, vec, colorId, materialId, textureId

 */


/*
Mesh format:
{
    "credits": ["string or list of strings"],
    "mesh_format": 1,
    "parts": {
        "part-name": {
            "vertices": [
                [0.0, 1.0, 2.0] // vertices are in meters instead of 16x16 pixel space
            ],
            "named_vertices": {
                "unique_name": [1.0, 1.0, 2.0] // named vertices makes keeping trak of things easier
            },
            "compute_vertices": {
                // interpolated vertices must be named but are
                // not required to interpolate from named vertices
                "vertice_name": {
                    "points": [0, "unique_name"], // key determines interpolation function
                    "function": "linear",
                    "delta": 0.5, // if delta: interpolate this much between points
                    "y": 3.0      // if x/y/z: set given axis to the provided value as a.<axis> + value and then interpolate the other 2 (or other 1 if 2 axes are specified) axes by `function`
                    // note: only one of x/y/z or delta can be set
                }
            },
            "uvs": [...],
            "named_uvs": {...},
            "normals": [...], // vec vectors will be normalized when loaded
            "named_normals": {...},
            "compute_normals": {
                "name": [vertA, vertB, vertC] // vec faces towards camera when vertices are wound clockwise
            },
            "triangles": [
                [
                    [v0, uv0, normal0], // vertex 0
                    [v1, uv1, normal1], // vertex 1
                    [v2, uv2, normal2], // vertex 2
                    "light", {"color": 0} // data index/name, the object afterwards overrides data in the named data attributes
                ]
            ]
        }
    },
    "mesh": [
        {
            "part": "name",
            "scale": [1, 1, 1],
            "position": [0, 0, 0],
            "rotation": [0, 0, 0, 1] // quaternion
        }
    ]
    "textures": {
        "0": "beatcraft:path/name" // only 0, 1, and 2 are valid texture slots
    },
    "data": {
        "default": { "material": 0, "texture": 0, "color": 0 }, // defines the default for any un-defined data
        "light": { "material": 1 } // missing keys fallback to 'default'
    },
    "cull": false, // whether to do backface culling
    "bloom_pass": true,  // defaults to true if absent; set to false to disable the bloom render pass
    "mirror_pass": true, // defaults to true; whether to draw this mesh in the runway mirror
    "solid_pass": true,  // defaults to true; set to false to disable the regular visible pass
    "bloomfog_style": 0  // defaults to 0;
    // 0 = anything that renders to bloom will also render to bloomfog
    // 1 = everything renders to bloomfog. note: the pass that draws to bloomfog will supply a blank texture as the mesh's u_bloomfog sampler
    // 2 = don't render to bloomfog

}

 */

import com.beatcraft.BeatCraft;
import com.beatcraft.animation.Easing;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.gl.GlUtil;
import com.beatcraft.utils.JsonUtil;
import com.beatcraft.utils.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class LightMesh {

    private static final class Location {
        static final int POSITION = 0;
        static final int UV = 1;
        static final int NORMAL = 2;
        static final int LAYERS = 3;
        static final int TRANSFORM = 4;
        static int color(int channel) {
            return 8 + Math.clamp(channel, 0, 7);
        }
        static final int[] ALL_INSTANCED = new int[]{
            TRANSFORM, TRANSFORM + 1, TRANSFORM + 2, TRANSFORM + 3,
            8, 9, 10, 11,
            12, 13, 14, 15
        };
    }

    private record Draw(Matrix4f transform, LightState[] colors) {
        private static final Stack<Draw> shared = new Stack<>();

        public static Draw create(Matrix4f transform, LightState[] colors) {
            if (shared.empty()) {
                var copyColors = new LightState[colors.length];
                for (int i = 0; i < colors.length; i++) {
                    copyColors[i] = colors[i].copy();
                }
                return new Draw(new Matrix4f(transform), copyColors);
            } else {
                var inst = shared.pop();
                inst.transform.set(transform);
                for (int i = 0; i < colors.length; i++) {
                    inst.colors[i].set(colors[i]);
                }
                return inst;
            }
        }

        public void free() {
            shared.push(this);
        }

        public void upload(FloatBuffer buffer, boolean isBloom) {

            buffer.put(transform.m00()).put(transform.m01()).put(transform.m02()).put(transform.m03());
            buffer.put(transform.m10()).put(transform.m11()).put(transform.m12()).put(transform.m13());
            buffer.put(transform.m20()).put(transform.m21()).put(transform.m22()).put(transform.m23());
            buffer.put(transform.m30()).put(transform.m31()).put(transform.m32()).put(transform.m33());

            for (var lightState : colors) {
                var c = isBloom ? lightState.getBloomColor() : lightState.getEffectiveColor();
                var color = new Color(c);
                buffer.put(color.getRed()).put(color.getGreen()).put(color.getBlue()).put(color.getAlpha());
            }

        }

    }

    public static final HashMap<String, LightMesh> meshes = new HashMap<>();

    private static final ArrayList<Identifier> unloadedTextures = new ArrayList<>();
    private static final HashMap<Identifier, Vector2f> uvMap = new HashMap<>();
    public static boolean initialized = false;
    private static int atlasGlId;

    protected static class AtlasBuilder {
        public final int maxWidth;
        public final int maxHeight;
        public int skylineCount = 0;
        private boolean initialized = false;
        private RectanglePacker packer;

        protected AtlasBuilder(int size) {
            this.maxWidth = size;
            this.maxHeight = size;
            this.packer = new RectanglePacker(size, size);
        }

        protected boolean add(Vector2i size, Vector2i posOut) {
            var out = packer.pack(size.x, size.y);
            if (out != null) {
                posOut.set(out);
                return true;
            }
            return false;
        }
    }

    public static void buildMeshes() {
        if (initialized) return;

        var atlasBuilder = new AtlasBuilder(1024);
        try (var atlas = new NativeImage(1024, 1024, false)) {

            var manager = MinecraftClient.getInstance().getResourceManager();

            for (var ident : unloadedTextures) {
                var in = manager.getResource(ident).orElseThrow(() -> new RuntimeException("File '" + ident + "' could not be loaded"));
                try (var input = in.getInputStream()) {
                    var tex = NativeImage.read(NativeImage.Format.RGBA, input);
                    var w = tex.getWidth();
                    var h = tex.getHeight();

                    var pos = new Vector2i();

                    if (atlasBuilder.add(new Vector2i(w, h), pos)) {
                        uvMap.put(ident, new Vector2f(pos.x / 1024f, pos.y / 1024f));
                        tex.copyRect(atlas, 0, 0, pos.x, pos.y, w, h, false, true);
                    } else {
                        throw new RuntimeException("Atlas size exceeded");
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            atlasGlId = GL31.glGenTextures();
            GL31.glBindTexture(GL31.GL_TEXTURE_2D, atlasGlId);

            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_EDGE);
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0,
                GL11.GL_RGBA8, 1024, 1024, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null
            );

            //try {
            //    atlas.writeTo(Path.of(MinecraftClient.getInstance().runDirectory.getPath() + "/beatcraft_lightmesh_atlas.png"));
            //} catch (IOException e) {
            //    throw new RuntimeException(e);
            //}
            atlas.upload(0, 0, 0, 0, 0, 1024, 1024, false, true);


            initialized = true;
        }

        meshes.values().forEach(LightMesh::buildMesh);

    }

    protected record VertexData(Vector3f vertex, Vector2f uv, Vector3f normal) {
        protected VertexData transform(Vector3f scale, Vector3f pos, Quaternionf rot) {
            var v = new Vector3f(vertex).mul(scale).rotate(rot).add(pos);
            var n = new Vector3f(normal).mul(scale).rotate(rot).normalize();
            return new VertexData(v, uv, n);
        }
    }

    protected record TriangleData(int colorId, int materialId, int textureId) {
        protected TriangleData extend(JsonObject overrides) {
            var colId = JsonUtil.getOrDefault(overrides, "color", JsonElement::getAsInt, colorId);
            var matId = JsonUtil.getOrDefault(overrides, "material", JsonElement::getAsInt, materialId);
            var tex = JsonUtil.getOrDefault(overrides, "texture", JsonElement::getAsInt, textureId);
            return new TriangleData(colId, matId, tex);
        }
    }

    protected record Triangle(VertexData a, VertexData b, VertexData c, TriangleData data, String baseMaterial) {
        protected Triangle transform(JsonObject transformation, HashMap<String, TriangleData> materials) {
            if (
                transformation.has("scale")
                || transformation.has("remap_data")
                || transformation.has("position")
                || transformation.has("rotation")
            ) {
                var scale = new Vector3f(1, 1, 1);
                var pos = new Vector3f();
                var rotation = new Quaternionf();
                var mat = data;
                String baseMat = null;

                if (transformation.has("remap_data")) {
                    var remapping = transformation.getAsJsonObject("remap_data");
                    var newMat = remapping.get(baseMaterial);
                    if (newMat != null) {
                        mat = materials.get(newMat.getAsString());
                        baseMat = newMat.getAsString();
                    }
                }

                boolean reWind = false;
                if (transformation.has("scale")) {
                    scale = JsonUtil.getVector3(transformation.getAsJsonArray("scale"));
                    reWind = scale.x * scale.y * scale.z < 0;
                }

                if (transformation.has("position")) {
                    pos = JsonUtil.getVector3(transformation.getAsJsonArray("position"));
                }

                if (transformation.has("rotation")) {
                    var rot = JsonUtil.getVector4(transformation.getAsJsonArray("rotation"));
                    rotation = new Quaternionf(rot.x, rot.y, rot.z, rot.w).normalize();
                }

                var a = this.a.transform(scale, pos, rotation);
                var b = this.b.transform(scale, pos, rotation);
                var c = this.c.transform(scale, pos, rotation);

                if (reWind) {
                    return new Triangle(c, b, a, mat, baseMat);
                } else {
                    return new Triangle(a, b, c, mat, baseMat);
                }
            }
            return this;
        }

    }

    private final ArrayList<Triangle> triangles;
    private final HashMap<Integer, Identifier> meshTextures;
    private boolean doBloom = true;
    private boolean doSolid = true;
    private boolean doMirroring = true;
    private int bloomfogStyle = 0;
    private boolean cullBackfaces = false;

    private int shaderProgram = 0;

    // TODO: mirror probably needs a different shader to do correct clipping
    private int mirrorProgram = 0;
    private int bloomProgram = 0;
    private int bloomfogProgram = 0;

    private int vao;
    private int vertexVbo;
    private int uvVbo;
    private int normalVbo;
    private int materialVbo;
    private int indicesVbo;
    private int instanceVbo;
    private int indicesLength;

    private boolean loaded = false;

    private final ArrayList<Draw> draws = new ArrayList<>();
    private final ArrayList<Draw> mirrorDraws = new ArrayList<>();
    private final ArrayList<Draw> bloomfogDraws = new ArrayList<>();
    private final ArrayList<Draw> bloomDraws = new ArrayList<>();

    protected LightMesh(HashMap<Integer, Identifier> unloadedTextures) {
        this.triangles = new ArrayList<>();
        meshTextures = unloadedTextures;
        LightMesh.unloadedTextures.addAll(unloadedTextures.values());
    }

    protected void addTriangle(Triangle tri) {
        triangles.add(tri);
    }

    public void draw(Matrix4f transform, LightState[] colors) {
        if (doSolid) draws.add(Draw.create(transform, colors));
        if (doMirroring) {
            var c = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
            var renderPos = transform.getTranslation(MemoryPool.newVector3f());
            var renderRotation = transform.getUnnormalizedRotation(MemoryPool.newQuaternionf());
            var renderScale = transform.getScale(MemoryPool.newVector3f());

            var flipped = new Matrix4f().scale(1, -1, 1);
            flipped.translate(0, c.y * 2f, 0);
            flipped.translate(renderPos);
            flipped.rotate(renderRotation);
            flipped.scale(renderScale);

            mirrorDraws.add(Draw.create(transform, colors));
        }
        if (bloomfogStyle < 2) bloomfogDraws.add(Draw.create(transform, colors));
        if (doBloom) bloomDraws.add(Draw.create(transform, colors));
    }


    private String processShaderSource(String source) {

        return GlUtil.reProcess(source);
    }

    private static final Identifier lightObjectVsh = BeatCraft.id("shaders/instanced/light_object.vsh");
    private static final Identifier lightObjectFsh = BeatCraft.id("shaders/instanced/light_object.fsh");

    private void putVec3f(FloatBuffer buf, Vector3f vec) {
        buf.put(vec.x).put(vec.y).put(vec.z);
    }

    private void putVec3i(IntBuffer buf, int a, int b, int c) {
        buf.put(a).put(b).put(c);
    }

    private void uploadFloatBuffer(int vbo, int location, int size, FloatBuffer buffer) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(location, size, GL15.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(location);
        MemoryUtil.memFree(buffer);
    }

    private void uploadIntBuffer(int vbo, int location, int size, IntBuffer buffer) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribIPointer(location, size, GL11.GL_INT, 0, 0);
        GL20.glEnableVertexAttribArray(location);
        MemoryUtil.memFree(buffer);
    }

    private void buildMesh() {
        if (loaded) return;

        shaderProgram = GlUtil.createShaderProgram(lightObjectVsh, lightObjectFsh, this::processShaderSource);

        vao = GL45C.glCreateVertexArrays();
        GL30.glBindVertexArray(vao);
        vertexVbo = GL15.glGenBuffers();
        indicesVbo = GL15.glGenBuffers();
        uvVbo = GL15.glGenBuffers();
        normalVbo = GL15.glGenBuffers();
        materialVbo = GL15.glGenBuffers();

        var uvBuffer = MemoryUtil.memAllocFloat(triangles.size() * 3 * 2);
        var normalBuffer = MemoryUtil.memAllocFloat(triangles.size() * 3 * 3);
        var materialBuffer = MemoryUtil.memAllocInt(triangles.size() * 3 * 3);


        var vertices = new ArrayList<Vector3f>();

        for (var tri : triangles) {
            vertices.add(tri.a.vertex);
            vertices.add(tri.b.vertex);
            vertices.add(tri.c.vertex);

            var offset = uvMap.get(meshTextures.get(tri.data.textureId));

            uvBuffer.put(tri.a.uv.x + offset.x).put(tri.a.uv.y + offset.y);
            uvBuffer.put(tri.b.uv.x + offset.x).put(tri.b.uv.y + offset.y);
            uvBuffer.put(tri.c.uv.x + offset.x).put(tri.c.uv.y + offset.y);

            putVec3f(normalBuffer, tri.a.normal);
            putVec3f(normalBuffer, tri.b.normal);
            putVec3f(normalBuffer, tri.c.normal);

            putVec3i(materialBuffer, tri.data.colorId, tri.data.materialId, 0);
            putVec3i(materialBuffer, tri.data.colorId, tri.data.materialId, 0);
            putVec3i(materialBuffer, tri.data.colorId, tri.data.materialId, 0);

        }

        //var indexedVertices = GlUtil.getDedupedVertices(vertices);

        var vertexBuffer = MemoryUtil.memAllocFloat(vertices.size() * 3);
        var indexBuffer = MemoryUtil.memAllocInt(vertices.size());
        indicesLength = vertices.size();

        var verts = new float[vertices.size() * 3];
        var indices = new int[vertices.size()];
        var i = 0;
        for (var vert : vertices) {
            verts[i * 3] = vert.x;
            verts[(i * 3) + 1] = vert.y;
            verts[(i * 3) + 2] = vert.z;
            indices[i] = i++;
        }

        vertexBuffer.put(verts);
        indexBuffer.put(indices);

        vertexBuffer.flip();
        indexBuffer.flip();
        uvBuffer.flip();
        normalBuffer.flip();
        materialBuffer.flip();

        uploadFloatBuffer(vertexVbo, Location.POSITION, 3, vertexBuffer);
        uploadFloatBuffer(uvVbo, Location.UV, 2, uvBuffer);
        uploadFloatBuffer(normalVbo, Location.NORMAL, 3, normalBuffer);
        uploadIntBuffer(materialVbo, Location.LAYERS, 3, materialBuffer);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(indexBuffer);

        setupInstanceBuffer();

        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        loaded = true;
    }

    private static final int COLOR_COUNT = 8;
    private static final int FRAME_SIZE = 16 + (4 * COLOR_COUNT);
    private static final int STRIDE = FRAME_SIZE * 4;
    private void setupInstanceBuffer() {

        instanceVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);

        for (int i = 0; i < 4; i++) {
            int location = Location.TRANSFORM + i;
            GL20.glVertexAttribPointer(
                location, 4, GL11.GL_FLOAT, false, STRIDE, i * 16
            );
            GL20.glEnableVertexAttribArray(location);
            ARBInstancedArrays.glVertexAttribDivisorARB(location, 1);
        }

        for (int i = 0; i < COLOR_COUNT; i++) {
            int location = Location.color(i);
            GL20.glVertexAttribPointer(
                location, 4, GL11.GL_FLOAT, false, STRIDE, (16 * 4) + (i * 16)
            );
            GL20.glEnableVertexAttribArray(location);
            ARBInstancedArrays.glVertexAttribDivisorARB(location, 1);
        }

    }

    public static void renderAllSolid() {
        for (var mesh : meshes.values()) {
            mesh.renderSolid();
        }
    }

    public static void renderAllMirror() {
        for (var mesh : meshes.values()) {
            mesh.renderMirror();
        }
    }

    public static void renderAllBloom(int sceneDepthBuffer) {
        for (var mesh : meshes.values()) {
            mesh.renderBloom(sceneDepthBuffer);
        }
    }

    public static void renderAllBloomfog() {
        for (var mesh : meshes.values()) {
            mesh.renderBloomfog();
        }
    }

    public static void cancelMirrorDraws() {
        for (var mesh : meshes.values()) {
            mesh.cancelMirrorDraw();
        }
    }

    public static void cancelBloomDraws() {
        for (var mesh : meshes.values()) {
            mesh.cancelBloomDraw();
        }
    }

    public void renderSolid() {
        render(draws, false, false, -1);
    }

    public void renderMirror() {
        render(mirrorDraws, true, false, -1);
    }

    private void cancelMirrorDraw() {
        mirrorDraws.clear();
    }

    public void renderBloom(int sceneDepthBuffer) {
        render(bloomDraws, false, true, sceneDepthBuffer);
    }

    private void cancelBloomDraw() {
        bloomDraws.clear();
    }

    public void renderBloomfog() {
        render(bloomfogDraws, true, false, -1);
    }

    private void render(ArrayList<Draw> drawList, boolean preBloomfog, boolean isBloom, int sceneDepthBuffer) {

        if (drawList.isEmpty()) return;

        IntBuffer vaoBuf = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, vaoBuf);
        int oldVAO = vaoBuf.get(0);

        IntBuffer vboBuf = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, vboBuf);
        int oldVBO = vboBuf.get(0);

        // PHASE 1: enable instancing attributes
        GL30.glBindVertexArray(vao);

        ARBInstancedArrays.glVertexAttribDivisorARB(Location.POSITION, 0);
        ARBInstancedArrays.glVertexAttribDivisorARB(Location.UV, 0);
        ARBInstancedArrays.glVertexAttribDivisorARB(Location.NORMAL, 0);

        for (var loc : Location.ALL_INSTANCED) {
            ARBInstancedArrays.glVertexAttribDivisorARB(loc, 1);
        }

        GlUtil.useProgram(shaderProgram);
        GlUtil.setTex(shaderProgram, "u_texture", 0, atlasGlId);
        var fog = BeatCraftRenderer.bloomfog;
        if (preBloomfog) {
            fog.extraBuffer.setClearColor(0, 0, 0, 1);
            fog.extraBuffer.clear(false);
            fog.extraBuffer.beginRead();
            GlUtil.setTex(shaderProgram, "u_bloomfog", 1, fog.extraBuffer.getColorAttachment());
        } else {
            GlUtil.setTex(shaderProgram, "u_bloomfog", 1, fog.getBloomfogColorAttachment());
        }
        GlUtil.uniform1i("passType", isBloom ? 1 : 0);
        if (sceneDepthBuffer != -1) {
            GlUtil.setTex(shaderProgram, "u_depth", 2, sceneDepthBuffer);
        }

        var fogHeights = Bloomfog.getFogHeights();
        GlUtil.uniform2f("u_fog", fogHeights[0], fogHeights[1]);


        var q = MemoryPool.newQuaternionf();
        if (isBloom) {
            q.set(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation()).conjugate();
        }
        var p = MemoryPool.newVector3f();
        p.set(MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f()).negate();

        var projMat = RenderSystem.getProjectionMatrix();
        var viewMat = new Matrix4f(RenderSystem.getModelViewMatrix()).rotate(q);
        MemoryPool.releaseSafe(q);
        MemoryPool.releaseSafe(p);
        GlUtil.uniformMat4f("u_projection", projMat);
        GlUtil.uniformMat4f("u_view", viewMat);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        if (!cullBackfaces) RenderSystem.disableCull();

        // PHASE 2: setup buffer data
        var instanceBuffer = MemoryUtil.memAllocFloat(drawList.size() * FRAME_SIZE);

        var instanceCount = drawList.size();

        // PHASE 3: write buffer data
        for (var draw : drawList) {
            draw.upload(instanceBuffer, isBloom);
        }
        instanceBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceBuffer, GL15.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(instanceBuffer);

        // PHASE 4: draw
        GL31.glDrawElementsInstanced(
            GL11.GL_TRIANGLES,
            indicesLength,
            GL11.GL_UNSIGNED_INT,
            0,
            instanceCount
        );
        // PHASE 5: disable instancing attributes

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        if (!cullBackfaces) RenderSystem.enableCull();

        if (preBloomfog) {
            fog.extraBuffer.endRead();
            fog.extraBuffer.setClearColor(0, 0, 0, 0);
        }

        GL20.glUseProgram(0);

        for (var loc : Location.ALL_INSTANCED) {
            ARBInstancedArrays.glVertexAttribDivisorARB(loc, 0);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, oldVBO);
        GL30.glBindVertexArray(oldVAO);


        drawList.forEach(Draw::free);
        drawList.clear();
    }

    private static class MeshConstructor {

        private final ArrayList<Vector3f> vertices = new ArrayList<>();
        private final ArrayList<Vector2f> uvs = new ArrayList<>();
        private final ArrayList<Vector3f> normals = new ArrayList<>();

        // named Lists map to the index into the un-named list instead of to vertices
        private final HashMap<String, Integer> namedVertices = new HashMap<>();
        private final HashMap<String, Integer> namedUvs = new HashMap<>();
        private final HashMap<String, Integer> namedNormals = new HashMap<>();

        private final ArrayList<Triangle> triangles = new ArrayList<>();

        protected void addTriangle(Triangle tri) {
            triangles.add(tri);
        }

        protected void addToMesh(LightMesh mesh, JsonObject transform, HashMap<String, TriangleData> materials) {
            for (var tri : triangles) {
                mesh.addTriangle(tri.transform(transform, materials));
            }
        }

        protected Vector3f getVertex(Object idxOrName) {
            if (idxOrName instanceof Integer i) {
                return vertices.get(i);
            } else if (idxOrName instanceof String s) {
                var idx = namedVertices.get(s);
                if (idx == null) {
                    throw new IllegalArgumentException("vertex name is not valid");
                }
                return vertices.get(idx);
            }
            throw new IllegalArgumentException("idxOrName must be a String or and Integer");
        }

        protected Vector3f getNormal(Object idxOrName) {
            if (idxOrName instanceof Integer i) {
                return normals.get(i);
            } else if (idxOrName instanceof String s) {
                var idx = namedNormals.get(s);
                if (idx == null) {
                    throw new IllegalArgumentException("vec name '" + s + "' is not valid");
                }
                return normals.get(idx);
            }
            throw new IllegalArgumentException("idxOrName must be a String or and Integer");
        }

        protected Vector2f getUv(Object idxOrName) {
            if (idxOrName instanceof Integer i) {
                return uvs.get(i);
            } else if (idxOrName instanceof String s) {
                var idx = namedUvs.get(s);
                if (idx == null) {
                    throw new IllegalArgumentException("uv name is not valid");
                }
                return uvs.get(idx);
            }
            throw new IllegalArgumentException("idxOrName must be a String or and Integer");
        }

        protected void addVertices(JsonArray vertices) {
            vertices.forEach(v -> {
                this.vertices.add(JsonUtil.getVector3(v.getAsJsonArray()));
            });
        }

        protected void addNamedVertices(JsonObject vertices) {
            for (var key : vertices.keySet()) {
                var val = vertices.get(key);
                var i = this.vertices.size();
                this.vertices.add(JsonUtil.getVector3(val.getAsJsonArray()));
                namedVertices.put(key, i);
            }
        }

        protected void addUvs(JsonArray uvs) {
            uvs.forEach(uv -> {
                this.uvs.add(JsonUtil.getVector2(uv.getAsJsonArray()).div(1024));
            });
        }

        protected void addNamedUvs(JsonObject uvs) {
            for (var key : uvs.keySet()) {
                var val = uvs.get(key);
                var i = this.uvs.size();
                this.uvs.add(JsonUtil.getVector2(val.getAsJsonArray()).div(1024));
                namedUvs.put(key, i);
            }
        }

        protected void addNormals(JsonArray normals) {
            normals.forEach(normal -> {
                this.normals.add(JsonUtil.getVector3(normal.getAsJsonArray()));
            });
        }

        protected void addNamedNormals(JsonObject normals) {
            for (var key : normals.keySet()) {
                var val = normals.get(key);
                var i = this.normals.size();
                this.normals.add(JsonUtil.getVector3(val.getAsJsonArray()));
                namedNormals.put(key, i);
            }
        }

        private Vector3f computeVertex(JsonObject json) {
            var points = json.getAsJsonArray("points");
            var ra = points.get(0).getAsJsonPrimitive();
            var rb = points.get(1).getAsJsonPrimitive();
            var a = getVertex(ra.isString() ? ra.getAsString() : ra.getAsInt());
            var b = getVertex(rb.isString() ? rb.getAsString() : rb.getAsInt());

            var delta = b.sub(a, new Vector3f());
            var vx = delta.x == 0 ? 0 : delta.x > 0 ? 1 : -1;
            var vy = delta.y == 0 ? 0 : delta.y > 0 ? 1 : -1;
            var vz = delta.z == 0 ? 0 : delta.z > 0 ? 1 : -1;

            var dt = JsonUtil.getOrDefault(json, "delta", JsonElement::getAsFloat, 0f);
            var f = Easing.getEasing(JsonUtil.getOrDefault(json, "function", JsonElement::getAsString, "easeLinear"));

            var point = MathUtil.lerpVector3(a, b, f.apply(dt));

            if (json.has("x")) {
                var x = json.get("x").getAsFloat();
                point.x = a.x + (vx * x);
                var dx = MathUtil.inverseLerp(0, b.x-a.x, vx * x);
                if (!json.has("y")) {
                    point.y = MathHelper.lerp(dx, a.y, b.y);
                }
                if (!json.has("z")) {
                    point.z = MathHelper.lerp(dx, a.z, b.z);
                }
            }

            if (json.has("y")) {
                var y = json.get("y").getAsFloat();
                point.y = a.y + (vy * y);
                var dy = MathUtil.inverseLerp(0, b.y-a.y, vy * y);
                if (!json.has("x")) {
                    point.x = MathHelper.lerp(dy, a.x, b.x);
                }
                if (!json.has("z")) {
                    point.z = MathHelper.lerp(dy, a.z, b.z);
                }
            }

            if (json.has("z")) {
                var z = json.get("z").getAsFloat();
                point.z = a.z + (vz * z);
                var dz = MathUtil.inverseLerp(0, b.z-a.z, vz * z);
                if (!json.has("x")) {
                    point.x = MathHelper.lerp(dz, a.x, b.x);
                }
                if (!json.has("y")) {
                    point.y = MathHelper.lerp(dz, a.y, b.y);
                }
            }

            return point;

        }

        protected void addComputeVertices(JsonObject json) {
            for (var key : json.keySet()) {
                var rawValue = json.get(key).getAsJsonObject();
                var vec = computeVertex(rawValue);
                var i = vertices.size();
                vertices.add(vec);
                namedVertices.put(key, i);
            }
        }

        private Vector3f computeNormal(JsonArray json) {
            var ra = json.get(0).getAsJsonPrimitive();
            var rb = json.get(1).getAsJsonPrimitive();
            var rc = json.get(2).getAsJsonPrimitive();
            var a = getVertex(ra.isString() ? ra.getAsString() : ra.getAsInt());
            var b = getVertex(rb.isString() ? rb.getAsString() : rb.getAsInt());
            var c = getVertex(rc.isString() ? rc.getAsString() : rc.getAsInt());

            var ab = b.sub(a, new Vector3f());
            var ac = c.sub(a, new Vector3f());
            return ab.cross(ac).normalize();
        }

        protected void addComputeNormals(JsonObject json) {
            for (var key : json.keySet()) {
                var rawValue = json.get(key).getAsJsonArray();
                var vec = computeNormal(rawValue);
                var i = normals.size();
                normals.add(vec);
                namedNormals.put(key, i);
            }
        }

    }

    public static LightMesh load(String name, Identifier source) throws IOException {

        var reader = MinecraftClient.getInstance().getResourceManager().getResource(source).orElseThrow().getReader();
        var rawJson = String.join("\n", reader.lines().toList());
        var json = JsonParser.parseString(rawJson).getAsJsonObject();

        var format = JsonUtil.getOrDefault(json, "mesh_format", JsonElement::getAsInt, 0);

        if (format != 1) {
            throw new IOException("Mesh is not in a known format");
        }

        var parts = new HashMap<String, MeshConstructor>();
        var textures = new HashMap<Integer, Identifier>();
        var data = new HashMap<String, TriangleData>();

        var rawData = json.get("data").getAsJsonObject();

        var defaultData = new TriangleData(0, 0, 0)
            .extend(rawData.get("default").getAsJsonObject());

        for (var key : rawData.keySet()) {
            var dat = rawData.get(key).getAsJsonObject();
            data.put(key, defaultData.extend(dat));
        }

        var rawTextures = json.get("textures").getAsJsonObject();
        for (int i = 0; i < 3; i++) {
            if (rawTextures.has(String.valueOf(i))) {
                textures.put(i, Identifier.tryParse(rawTextures.get(String.valueOf(i)).getAsString()));
            }
        }

        var rawPartsData = json.get("parts").getAsJsonObject();
        for (var partName : rawPartsData.keySet()) {
            var partData = rawPartsData.get(partName).getAsJsonObject();

            var builder = new MeshConstructor();
            parts.put(partName, builder);

            if (partData.has("vertices")) {
                builder.addVertices(partData.getAsJsonArray("vertices"));
            }
            if (partData.has("named_vertices")) {
                builder.addNamedVertices(partData.getAsJsonObject("named_vertices"));
            }
            if (partData.has("compute_vertices")) {
                builder.addComputeVertices(partData.getAsJsonObject("compute_vertices"));
            }

            if (partData.has("uvs")) {
                builder.addUvs(partData.getAsJsonArray("uvs"));
            }
            if (partData.has("named_uvs")) {
                builder.addNamedUvs(partData.getAsJsonObject("named_uvs"));
            }

            if (partData.has("normals")) {
                builder.addNormals(partData.getAsJsonArray("normals"));
            }
            if (partData.has("named_normals")) {
                builder.addNamedNormals(partData.getAsJsonObject("named_normals"));
            }
            if (partData.has("compute_normals")) {
                builder.addComputeNormals(partData.getAsJsonObject("compute_normals"));
            }

            var triangleDataList = partData.getAsJsonArray("triangles");

            var defaultUv = builder.getUv(0);
            Object defaultUvi = 0;
            var defaultNormal = builder.getNormal(0);
            Object defaultNormali = 0;

            for (var item : triangleDataList) {
                if (item.isJsonObject()) {
                    var obj = item.getAsJsonObject();
                    var uv = obj.get("uv");
                    if (uv.isJsonPrimitive() && uv.getAsJsonPrimitive().isNumber()) {
                        defaultUvi = uv.getAsInt();
                    } else {
                        defaultUvi = uv.getAsString();
                    }
                    defaultUv = builder.getUv(defaultUvi);

                    var n = obj.get("normal");
                    if (n.isJsonPrimitive() && n.getAsJsonPrimitive().isNumber()) {
                        defaultNormali = n.getAsInt();
                    } else {
                        defaultNormali = n.getAsString();
                    }
                    defaultNormal = builder.getNormal(defaultNormali);
                } else if (item.isJsonArray()) {
                    var arr = item.getAsJsonArray();
                    var ra = arr.get(0);
                    var rb = arr.get(1);
                    var rc = arr.get(2);

                    var a = parseData(builder, ra, defaultUv, defaultNormal);
                    var b = parseData(builder, rb, defaultUv, defaultNormal);
                    var c = parseData(builder, rc, defaultUv, defaultNormal);

                    if (arr.size() == 4) {
                        var mat = arr.get(3);
                        if (mat.isJsonObject()) {
                            var dat = defaultData.extend(mat.getAsJsonObject());
                            builder.addTriangle(new Triangle(a, b, c, dat, null));
                        } else {
                            builder.addTriangle(new Triangle(a, b, c, data.get(mat.getAsString()), mat.getAsString()));
                        }
                    } else if (arr.size() == 5) {
                        var baseMat = arr.get(3);
                        var modifier = arr.get(4);
                        builder.addTriangle(new Triangle(a, b, c, data.get(baseMat.getAsString()).extend(modifier.getAsJsonObject()), baseMat.getAsString()));
                    } else {
                        builder.addTriangle(new Triangle(a, b, c, defaultData, "default"));
                    }

                }
            }

        }

        var rawMesh = json.getAsJsonArray("mesh");
        var mesh = new LightMesh(textures);
        meshes.put(name, mesh);

        mesh.cullBackfaces = JsonUtil.getOrDefault(json, "cull", JsonElement::getAsBoolean, mesh.cullBackfaces);
        mesh.doBloom = JsonUtil.getOrDefault(json, "bloom_pass", JsonElement::getAsBoolean, mesh.doBloom);
        mesh.doMirroring = JsonUtil.getOrDefault(json, "mirror_pass", JsonElement::getAsBoolean, mesh.doMirroring);
        mesh.bloomfogStyle = JsonUtil.getOrDefault(json, "bloomfog_style", JsonElement::getAsInt, mesh.bloomfogStyle);
        mesh.doSolid = JsonUtil.getOrDefault(json, "solid_pass", JsonElement::getAsBoolean, mesh.doSolid);

        for (var dat : rawMesh) {
            var transform = dat.getAsJsonObject();
            var partName = transform.get("part").getAsString();
            var part = parts.get(partName);
            part.addToMesh(mesh, transform, data);
        }

        return mesh;

    }

    private static VertexData parseData(MeshConstructor builder, JsonElement vertex, Vector2f defaultUv, Vector3f defaultNormal) {
        if (vertex.isJsonArray()) {
            var data = vertex.getAsJsonArray();
            var v = data.get(0).getAsJsonPrimitive();
            var vec = builder.getVertex(v.isString() ? v.getAsString() : v.getAsInt());
            if (data.size() == 2) {
                var v0 = data.get(1).getAsJsonPrimitive();
                var uv = builder.getUv(v0.isString() ? v0.getAsString() : v0.getAsInt());
                return new VertexData(vec, uv, defaultNormal);
            } else {
                var v0 = data.get(1).getAsJsonPrimitive();
                var uv = builder.getUv(v0.isString() ? v0.getAsString() : v0.getAsInt());
                var v1 = data.get(2).getAsJsonPrimitive();
                var norm = builder.getNormal(v1.isString() ? v1.getAsString() : v1.getAsInt());
                return new VertexData(vec, uv, norm);
            }
        } else {
            var v = vertex.getAsJsonPrimitive();
            var vec = builder.getVertex(v.isString() ? v.getAsString() : v.getAsInt());
            return new VertexData(vec, defaultUv, defaultNormal);
        }
    }

}
