package com.beatcraft.render.item;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.memory.MemoryPool;
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
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
                    var nameVars = shaderData.split(":", 1);
                    var shaderName = nameVars[0];
                    var shaderVars = nameVars[1];

                    this.shader = shaderName;

                    var vars = shaderVars.splitWithDelimiters("[a-zA-Z]+", 0);

                    var src = shaderSources.getOrDefault(shaderName + ".fsh", null);

                    var vert = shaderSources.get("vertex.vsh");

                    if (src == null) {
                        BeatCraft.LOGGER.error("Saber shader name '{}' is not valid", shaderName);
                        throw new RuntimeException("Saber shader name '" + shaderName + "' is not valid");
                    }

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

                    shaderProgram = GlUtil.createShaderProgram(vert, src);

                }
            }

        }

        public void draw(Matrix4f matrices, int color, Vector3f cameraPos, BufferBuilder buffer, ArrayList<Runnable> afterCalls) {

            var c = tinted ? color : 0xFFFFFFFF;

            Quaternionf orientation = new Quaternionf();

            if (swivel) {
                Vector3f objectPos = matrices.getTranslation(new Vector3f());
                Vector3f toCamera = new Vector3f(cameraPos).sub(objectPos).normalize();

                Vector3f axis = new Vector3f();
                if ((swivelAxis & 0b001) != 0) axis.x = 1;
                if ((swivelAxis & 0b010) != 0) axis.y = 1;
                if ((swivelAxis & 0b100) != 0) axis.z = 1;

                if (axis.lengthSquared() > 0) {
                    axis.normalize();

                    Vector3f projected = new Vector3f(toCamera).sub(
                        new Vector3f(axis).mul(toCamera.dot(axis))
                    ).normalize();

                    Vector3f forward = new Vector3f(0, 0, -1); // or (0, 0, 1) depending on your system

                    orientation = new Quaternionf().rotateTo(forward, projected);
                }
            }

            for (var tri : mesh.tris) {
                var v0 = MemoryPool.newVector3f(mesh.vertices.get(tri.a()));
                var v1 = MemoryPool.newVector3f(mesh.vertices.get(tri.b()));
                var v2 = MemoryPool.newVector3f(mesh.vertices.get(tri.c()));

                v0.sub(swivelPivot).rotate(orientation).add(swivelPivot);
                v1.sub(swivelPivot).rotate(orientation).add(swivelPivot);
                v2.sub(swivelPivot).rotate(orientation).add(swivelPivot);

                buffer.vertex(matrices, v0.x, v0.y, v0.z).color(c).texture(tri.uvA().x, tri.uvA().y);
                buffer.vertex(matrices, v1.x, v1.y, v1.z).color(c).texture(tri.uvB().x, tri.uvB().y);
                buffer.vertex(matrices, v2.x, v2.y, v2.z).color(c).texture(tri.uvC().x, tri.uvC().y);


                MemoryPool.release(v0, v1, v2);
            }


        }

        public void destroy() {
            if (shaderProgram != 0) {
                GL30.glDeleteProgram(shaderProgram);
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
                mesh.draw(matrices, color, cameraPos, buffer, afterCalls);
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

                BufferRenderer.drawWithGlobalProgram(buff);

                RenderSystem.disableCull();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableBlend();

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
