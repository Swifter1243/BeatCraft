package com.beatcraft.render.item;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.data.types.Color;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.gl.GlUtil;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.render.mesh.TriangleMesh;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GL31;

import java.io.IOException;
import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class SaberItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    public static class AttributedMesh {
        public TriangleMesh mesh;

        private boolean swivel = false;
        private int swivelAxis = 0b000;
        private final Vector3f swivelPivot;

        private boolean doBloom = false;
        private boolean tinted = false;

        private String shader = null;
        private int shaderProgram = 0;

        private int vao = -1;
        private int vbo = -1;
        private boolean initialized = false;

        private static final HashMap<String, String> shaderSources = new HashMap<>();

        private static final String[] sources = new String[]{
            "circle.fsh",
            "vertex.vsh"
        };
        public static void init() {

            for (var source : sources) {
                try {
                    var src = MinecraftClient.getInstance().getResourceManager().getResource(BeatCraft.id("shaders/saber/" + source))
                        .orElseThrow()
                        .getReader()
                        .lines().filter(x -> !x.contains("REMOVELINE"))
                        .collect(Collectors.joining("\n"));
                    shaderSources.put(source, src);
                } catch (IOException e) {
                    BeatCraft.LOGGER.error("Could not find saber shader '{}'", source);
                    throw new RuntimeException(e);
                }
            }

        }

        public void initCustom() {
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


        public boolean matchesAttributes(AttributedMesh other) {
            return (
                swivel == other.swivel &&
                swivelAxis == other.swivelAxis &&
                swivelPivot == other.swivelPivot &&
                doBloom == other.doBloom &&
                tinted == other.tinted &&
                Objects.equals(shader, other.shader)
            );
        }

        public AttributedMesh(TriangleMesh mesh, Vector3f swivelPivot, String attributes) {
            this.mesh = mesh;
            this.swivelPivot = swivelPivot;

            for (var attr : attributes.split(";")) {
                if (attr.equals("bloom")) {
                    doBloom = true;
                } else if (attr.equals("colored")) {
                    tinted = true;
                } else if (attr.startsWith("swivel:")) {
                    var axis = attr.replace("swivel:", "").toLowerCase();
                    swivelAxis = (
                        (axis.contains("x") ? 0b100 : 0b0) +
                        (axis.contains("y") ? 0b010 : 0b0) +
                        (axis.contains("z") ? 0b001 : 0b0)
                    );
                    swivel = true;
                } else if (attr.startsWith("shader:") && mesh != null) {
                    var shaderData = attr.replace("shader:", "");
                    var nameVars = shaderData.split(":", 2);
                    var shaderName = nameVars[0];
                    var shaderVars = nameVars[1];

                    this.shader = shaderName;

                    String[] vars = Arrays.stream(shaderVars.splitWithDelimiters("[a-zA-Z]+", 0)).filter(e -> !e.isBlank()).toArray(String[]::new);

                    var src = shaderSources.getOrDefault(shaderName + ".fsh", null);

                    var vert = shaderSources.get("vertex.vsh");

                    if (src == null) {
                        BeatCraft.LOGGER.error("Saber shader name '{}' is not valid", shaderName);
                        throw new RuntimeException("Saber shader name '" + shaderName + "' is not valid");
                    }

                    BeatCraft.LOGGER.info("shader vars: {}", (Object) vars);
                    for (int i = 0; i < vars.length; i += 2) {
                        var vName = vars[i];
                        var vVal = 0f;
                        try {
                            vVal = Float.parseFloat(vars[i + 1]);
                        } catch (NumberFormatException e) {
                            BeatCraft.LOGGER.error("Saber shader variable '{}' had non-parseable float value: '{}'", vName, vars[i+1]);
                        }

                        src = src.replace("${" + vName + "}", String.valueOf(vVal));

                    }

                    BeatCraft.LOGGER.info("generated shaders:\nvert:\n{}\nfrag:\n{}\n", vert, src);

                    shaderProgram = GlUtil.createShaderProgram(vert, src);
                    initCustom();
                }
            }

        }

        private void addVertex(List<Float> list, Vector3f pos, Vector2f uv, Vector4f color) {
            list.add(pos.x); list.add(pos.y); list.add(pos.z);
            list.add(uv.x); list.add(uv.y);
            list.add(color.x); list.add(color.y); list.add(color.z); list.add(color.w);
        }

        private void customRender(int c, Identifier texture, Matrix4f mts, Quaternionf ori, Quaternionf cameraRot) {

            if (shader.equals("circle")) {
                IntBuffer vaoBuf = BufferUtils.createIntBuffer(1);
                GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, vaoBuf);
                int oldVAO = vaoBuf.get(0);

                IntBuffer vboBuf = BufferUtils.createIntBuffer(1);
                GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, vboBuf);
                int oldVBO = vboBuf.get(0);


                GL31.glBindVertexArray(vao);

                GL31.glUseProgram(shaderProgram);

                var projMat = RenderSystem.getProjectionMatrix();
                var viewMat = RenderSystem.getModelViewMatrix();

                GlUtil.setMat4f(shaderProgram, "u_projection", projMat);
                GlUtil.setMat4f(shaderProgram, "u_view", viewMat);

                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.bindTexture(0);
                var col = new Color(c);

                var vColor = new Vector4f(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());

                List<Float> dataList = new ArrayList<>();

                for (var tri : mesh.tris) {
                    var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                    var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                    var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                    v0.sub(swivelPivot).rotate(ori).add(swivelPivot);
                    v1.sub(swivelPivot).rotate(ori).add(swivelPivot);
                    v2.sub(swivelPivot).rotate(ori).add(swivelPivot);

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
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.enableBlend();

                GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, vbo);
                GL31.glBufferData(GL31.GL_ARRAY_BUFFER, vertexBuffer, GL31.GL_DYNAMIC_DRAW);

                GL31.glDrawArrays(GL31.GL_TRIANGLES, 0, dataList.size() / 9);

                GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, oldVBO);
                GL31.glBindVertexArray(oldVAO);

            }
        }

        public void draw(Matrix4f matrices, int color, Vector3f cameraPos, BufferBuilder buffer, ArrayList<Runnable> afterCalls, Identifier texture) {

            var c = tinted ? color : 0xFFFFFFFF;

            Quaternionf currentOri = matrices.getUnnormalizedRotation(new Quaternionf());
            Quaternionf orientation = new Quaternionf();

            if (swivel) {
                Vector3f axisLocal = switch (swivelAxis) {
                    case 0b100 -> new Vector3f(1, 0, 0);
                    case 0b010 -> new Vector3f(0, 1, 0);
                    case 0b001 -> new Vector3f(0, 0, 1);
                    default -> new Vector3f();
                };

                Vector3f axisWorld = new Vector3f(axisLocal).normalize().rotate(currentOri);

                Vector3f modelWorldPos = new Vector3f(swivelPivot).mulPosition(matrices);

                Vector3f toModel = new Vector3f(modelWorldPos).negate();

                Vector3f projectedToModel = new Vector3f(toModel)
                    .sub(new Vector3f(axisWorld).mul(toModel.dot(axisWorld)))
                    .normalize();

                if (projectedToModel.lengthSquared() >= 1e-6f) {
                    orientation.identity();

                    Vector3f forwardWorld = new Vector3f(0, 0, -1).rotate(currentOri);

                    Vector3f projectedForward = new Vector3f(forwardWorld)
                        .sub(new Vector3f(axisWorld).mul(forwardWorld.dot(axisWorld)))
                        .normalize();

                    float dot = projectedForward.dot(projectedToModel);
                    dot = Math.max(-1.0f, Math.min(1.0f, dot));
                    float angle = (float) Math.acos(dot);

                    Vector3f cross = projectedForward.cross(projectedToModel, new Vector3f());
                    float sign = Math.signum(cross.dot(axisWorld));
                    angle *= sign;

                    orientation.rotateAxis(angle, axisLocal);
                }
            }

            if (shaderProgram == 0) {

                for (var tri : mesh.tris) {
                    var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                    var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                    var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                    v0.sub(swivelPivot).rotate(orientation).add(swivelPivot);
                    v1.sub(swivelPivot).rotate(orientation).add(swivelPivot);
                    v2.sub(swivelPivot).rotate(orientation).add(swivelPivot);

                    buffer.vertex(matrices, v0.x, v0.y, v0.z).texture(tri.uvA().x, tri.uvA().y).color(c);
                    buffer.vertex(matrices, v1.x, v1.y, v1.z).texture(tri.uvB().x, tri.uvB().y).color(c);
                    buffer.vertex(matrices, v2.x, v2.y, v2.z).texture(tri.uvC().x, tri.uvC().y).color(c);

                }
            } else {
                var mts = new Matrix4f(matrices);
                var ori = new Quaternionf(orientation);
                afterCalls.add(() -> customRender(c, texture, mts, ori, new Quaternionf()));
            }

            if (doBloom) {
                var mts = new Matrix4f(matrices);
                var ori = new Quaternionf(orientation);
                if (shaderProgram == 0) {
                    BeatCraftRenderer.bloomfog.recordMiscBloomCall((camPos, cameraRot) -> {
                        var b = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
                        for (var tri : mesh.tris) {
                            var v0 = new Vector3f(mesh.vertices.get(tri.a()));
                            var v1 = new Vector3f(mesh.vertices.get(tri.b()));
                            var v2 = new Vector3f(mesh.vertices.get(tri.c()));

                            v0.sub(swivelPivot).rotate(ori).add(swivelPivot);
                            v1.sub(swivelPivot).rotate(ori).add(swivelPivot);
                            v2.sub(swivelPivot).rotate(ori).add(swivelPivot);

                            mts.transformPosition(v0);
                            mts.transformPosition(v1);
                            mts.transformPosition(v2);

                            v0.rotate(cameraRot);
                            v1.rotate(cameraRot);
                            v2.rotate(cameraRot);

                            b.vertex(v0.x, v0.y, v0.z).texture(tri.uvA().x, tri.uvA().y).color(c);
                            b.vertex(v1.x, v1.y, v1.z).texture(tri.uvB().x, tri.uvB().y).color(c);
                            b.vertex(v2.x, v2.y, v2.z).texture(tri.uvC().x, tri.uvC().y).color(c);

                        }

                        var buff = b.endNullable();

                        if (buff != null) {
                            RenderSystem.setShaderTexture(0, texture);
                            RenderSystem.bindTexture(0);
                            RenderSystem.setShader(() -> BeatCraftRenderer.BCPosTexColShader);
                            BufferRenderer.drawWithGlobalProgram(buff);
                        }

                    });
                } else {
                    BeatCraftRenderer.bloomfog.recordMiscBloomCall((v, q) -> customRender(c, texture, mts, ori, q));
                }
            }

        }

        public void destroy() {
            if (shaderProgram != 0) {
                GL31.glDeleteProgram(shaderProgram);
            }
        }

    }

    public static class SaberModel {

        public final String modelName;
        public final List<String> authors;
        private final ArrayList<AttributedMesh> meshComponents;
        public final int performanceImpactScore;

        private final Identifier texture;

        public SaberModel(String modelName, List<String> authors, ArrayList<AttributedMesh> meshComponents, int performanceImpactScore, Identifier texture) {
            this.modelName = modelName;
            this.authors = authors;
            this.meshComponents = meshComponents;
            this.performanceImpactScore = performanceImpactScore;
            this.texture = texture;
        }

        public void render(Matrix4f matrices, int color, Vector3f cameraPos) {

            var tessellator = Tessellator.getInstance();
            var buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

            var afterCalls = new ArrayList<Runnable>();

            for (var mesh : meshComponents) {
                mesh.draw(matrices, color, cameraPos, buffer, afterCalls, texture);
            }

            var buff = buffer.endNullable();
            if (buff != null) {

                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableBlend();

                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.bindTexture(0);
                RenderSystem.setShader(() -> BeatCraftRenderer.BCPosTexColShader);

                BufferRenderer.drawWithGlobalProgram(buff);

            }

            afterCalls.forEach(c -> {
                try {
                    c.run();
                } catch (Exception e) {
                    BeatCraft.LOGGER.error("Saber Model late callback errored!", e);
                    throw new RuntimeException(e);
                }
            });

        }

    }

    private static ArrayList<SaberModel> models = new ArrayList<>();

    private static SaberModel active = null;

    private static boolean initialized = false;

    public static void init() {
        AttributedMesh.init();

        var builtin = MeshLoader.loadSaberMesh(BeatCraft.id("saber/builtin_saber.json"), BeatCraft.id("textures/item/saber.png"));
        models.add(builtin);

        active = builtin;
        initialized = true;
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (!initialized) init();

        var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        int color;

        int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR, -1);

        if (sync == -1 || BeatmapPlayer.currentBeatmap == null) {
            color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0) + 0xFF000000;
        } else if (sync == 0) {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
        } else {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        }

        active.render(matrices.peek().getPositionMatrix(), color, cameraPos);

    }
}
