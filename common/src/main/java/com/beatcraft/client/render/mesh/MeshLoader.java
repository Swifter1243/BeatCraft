package com.beatcraft.client.render.mesh;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.lightshow.environment.kaleidoscope.RingSpike;
import com.beatcraft.client.render.instancing.debug.TransformationWidgetInstanceData;
import com.beatcraft.mixin_utils.ModelLoaderAccessor;
import com.beatcraft.client.render.dynamic_loader.DynamicTexture;
import com.beatcraft.client.render.instancing.*;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.item.SaberItemRenderer;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import oshi.util.tuples.Triplet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class MeshLoader {

    public static InstancedMesh<ColorNoteInstanceData> COLOR_NOTE_INSTANCED_MESH;
    public static InstancedMesh<BombNoteInstanceData> BOMB_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ColorNoteInstanceData> CHAIN_HEAD_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ColorNoteInstanceData> CHAIN_LINK_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> NOTE_ARROW_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> NOTE_DOT_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> CHAIN_DOT_INSTANCED_MESH;

    public static InstancedMesh<ColorNoteInstanceData> MIRROR_COLOR_NOTE_INSTANCED_MESH;
    public static InstancedMesh<BombNoteInstanceData> MIRROR_BOMB_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ColorNoteInstanceData> MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ColorNoteInstanceData> MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> MIRROR_NOTE_ARROW_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> MIRROR_NOTE_DOT_INSTANCED_MESH;
    public static InstancedMesh<ArrowInstanceData> MIRROR_CHAIN_DOT_INSTANCED_MESH;

    public static InstancedMesh<HeadsetInstanceData> HEADSET_INSTANCED_MESH;

    public static final ResourceLocation NOTE_TEXTURE = Beatcraft.id("textures/gameplay_objects/color_note.png");
    public static final ResourceLocation ARROW_TEXTURE = Beatcraft.id("textures/gameplay_objects/arrow.png");
    public static final ResourceLocation SMOKE_TEXTURE = Beatcraft.id("textures/noise/smoke.png");
    public static final ResourceLocation MATRIX_LOCATOR_TEXTURE = Beatcraft.id("textures/debug/matrix_visualizer.png");
    public static final ResourceLocation HEADSET_TEXTURE = Beatcraft.id("textures/item/headset.png");

    public static InstancedMesh<SmokeInstanceData> SMOKE_INSTANCED_MESH;

    public static InstancedMesh<TransformationWidgetInstanceData> MATRIX_LOCATOR_MESH;

    private static ModelLoaderAccessor modelLoader;

    public static LightMesh KALEIDOSCOPE_SPIKE;

    public static void loadMeshes() {
        COLOR_NOTE_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/color_note.json"), NOTE_TEXTURE, "instanced/color_note", 1f);
        CHAIN_HEAD_NOTE_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/color_note_chain_head.json"), NOTE_TEXTURE, "instanced/color_note", 1f);
        CHAIN_LINK_NOTE_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/color_note_chain_link.json"), NOTE_TEXTURE, "instanced/color_note", 1f);
        BOMB_NOTE_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/bomb_note.json"), NOTE_TEXTURE, "instanced/bomb_note", 1f);
        NOTE_ARROW_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/note_arrow.json"), ARROW_TEXTURE, "instanced/arrow", 1f);
        NOTE_DOT_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/note_dot.json"), ARROW_TEXTURE, "instanced/arrow", 1f);
        CHAIN_DOT_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/chain_note_dot.json"), ARROW_TEXTURE, "instanced/arrow", 1f);

        HEADSET_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/item/headset.json"), HEADSET_TEXTURE, "instanced/headset", 1f);

        MIRROR_COLOR_NOTE_INSTANCED_MESH = COLOR_NOTE_INSTANCED_MESH.copy();
        MIRROR_BOMB_NOTE_INSTANCED_MESH = BOMB_NOTE_INSTANCED_MESH.copy();
        MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH = CHAIN_HEAD_NOTE_INSTANCED_MESH.copy();
        MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH = CHAIN_LINK_NOTE_INSTANCED_MESH.copy();
        MIRROR_NOTE_ARROW_INSTANCED_MESH = NOTE_ARROW_INSTANCED_MESH.copy();
        MIRROR_NOTE_DOT_INSTANCED_MESH = NOTE_DOT_INSTANCED_MESH.copy();
        MIRROR_CHAIN_DOT_INSTANCED_MESH = CHAIN_DOT_INSTANCED_MESH.copy();

        SMOKE_INSTANCED_MESH = loadInstancedMesh(Beatcraft.id("models/gameplay/smoke.json"), SMOKE_TEXTURE, "instanced/smoke", 6f);

        MATRIX_LOCATOR_MESH = loadInstancedMesh(Beatcraft.id("models/debug/matrix_visualizer.json"), MATRIX_LOCATOR_TEXTURE, "debug/matrix_visualizer", 1f);


        try {
            KALEIDOSCOPE_SPIKE = LightMesh.load("kaleidoscope_spike", Beatcraft.id("meshes/environment/kaleidoscope/spikes.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void init(ModelLoaderAccessor modelLoader) {
        MeshLoader.modelLoader = modelLoader;
    }

    protected static class UnboundJsonModel {
        protected record UnboundJsonRotation(int axis, float angle, Vector3f origin) {
            protected static UnboundJsonRotation parse(JsonObject json) {
                if (json == null) {
                    return new UnboundJsonRotation(0, 0, new Vector3f());
                }
                var ax = json.get("axis").getAsString();
                return new UnboundJsonRotation(
                    ax.equals("x") ? 0 : ax.equals("y") ? 1 : 2,
                    json.get("angle").getAsFloat(),
                    JsonUtil.getVector3(json.getAsJsonArray("origin")).mul(1f/16f)
                );
            }
        }
        protected static class UnboundJsonFace {
            protected UnboundTextureData texData;
            protected UnboundTextureData textureData() {
                return texData;
            }
            private static final String[] directions = new String[]{
                "north", "east", "south",
                "west", "up", "down"
            };
            protected static Map<Direction, UnboundJsonFace> parseFaces(JsonObject json) {
                var map = new HashMap<Direction, UnboundJsonFace>();
                for (var face : directions) {
                    if (json.has(face)) {
                        map.put(Direction.byName(face), parseFace(json.getAsJsonObject(face)));
                    }
                }
                return map;
            }
            protected static UnboundJsonFace parseFace(JsonObject json) {
                var f = new UnboundJsonFace();
                f.texData = new UnboundTextureData();
                var rawUvs = json.getAsJsonArray("uv");
                f.texData.uvs = new float[]{
                    rawUvs.get(0).getAsFloat(),
                    rawUvs.get(1).getAsFloat(),
                    rawUvs.get(2).getAsFloat(),
                    rawUvs.get(3).getAsFloat()
                };
                f.texData.rotation = JsonUtil.getOrDefault(json, "rotation", JsonElement::getAsFloat, 0f);
                return f;
            }
        }
        protected static class UnboundTextureData {
            float rotation;
            float[] uvs;
        }

        protected static class UnboundJsonElement {
            protected Vector3f from;
            protected Vector3f to;
            protected UnboundJsonRotation rotation;
            protected Map<Direction, UnboundJsonFace> faces;
        }

        private final List<UnboundJsonElement> elements;

        protected List<UnboundJsonElement> getElements() {
            return elements;
        }

        protected UnboundJsonModel(ResourceLocation identifier) throws IOException {
            var reader = Minecraft.getInstance().getResourceManager().getResource(identifier).orElseThrow().openAsReader();
            var rawJson = String.join("\n", reader.lines().toList());
            var json = JsonParser.parseString(rawJson).getAsJsonObject();

            var arr = new ArrayList<UnboundJsonElement>();

            var rawElements = json.getAsJsonArray("elements");

            rawElements.forEach(re -> {
                var elementData = re.getAsJsonObject();
                var element = new UnboundJsonElement();
                element.to = JsonUtil.getVector3(elementData.getAsJsonArray("to"));
                element.from = JsonUtil.getVector3(elementData.getAsJsonArray("from"));
                element.rotation = UnboundJsonRotation.parse(elementData.getAsJsonObject("rotation"));
                element.faces = UnboundJsonFace.parseFaces(elementData.getAsJsonObject("faces"));
                arr.add(element);
            });

            elements = arr;
        }
    }

    public static <T extends InstancedMesh.InstanceData> InstancedMesh<T> loadInstancedMesh(ResourceLocation identifier, ResourceLocation texture, String shaderSet, float sizeMultiplier) {
        try {
            var model = new UnboundJsonModel(identifier);
            var vertices = new ArrayList<Triplet<Vector3f, Vector2f, Vector3f>>();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(sizeMultiplier/16f, new Vector3f());
                Vector3f max = element.to.mul(sizeMultiplier/16f, new Vector3f());

                float angleDegrees;
                int axis;
                Vector3f origin;
                if (element.rotation != null) {
                    angleDegrees = element.rotation.angle();
                    axis = element.rotation.axis();
                    origin = element.rotation.origin().mul(sizeMultiplier);
                } else {
                    axis = 0;
                    angleDegrees = 0;
                    origin = new Vector3f(0, 0, 0);
                }

                element.faces.forEach((dir, face) -> {
                    var uvs = getUvs(face);
                    uvs = new Vector2f[]{uvs[2], uvs[0], uvs[1], uvs[3]};
                    var verts = new ArrayList<Vector3f>();
                    Vector3f normal = new Vector3f();
                    switch (dir) {
                        case DOWN -> {
                            normal = new Vector3f(0, -1, 0);
                            verts.addAll(List.of(
                                new Vector3f(min),
                                new Vector3f(min.x, min.y, max.z),
                                new Vector3f(max.x, min.y, max.z),
                                new Vector3f(max.x, min.y, min.z)
                            ));
                        }
                        case UP -> {
                            normal = new Vector3f(0, 1, 0);
                            verts.addAll(List.of(
                                new Vector3f(min.x, max.y, max.z),
                                new Vector3f(min.x, max.y, min.z),
                                new Vector3f(max.x, max.y, min.z),
                                new Vector3f(max)
                            ));
                        }
                        case NORTH -> {
                            normal = new Vector3f(0, 0, -1);
                            verts.addAll(List.of(
                                new Vector3f(max.x, min.y, min.z),
                                new Vector3f(max.x, max.y, min.z),
                                new Vector3f(min.x, max.y, min.z),
                                new Vector3f(min)
                            ));
                        }
                        case SOUTH -> {
                            normal = new Vector3f(0, 0, 1);
                            verts.addAll(List.of(
                                new Vector3f(min.x, min.y, max.z),
                                new Vector3f(min.x, max.y, max.z),
                                new Vector3f(max),
                                new Vector3f(max.x, min.y, max.z)
                            ));
                        }
                        case WEST -> {
                            normal = new Vector3f(-1, 0, 0);
                            verts.addAll(List.of(
                                new Vector3f(min),
                                new Vector3f(min.x, max.y, min.z),
                                new Vector3f(min.x, max.y, max.z),
                                new Vector3f(min.x, min.y, max.z)
                            ));
                        }
                        case EAST -> {
                            normal = new Vector3f(1, 0, 0);
                            verts.addAll(List.of(
                                new Vector3f(max.x, min.y, max.z),
                                new Vector3f(max),
                                new Vector3f(max.x, max.y, min.z),
                                new Vector3f(max.x, min.y, min.z)
                            ));
                        }
                    }

                    if (angleDegrees != 0) {
                        var rotationAxis = axis == 0 ? new Vector3f(1, 0, 0) : axis == 1 ? new Vector3f(0, 1, 0) : new Vector3f(0, 0, 1);
                        var rotation = new Quaternionf().rotationAxis(angleDegrees * Mth.DEG_TO_RAD, rotationAxis);
                        verts.forEach(vert -> {
                            vert.sub(origin).rotate(rotation).add(origin);
                        });
                        normal.rotate(rotation);

                    }

                    vertices.addAll(List.of(
                        new Triplet<>(verts.get(3), uvs[3], new Vector3f(normal)),
                        new Triplet<>(verts.get(2), uvs[2], new Vector3f(normal)),
                        new Triplet<>(verts.get(0), uvs[0], new Vector3f(normal)),
                        new Triplet<>(verts.get(2), uvs[2], new Vector3f(normal)),
                        new Triplet<>(verts.get(1), uvs[1], new Vector3f(normal)),
                        new Triplet<>(verts.get(0), uvs[0], normal)
                    ));

                });

            });


            Triplet<Vector3f, Vector2f, Vector3f>[] arr = new Triplet[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                arr[i] = vertices.get(i);
            }
            return new InstancedMesh<>(Beatcraft.id(shaderSet), texture, arr);
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static QuadMesh loadMesh(ResourceLocation identifier) {
        try {
            BlockModel model = modelLoader.beatCraft$loadJsonModel(identifier);

            QuadMesh mesh = new QuadMesh();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(1/32f, new Vector3f());
                Vector3f max = element.to.mul(1/32f, new Vector3f());

                float angleDegrees;
                Direction.Axis axis;
                Vector3f origin;
                angleDegrees = element.rotation.angle();
                axis = element.rotation.axis();
                origin = element.rotation.origin().mul(0.5f);
                int start = mesh.vertices.size();
                if (angleDegrees != 0) {
                    mesh.addUniquePermutedVertices(min, max);
                } else {
                    mesh.addPermutedVertices(min, max);
                }
                int end = mesh.vertices.size();
                element.faces.forEach((dir, face) -> {
                    Vector2f[] uvs = getUvs(face);
                    switch (dir) {
                        case DOWN -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(min),
                                mesh.vertIdx(new Vector3f(min.x, min.y, max.z)),
                                mesh.vertIdx(new Vector3f(max.x, min.y, max.z)),
                                mesh.vertIdx(new Vector3f(max.x, min.y, min.z)),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                        case UP -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(new Vector3f(min.x, max.y, max.z)),
                                mesh.vertIdx(new Vector3f(min.x, max.y, min.z)),
                                mesh.vertIdx(new Vector3f(max.x, max.y, min.z)),
                                mesh.vertIdx(max),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                        case NORTH -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(new Vector3f(max.x, min.y, min.z)),
                                mesh.vertIdx(new Vector3f(max.x, max.y, min.z)),
                                mesh.vertIdx(new Vector3f(min.x, max.y, min.z)),
                                mesh.vertIdx(min),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                        case SOUTH -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(new Vector3f(min.x, min.y, max.z)),
                                mesh.vertIdx(new Vector3f(min.x, max.y, max.z)),
                                mesh.vertIdx(max),
                                mesh.vertIdx(new Vector3f(max.x, min.y, max.z)),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                        case WEST -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(min),
                                mesh.vertIdx(new Vector3f(min.x, max.y, min.z)),
                                mesh.vertIdx(new Vector3f(min.x, max.y, max.z)),
                                mesh.vertIdx(new Vector3f(min.x, min.y, max.z)),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                        case EAST -> {
                            mesh.quads.add(new Quad(
                                mesh.vertIdx(new Vector3f(max.x, min.y, max.z)),
                                mesh.vertIdx(max),
                                mesh.vertIdx(new Vector3f(max.x, max.y, min.z)),
                                mesh.vertIdx(new Vector3f(max.x, min.y, min.z)),
                                uvs[2], uvs[0], uvs[1], uvs[3]
                            ));
                        }
                    }
                });

                if (angleDegrees != 0) {
                    var rotationAxis = axis == Direction.Axis.X ? new Vector3f(1, 0, 0) : axis == Direction.Axis.Y ? new Vector3f(0, 1, 0) : new Vector3f(0, 0, 1);
                    var rotation = new Quaternionf().rotationAxis(angleDegrees * Mth.DEG_TO_RAD, rotationAxis);
                    mesh.transformVertices(start, end, vert -> {
                        vert.sub(origin);
                        vert.rotate(rotation);
                        vert.add(origin);
                    });
                }
            });


            return mesh;
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static SaberItemRenderer.SaberModel loadSaberMesh(ResourceLocation identifier, ResourceLocation texture) {
        try {
            var reader = Minecraft.getInstance().getResourceManager().getResource(identifier).orElseThrow().openAsReader();
            var rawJson = String.join("\n", reader.lines().toList());
            var json = JsonParser.parseString(rawJson).getAsJsonObject();
            var split = identifier.getPath().split("/");
            return loadSectionedMesh(json, split[split.length-1], texture);
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static SaberItemRenderer.SaberModel loadSaberMesh(String filePath, HashMap<String, File> textureLookup) {
        try {
            var p = Path.of(filePath);
            var rawJson = Files.readString(p);
            var json = JsonParser.parseString(rawJson).getAsJsonObject();

            var textures = json.getAsJsonObject("textures");
            String[] parts = null;
            for (int i = 0; i < 5; i++) {
                if (textures.has(String.valueOf(i))) {
                    parts = textures.get(String.valueOf(i)).getAsString().split("[/:]");
                }
            }
            if (parts == null) {
                Beatcraft.LOGGER.error("Failed to load model json! (texture must be named '0' - '4')");
                return null;
            }
            var name = parts[parts.length-1];

            var f = textureLookup.get(name);

            if (f == null) {
                Beatcraft.LOGGER.error("Undefined texture: '{}'", name);
                return null;
            }

            var tex = new DynamicTexture(f.getAbsolutePath());
            return loadSectionedMesh(json, p.getFileName().toString(), tex.id());
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load model json!", e);
            return null;
        }
    }

    private static SaberItemRenderer.SaberModel loadSectionedMesh(JsonObject json, String fileName, ResourceLocation texture) {
        var displayName = JsonUtil.getOrDefault(json, "display_name", JsonElement::getAsString, fileName);
        var authors = JsonUtil.getOrDefault(json, "authors", JsonElement::getAsJsonArray, new JsonArray()).asList().stream().map(JsonElement::getAsString).toList();

        AtomicInteger complexityScore = new AtomicInteger();

        var textureSize = json.getAsJsonArray("texture_size").asList().stream().map(JsonElement::getAsInt).toList();

        var groupAttrs = new HashMap<Integer, String>();
        var origins = new HashMap<Integer, Vector3f>();

        var groups = json.getAsJsonArray("groups");

        groups.forEach(o -> {
            var obj = o.getAsJsonObject();
            var indices = obj.getAsJsonArray("children").asList().stream().map(JsonElement::getAsInt).toList();
            var groupAttr = obj.get("name").getAsString();
            var origin = JsonUtil.getVector3(obj.getAsJsonArray("origin")).div(16f);

            for (var idx : indices) {
                groupAttrs.put(idx, groupAttr + ";");
                origins.put(idx, origin);
            }

        });

        var elements = json.getAsJsonArray("elements");


        var meshes = new ArrayList<SaberItemRenderer.AttributedMesh>();

        BiFunction<Vector3f, String, SaberItemRenderer.AttributedMesh> getMesh = (v, s) -> {
            for (var m : meshes) {
                if (m.matchesAttributes(new SaberItemRenderer.AttributedMesh(null, v, s))) {
                    return m;
                }
            }
            var m = new SaberItemRenderer.AttributedMesh(new TriangleMesh(List.of(), List.of()), v, s);
            meshes.add(m);
            complexityScore.getAndIncrement();
            return m;
        };

        AtomicInteger i = new AtomicInteger(0);
        elements.forEach(e -> {
            var obj = e.getAsJsonObject();
            var idx = i.getAndIncrement();

            var attrs = groupAttrs.getOrDefault(idx, "") + JsonUtil.getOrDefault(obj, "name", JsonElement::getAsString, "");
            var rawRotation = obj.getAsJsonObject("rotation");
            var rotationOrigin = JsonUtil.getVector3(rawRotation.getAsJsonArray("origin")).div(16f);

            var swivel = new Vector3f(origins.computeIfAbsent(idx, x -> new Vector3f()));

            swivel = new Vector3f(rotationOrigin).add(0, 0.5f, 0);

            var min = JsonUtil.getVector3(obj.getAsJsonArray("from")).div(16f).add(0, 0.5f, 0);
            var max = JsonUtil.getVector3(obj.getAsJsonArray("to")).div(16f).add(0, 0.5f, 0);

            var include = new ArrayList<>(List.of("north", "east", "south", "west", "up", "down"));

            if (min.x == max.x) {
                include.removeAll(List.of("north", "south", "up", "down"));
            }
            if (min.y == max.y) {
                include.removeAll(List.of("north", "east", "south", "west"));
            }
            if (min.z == max.z) {
                include.removeAll(List.of("east", "west", "up", "down"));
            }

            if (include.isEmpty()) return;


            var angleDegrees = rawRotation.get("angle").getAsFloat();
            var rotationAxis = rawRotation.get("axis").getAsString();

            var rotQt = new Quaternionf().rotationAxis(
                angleDegrees * Mth.DEG_TO_RAD,
                new Vector3f(
                    rotationAxis.equals("x") ? 1 : 0,
                    rotationAxis.equals("y") ? 1 : 0,
                    rotationAxis.equals("z") ? 1 : 0
                )
            );

            var mesh = getMesh.apply(swivel, attrs);

            var verts = new ArrayList<Vector3f>();
            var tris = new ArrayList<Triangle>();

            var rawFaces = obj.getAsJsonObject("faces");

            var n = 0;
            for (var faceId : include) {
                var rawFace = rawFaces.getAsJsonObject(faceId);
                var uv = JsonUtil.getVector4(rawFace.getAsJsonArray("uv"));
                uv.div(16f);
                switch (faceId) {
                    case "north" -> {
                        verts.addAll(List.of(
                            min,
                            new Vector3f(min.x, max.y, min.z),
                            new Vector3f(max.x, max.y, min.z),
                            new Vector3f(max.x, min.y, min.z)
                        ));
                    }
                    case "east" -> {
                        verts.addAll(List.of(
                            new Vector3f(max.x, min.y, min.z),
                            new Vector3f(max.x, max.y, min.z),
                            max,
                            new Vector3f(max.x, min.y, max.z)
                        ));
                    }
                    case "south" -> {
                        verts.addAll(List.of(
                            new Vector3f(max.x, min.y, max.z),
                            max,
                            new Vector3f(min.x, max.y, max.z),
                            new Vector3f(min.x, min.y, max.z)
                        ));
                    }
                    case "west" -> {
                        verts.addAll(List.of(
                            new Vector3f(min.x, min.y, max.z),
                            new Vector3f(min.x, max.y, max.z),
                            new Vector3f(min.x, max.y, min.z),
                            min
                        ));
                    }
                    case "up" -> {
                        verts.addAll(List.of(
                            max,
                            new Vector3f(max.x, max.y, min.z),
                            new Vector3f(min.x, max.y, min.z),
                            new Vector3f(min.x, max.y, max.z)
                        ));
                    }
                    case "down" -> {
                        verts.addAll(List.of(
                            new Vector3f(max.x, min.y, min.z),
                            new Vector3f(max.x, min.y, max.z),
                            new Vector3f(min.x, min.y, max.z),
                            min
                        ));
                    }
                }
                var x = n * 4;
                tris.addAll(List.of(
                    new Triangle(x, x + 1, x + 2, new Vector2f(uv.x, uv.w), new Vector2f(uv.x, uv.y), new Vector2f(uv.z, uv.y)),
                    new Triangle(x, x + 2, x + 3, new Vector2f(uv.x, uv.w), new Vector2f(uv.z, uv.y), new Vector2f(uv.z, uv.w))
                ));
                n++;
            }

            for (var vert : verts) {
                vert.sub(rotationOrigin).rotate(rotQt).add(rotationOrigin);
            }

            mesh.mesh.addGeometry(verts, tris);

        });

        return new SaberItemRenderer.SaberModel(fileName, displayName, authors, meshes, complexityScore.get(), texture);

    }

    private static Vector2f @NotNull [] getUvs(UnboundJsonModel.UnboundJsonFace face) {
        int rotation = (int) face.textureData().rotation;
        float[] rawUvs = face.textureData().uvs;
        Vector2f[] uvs = new Vector2f[]{
            new Vector2f(rawUvs[0]/16f, rawUvs[1]/16f),
            new Vector2f(rawUvs[2]/16f, rawUvs[1]/16f),
            new Vector2f(rawUvs[0]/16f, rawUvs[3]/16f),
            new Vector2f(rawUvs[2]/16f, rawUvs[3]/16f)
        }; // order: tl, tr, bl, br

        while (rotation > 0) {
            uvs = new Vector2f[]{
                uvs[2], uvs[0], uvs[3], uvs[1]
            };
            rotation -= 90;
        }
        return uvs;
    }
    private static Vector2f @NotNull [] getUvs(BlockElementFace face) {
        int rotation = face.uv().rotation;
        float[] rawUvs = face.uv().uvs;
        Vector2f[] uvs = new Vector2f[]{
            new Vector2f(rawUvs[0]/16f, rawUvs[1]/16f),
            new Vector2f(rawUvs[2]/16f, rawUvs[1]/16f),
            new Vector2f(rawUvs[0]/16f, rawUvs[3]/16f),
            new Vector2f(rawUvs[2]/16f, rawUvs[3]/16f)
        }; // order: tl, tr, bl, br

        while (rotation > 0) {
            uvs = new Vector2f[]{
                uvs[2], uvs[0], uvs[3], uvs[1]
            };
            rotation -= 90;
        }
        return uvs;
    }

}