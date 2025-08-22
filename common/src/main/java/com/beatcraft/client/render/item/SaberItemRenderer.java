package com.beatcraft.client.render.item;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
//import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.services.VivecraftClientInterface;
import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.gl.GlUtil;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.client.render.mesh.TriangleMesh;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GL31;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SaberItemRenderer {

    public static class AttributedMesh {
        public TriangleMesh mesh;

        private boolean swivel = false;
        private Vector3f swivelAxis;
        private final Vector3f pivot;

        private boolean spin = false;
        private Vector3f spinAxis;
        private float spinSecondsPerRev;

        private boolean doBloom = false;
        private boolean tinted = false;

        private String shader = null;
        private int shaderProgram = 0;

        private int vao = -1;
        private int vbo = -1;

        private int bloom_vao = -1;
        private int bloom_vbo = -1;
        private int bloomShaderProgram = 0;
        private String[] shaderVariables;

        private static final HashMap<String, String> shaderSources = new HashMap<>();

        private static final String[] sources = new String[]{
            "circle.fsh",
            "circle_bloom_mask.fsh",
            "vertex.vsh"
        };
        public static void init() {

            for (var source : sources) {
                try {
                    var src = Minecraft.getInstance().getResourceManager().getResource(Beatcraft.id("shaders/saber/" + source))
                        .orElseThrow()
                        .openAsReader()
                        .lines().filter(x -> !x.contains("REMOVELINE"))
                        .collect(Collectors.joining("\n"));
                    shaderSources.put(source, src);
                } catch (IOException e) {
                    Beatcraft.LOGGER.error("Could not find saber shader '{}'", source);
                    throw new RuntimeException(e);
                }
            }

        }

        private void initCustom() {
            vao = GL31.glGenVertexArrays();
            vbo = GL31.glGenBuffers();

            GL31.glBindVertexArray(vao);
            GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, vbo);

            int stride = (3 + 2 + 4) * Float.BYTES;

            GL31.glEnableVertexAttribArray(0);
            GL31.glVertexAttribPointer(0, 3, GL31.GL_FLOAT, false, stride, 0);

            GL31.glEnableVertexAttribArray(1);
            GL31.glVertexAttribPointer(1, 2, GL31.GL_FLOAT, false, stride, 3 * Float.BYTES);

            GL31.glEnableVertexAttribArray(2);
            GL31.glVertexAttribPointer(2, 4, GL31.GL_FLOAT, false, stride, (3 + 2) * Float.BYTES);

            GL31.glBindVertexArray(0);
        }

        private void initCustomBloom() {
            bloom_vao = GL31.glGenVertexArrays();
            bloom_vbo = GL31.glGenBuffers();

            GL31.glBindVertexArray(bloom_vao);
            GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, bloom_vbo);

            int stride = (3 + 2 + 4) * Float.BYTES;

            GL31.glEnableVertexAttribArray(0);
            GL31.glVertexAttribPointer(0, 3, GL31.GL_FLOAT, false, stride, 0);

            GL31.glEnableVertexAttribArray(1);
            GL31.glVertexAttribPointer(1, 2, GL31.GL_FLOAT, false, stride, 3 * Float.BYTES);

            GL31.glEnableVertexAttribArray(2);
            GL31.glVertexAttribPointer(2, 4, GL31.GL_FLOAT, false, stride, (3 + 2) * Float.BYTES);

            GL31.glBindVertexArray(0);
        }


        public boolean matchesAttributes(AttributedMesh other) {
            return (
                swivel == other.swivel &&
                    swivelAxis == other.swivelAxis &&
                    pivot == other.pivot &&
                    doBloom == other.doBloom &&
                    tinted == other.tinted &&
                    Objects.equals(shader, other.shader)
            );
        }

        // Matches: xFLOATyFLOATzFLOAT
        private static final Pattern axesPattern = Pattern.compile("(?:x(?<x>[+-]?(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?[fF]?)?)?(?:y(?<y>[+-]?(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?[fF]?)?)?(?:z(?<z>[+-]?(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?[fF]?)?)?");

        private static Vector3f getAxis(String in) {
            var matcher = axesPattern.matcher(in);

            var x = in.contains("x") ? 1f : 0f;
            var y = in.contains("y") ? 1f : 0f;
            var z = in.contains("z") ? 1f : 0f;

            if (matcher.find()) {
                var rx = matcher.group("x");
                var ry = matcher.group("y");
                var rz = matcher.group("z");
                if (rx != null) x = Float.parseFloat(rx);
                if (ry != null) y = Float.parseFloat(ry);
                if (rz != null) z = Float.parseFloat(rz);
            }

            return new Vector3f(x, y, z).normalize();
        }

        public AttributedMesh(TriangleMesh mesh, Vector3f pivot, String attributes) {
            this.mesh = mesh;
            this.pivot = pivot;

            for (var attr : attributes.split(";")) {
                if (attr.equals("bloom")) {
                    doBloom = true;
                } else if (attr.equals("colored")) {
                    tinted = true;
                } else if (attr.startsWith("swivel:")) {
                    var axis = attr.replace("swivel:", "").toLowerCase();
                    swivelAxis = getAxis(axis);
                    swivel = true;
                } else if (attr.startsWith("spin:")) {
                    var axisSpeed = attr.replace("spin:", "").toLowerCase().split(":");
                    var axis = axisSpeed[0];
                    spinAxis = getAxis(axis);
                    spinSecondsPerRev = Float.parseFloat(axisSpeed[1]);
                    spin = true;
                } else if (attr.startsWith("shader:") && mesh != null) {
                    var shaderData = attr.replace("shader:", "");
                    var nameVars = shaderData.split(":", 2);
                    var shaderName = nameVars[0];
                    var shaderVars = nameVars[1];

                    this.shader = shaderName;

                    shaderVariables = Arrays.stream(shaderVars.splitWithDelimiters("[a-zA-Z]+", 0)).filter(e -> !e.isBlank()).toArray(String[]::new);

                    var src = shaderSources.getOrDefault(shaderName + ".fsh", null);

                    var vert = shaderSources.get("vertex.vsh");

                    if (src == null) {
                        Beatcraft.LOGGER.error("Saber shader name '{}' is not valid", shaderName);
                        throw new RuntimeException("Saber shader name '" + shaderName + "' is not valid");
                    }

                    for (int i = 0; i < shaderVariables.length; i += 2) {
                        var vName = shaderVariables[i];
                        var vVal = 0f;
                        try {
                            vVal = Float.parseFloat(shaderVariables[i + 1]);
                        } catch (NumberFormatException e) {
                            Beatcraft.LOGGER.error("Saber shader variable '{}' had non-parseable float value: '{}'", vName, shaderVariables[i+1]);
                        }

                        src = src.replace("${" + vName + "}", String.valueOf(vVal));

                    }

                    shaderProgram = GlUtil.createShaderProgram(vert, src);
                    initCustom();
                }
            }

            if (shaderProgram != 0 && doBloom) {
                var src = shaderSources.getOrDefault(shader + "_bloom_mask.fsh", null);

                var vert = shaderSources.get("vertex.vsh");

                for (int i = 0; i < shaderVariables.length; i += 2) {
                    var vName = shaderVariables[i];
                    var vVal = 0f;
                    try {
                        vVal = Float.parseFloat(shaderVariables[i + 1]);
                    } catch (NumberFormatException e) {
                        Beatcraft.LOGGER.error("Saber bloom shader variable '{}' had non-parseable float value: '{}'", vName, shaderVariables[i+1]);
                    }

                    src = src.replace("${" + vName + "}", String.valueOf(vVal));

                }

                bloomShaderProgram = GlUtil.createShaderProgram(vert, src);
                initCustomBloom();
            }

        }

        private void addVertex(List<Float> list, Vector3f pos, Vector2f uv, Vector4f color) {
            list.add(pos.x); list.add(pos.y); list.add(pos.z);
            list.add(uv.x); list.add(uv.y);
            list.add(color.x); list.add(color.y); list.add(color.z); list.add(color.w);
        }

        private void customRender(int c, ResourceLocation texture, Matrix4f mts, Quaternionf ori, Quaternionf cameraRot, boolean isBloom, int depthBuffer) {

            var vVao = isBloom ? bloom_vao : vao;
            var vVbo = isBloom ? bloom_vbo : vbo;
            var program = isBloom ? bloomShaderProgram : shaderProgram;

            if (shader.equals("circle")) {
                IntBuffer vaoBuf = BufferUtils.createIntBuffer(1);
                GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, vaoBuf);
                int oldVAO = vaoBuf.get(0);

                IntBuffer vboBuf = BufferUtils.createIntBuffer(1);
                GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, vboBuf);
                int oldVBO = vboBuf.get(0);


                GL31.glBindVertexArray(vVao);

                GL31.glUseProgram(program);

                var projMat = RenderSystem.getProjectionMatrix();
                var viewMat = RenderSystem.getModelViewMatrix();

                GlUtil.setMat4f(program, "u_projection", projMat);
                GlUtil.setMat4f(program, "u_view", viewMat);

                RenderSystem.setShaderTexture(0, texture);

                if (isBloom) {
                    GlUtil.setTex(program, "u_depth", 1, depthBuffer);
                }

                var col = new Color(c);

                var vColor = new Vector4f(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());

                List<Float> dataList = new ArrayList<>();

                for (var tri : mesh.tris) {
                    var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                    var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                    var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                    v0.sub(pivot).rotate(ori).add(pivot);
                    v1.sub(pivot).rotate(ori).add(pivot);
                    v2.sub(pivot).rotate(ori).add(pivot);

                    mts.transformPosition(v0);
                    mts.transformPosition(v1);
                    mts.transformPosition(v2);

                    v0.rotate(cameraRot);
                    v1.rotate(cameraRot);
                    v2.rotate(cameraRot);

                    var uv0 = tri.uvA();
                    var uv1 = tri.uvB();
                    var uv2 = tri.uvC();

                    addVertex(dataList, v0, uv0, vColor);
                    addVertex(dataList, v1, uv1, vColor);
                    addVertex(dataList, v2, uv2, vColor);
                }

                FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(dataList.size());
                for (float f : dataList) vertexBuffer.put(f);
                vertexBuffer.flip();

                RenderSystem.enableCull();
                if (!isBloom) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(true);
                }

                RenderSystem.enableBlend();

                GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, vVbo);
                GL31.glBufferData(GL31.GL_ARRAY_BUFFER, vertexBuffer, GL31.GL_DYNAMIC_DRAW);

                GL31.glDrawArrays(GL31.GL_TRIANGLES, 0, dataList.size() / 9);

                GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, oldVBO);
                GL31.glBindVertexArray(oldVAO);

            }
        }

        public void draw(Matrix4f matrices, int color, Vector3f cameraPos, BufferBuilder buffer, ArrayList<Runnable> afterCalls, ResourceLocation texture, boolean bypassBloom) {

            var c = tinted ? color : 0xFFFFFFFF;

            Quaternionf currentOri = matrices.getUnnormalizedRotation(new Quaternionf());
            Quaternionf orientation = new Quaternionf();

            if (spin) {
                double s = System.nanoTime() / 1_000_000_000d;
                double revs = s / spinSecondsPerRev;
                float angle = (float) ((revs * 2d * Math.PI) % (2d * Math.PI));
                orientation.rotateAxis(angle, spinAxis);
            }


            if (swivel) {

                Vector3f axisWorld = new Vector3f(swivelAxis).normalize().rotate(currentOri);

                Vector3f modelWorldPos = new Vector3f(pivot).mulPosition(matrices);

                Vector3f toModel = new Vector3f(modelWorldPos).negate();

                Vector3f projectedToModel = new Vector3f(toModel)
                    .sub(new Vector3f(axisWorld).mul(toModel.dot(axisWorld)))
                    .normalize();

                if (projectedToModel.lengthSquared() >= 1e-6f) {
                    orientation.identity();
                    Vector3f forwardWorld;
                    if (swivelAxis.x == 0 && swivelAxis.y == 0 && swivelAxis.z != 0) {
                        forwardWorld = new Vector3f(0, 1, 0).rotate(currentOri);
                    } else {
                        forwardWorld = new Vector3f(0, 0, -1).rotate(currentOri);
                    }

                    Vector3f projectedForward = new Vector3f(forwardWorld)
                        .sub(new Vector3f(axisWorld).mul(forwardWorld.dot(axisWorld)))
                        .normalize();

                    float dot = projectedForward.dot(projectedToModel);
                    dot = Math.max(-1.0f, Math.min(1.0f, dot));
                    float angle = (float) Math.acos(dot);

                    Vector3f cross = projectedForward.cross(projectedToModel, new Vector3f());
                    float sign = Math.signum(cross.dot(axisWorld));
                    angle *= sign;

                    orientation.rotateAxis(angle, swivelAxis);
                }
            }

            if (shaderProgram == 0) {

                for (var tri : mesh.tris) {
                    var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                    var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                    var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                    v0.sub(pivot).rotate(orientation).add(pivot);
                    v1.sub(pivot).rotate(orientation).add(pivot);
                    v2.sub(pivot).rotate(orientation).add(pivot);

                    buffer.addVertex(matrices, v0.x, v0.y, v0.z).setUv(tri.uvA().x, tri.uvA().y).setColor(c);
                    buffer.addVertex(matrices, v1.x, v1.y, v1.z).setUv(tri.uvB().x, tri.uvB().y).setColor(c);
                    buffer.addVertex(matrices, v2.x, v2.y, v2.z).setUv(tri.uvC().x, tri.uvC().y).setColor(c);

                }
            } else {
                var mts = new Matrix4f(matrices);
                var ori = new Quaternionf(orientation);
                afterCalls.add(() -> customRender(c, texture, mts, ori, new Quaternionf(), false, 0));
            }

            var vrActive = (VivecraftClientInterface.isVRNonNull() && VivecraftClientInterface.isVRActive());

            if (doBloom && !(bypassBloom && !vrActive)) {
                var mts = new Matrix4f(matrices);
                var ori = new Quaternionf(orientation);
                if (shaderProgram == 0) {
                    BeatcraftRenderer.bloomfog.recordMiscBloomCall((camPos, cameraRot, depthBuffer) -> {
                        var b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
                        for (var tri : mesh.tris) {
                            var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                            var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                            var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                            v0.sub(pivot).rotate(ori).add(pivot);
                            v1.sub(pivot).rotate(ori).add(pivot);
                            v2.sub(pivot).rotate(ori).add(pivot);

                            mts.transformPosition(v0);
                            mts.transformPosition(v1);
                            mts.transformPosition(v2);

                            v0.rotate(cameraRot);
                            v1.rotate(cameraRot);
                            v2.rotate(cameraRot);

                            b.addVertex(v0.x, v0.y, v0.z).setUv(tri.uvA().x, tri.uvA().y).setColor(c);
                            b.addVertex(v1.x, v1.y, v1.z).setUv(tri.uvB().x, tri.uvB().y).setColor(c);
                            b.addVertex(v2.x, v2.y, v2.z).setUv(tri.uvC().x, tri.uvC().y).setColor(c);

                        }

                        var buff = b.build();

                        if (buff != null) {

                            RenderSystem.setShaderTexture(0, texture);
                            RenderSystem.setShaderTexture(1, depthBuffer);
                            RenderSystem.enableBlend();
                            RenderSystem.setShader(() -> Bloomfog.bloomMaskLightTextureShader);
                            BufferUploader.drawWithShader(buff);

                        }

                    });
                } else {
                    BeatcraftRenderer.bloomfog.recordMiscBloomCall((v, q, d) -> customRender(c, texture, mts, ori, q, true, d));
                }
            }

        }

        public void destroy() {
            if (shaderProgram != 0) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
                GL15.glDeleteBuffers(vbo);
                GL30.glDeleteVertexArrays(vao);
                GL31.glDeleteProgram(shaderProgram);
                if (doBloom && bloomShaderProgram != 0) {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                    GL30.glBindVertexArray(0);
                    GL15.glDeleteBuffers(bloom_vbo);
                    GL30.glDeleteVertexArrays(bloom_vao);
                    GL32.glDeleteProgram(bloomShaderProgram);
                }
            }

        }

    }

    public static class SaberModel {

        public final String id;
        public final String modelName;
        public final List<String> authors;
        private final ArrayList<AttributedMesh> meshComponents;
        public final int performanceImpactScore;

        private final ResourceLocation texture;

        public SaberModel(String id, String modelName, List<String> authors, ArrayList<AttributedMesh> meshComponents, int performanceImpactScore, ResourceLocation texture) {
            this.id = id;
            this.modelName = modelName;
            this.authors = authors;
            this.meshComponents = meshComponents;
            this.performanceImpactScore = performanceImpactScore;
            this.texture = texture;
        }

        public void render(Matrix4f matrices, int color, Vector3f cameraPos, boolean bypassBloom) {

            var tessellator = Tesselator.getInstance();
            var buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

            var afterCalls = new ArrayList<Runnable>();

            for (var mesh : meshComponents) {
                mesh.draw(matrices, color, cameraPos, buffer, afterCalls, texture, bypassBloom);
            }

            var buff = buffer.build();
            if (buff != null) {

                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableBlend();

                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.bindTexture(0);
                RenderSystem.setShader(() -> BeatcraftRenderer.BCPosTexColShader);

                BufferUploader.drawWithShader(buff);

            }

            afterCalls.forEach(c -> {
                try {
                    c.run();
                } catch (Exception e) {
                    Beatcraft.LOGGER.error("Saber Model late callback errored!", e);
                    throw new RuntimeException(e);
                }
            });

        }

        public void destroy() {
            meshComponents.forEach(AttributedMesh::destroy);
            meshComponents.clear();
        }

    }

    public static final ArrayList<SaberModel> models = new ArrayList<>();
    private static SaberModel active = null;
    private static SaberModel builtin = null;

    private static boolean initialized = false;

    private static void loadCustomModels() {
        String rootModelFolder = Minecraft.getInstance().gameDirectory.toPath() + "/beatcraft/custom_sabers/";

        File folder = new File(rootModelFolder);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Beatcraft.LOGGER.error("Failed to create custom sabers folder");
                return;
            }
        }

        File[] modelFolders = folder.listFiles(File::isDirectory);

        if (modelFolders == null) {
            Beatcraft.LOGGER.error("Failed to load custom sabers");
            return;
        }

        models.forEach(SaberModel::destroy);
        models.clear();

        for (File modelFolder : modelFolders) {
            var pngs = modelFolder.listFiles(f -> f.isFile() && f.getAbsolutePath().endsWith(".png"));
            var models = modelFolder.listFiles(f -> f.isFile() && f.getAbsolutePath().endsWith(".json"));

            if (models == null) {
                Beatcraft.LOGGER.error("No model files found in model folder. make sure models are in blockbench's java block/item json format");
                continue;
            }
            if (pngs == null) {
                Beatcraft.LOGGER.error("No image files found in model folder. check that images are valid png files");
                continue;
            }

            var textureLookup = new HashMap<String, File>();

            for (var png : pngs) {
                var name = png.getName().replaceFirst("\\.png$", "");
                textureLookup.put(name, png);
            }

            for (var modelFile : models) {
                var model = MeshLoader.loadSaberMesh(modelFile.getAbsolutePath(), textureLookup);
                if (model == null) continue;
                SaberItemRenderer.models.add(model);
            }
        }

        var legacy = MeshLoader.loadSaberMesh(Beatcraft.id("saber/legacy.json"), Beatcraft.id("textures/item/saber.png"));
        models.add(legacy);
        legacy = MeshLoader.loadSaberMesh(Beatcraft.id("saber/legacy_updated.json"), Beatcraft.id("textures/item/saber.png"));
        models.add(legacy);

    }

    public static void selectModel(String id) {
        var found = false;
        for (var model : models) {
            if (model.id.equals(id)) {
                active = model;
                found = true;
                break;
            }
        }
        if (!found) {
            active = builtin;
        }
        BeatcraftClient.playerConfig.preferences.selectedSaber(active.id);
    }

    public static void init() {
        AttributedMesh.init();

        loadCustomModels();

        builtin = MeshLoader.loadSaberMesh(Beatcraft.id("saber/builtin_saber.json"), Beatcraft.id("textures/item/saber.png"));
        models.add(builtin);

        selectModel(BeatcraftClient.playerConfig.preferences.selectedSaber());

        initialized = true;
    }

    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {

        if (!initialized) init();

        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        int color;

        int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR.get(), -1);

        if (sync == -1 || BeatmapManager.hasNearbyBeatmapToPlayer()) {
            color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT.get(), 0) + 0xFF000000;
        } else if (sync == 0) {
            color = BeatmapManager.nearestBeatmapToPlayer().difficulty.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
        } else {
            color = BeatmapManager.nearestBeatmapToPlayer().difficulty.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        }

        active.render(matrices.last().pose(), color, cameraPos, mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || mode == ItemDisplayContext.GUI);

    }
}