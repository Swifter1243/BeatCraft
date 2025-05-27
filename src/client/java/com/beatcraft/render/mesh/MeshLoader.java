package com.beatcraft.render.mesh;

import com.beatcraft.BeatCraft;
import com.beatcraft.mixin_utils.ModelLoaderAccessor;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import com.beatcraft.render.instancing.ArrowInstanceData;
import com.beatcraft.render.instancing.BombNoteInstanceData;
import com.beatcraft.render.instancing.ColorNoteInstanceData;
import com.beatcraft.render.instancing.InstancedMesh;
import com.beatcraft.render.item.SaberItemRenderer;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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

    public static TriangleMesh NOTE_ARROW_RENDER_MESH;
    public static TriangleMesh NOTE_DOT_RENDER_MESH;
    public static TriangleMesh CHAIN_DOT_RENDER_MESH;

    public static final Identifier NOTE_TEXTURE = BeatCraft.id("textures/gameplay_objects/color_note.png");
    public static final Identifier ARROW_TEXTURE = BeatCraft.id("textures/gameplay_objects/arrow.png");

    private static ModelLoaderAccessor modelLoader;

    public static void loadGameplayMeshes(ModelLoaderAccessor modelLoader) {

        MeshLoader.modelLoader = modelLoader;
        COLOR_NOTE_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/color_note"), NOTE_TEXTURE, "instanced/color_note");
        CHAIN_HEAD_NOTE_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/color_note_chain_head"), NOTE_TEXTURE, "instanced/color_note");
        CHAIN_LINK_NOTE_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/color_note_chain_link"), NOTE_TEXTURE, "instanced/color_note");
        BOMB_NOTE_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/bomb_note"), NOTE_TEXTURE, "instanced/bomb_note");
        NOTE_ARROW_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/note_arrow"), ARROW_TEXTURE, "instanced/arrow");
        NOTE_DOT_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/note_dot"), ARROW_TEXTURE, "instanced/arrow");
        CHAIN_DOT_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/chain_note_dot"), ARROW_TEXTURE, "instanced/arrow");

        MIRROR_COLOR_NOTE_INSTANCED_MESH = COLOR_NOTE_INSTANCED_MESH.copy();
        MIRROR_BOMB_NOTE_INSTANCED_MESH = BOMB_NOTE_INSTANCED_MESH.copy();
        MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH = CHAIN_HEAD_NOTE_INSTANCED_MESH.copy();
        MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH = CHAIN_LINK_NOTE_INSTANCED_MESH.copy();
        MIRROR_NOTE_ARROW_INSTANCED_MESH = NOTE_ARROW_INSTANCED_MESH.copy();
        MIRROR_NOTE_DOT_INSTANCED_MESH = NOTE_DOT_INSTANCED_MESH.copy();
        MIRROR_CHAIN_DOT_INSTANCED_MESH = CHAIN_DOT_INSTANCED_MESH.copy();

        var arrow_mesh = loadMesh(BeatCraft.id("item/note_arrow"));
        arrow_mesh.texture = ARROW_TEXTURE;
        var dot_mesh = loadMesh(BeatCraft.id("item/note_dot"));
        dot_mesh.texture = ARROW_TEXTURE;
        var chain_dot_mesh = loadMesh(BeatCraft.id("item/chain_note_dot"));
        chain_dot_mesh.texture = ARROW_TEXTURE;

        NOTE_ARROW_RENDER_MESH = arrow_mesh.toTriangleMesh();
        NOTE_DOT_RENDER_MESH = dot_mesh.toTriangleMesh();
        CHAIN_DOT_RENDER_MESH = chain_dot_mesh.toTriangleMesh();

    }

    public static <T extends InstancedMesh.InstanceData> InstancedMesh<T> loadInstancedMesh(Identifier identifier, Identifier texture, String shaderSet) {
        try {
            JsonUnbakedModel model = modelLoader.beatCraft$loadJsonModel(identifier);
            var vertices = new ArrayList<Triplet<Vector3f, Vector2f, Vector3f>>();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(1/16f, new Vector3f());
                Vector3f max = element.to.mul(1/16f, new Vector3f());

                float angleDegrees;
                Direction.Axis axis;
                Vector3f origin;
                if (element.rotation != null) {
                    angleDegrees = element.rotation.angle();
                    axis = element.rotation.axis();
                    origin = element.rotation.origin();
                } else {
                    axis = Direction.Axis.X;
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
                        var rotationAxis = axis == Direction.Axis.X ? new Vector3f(1, 0, 0) : axis == Direction.Axis.Y ? new Vector3f(0, 1, 0) : new Vector3f(0, 0, 1);
                        var rotation = new Quaternionf().rotationAxis(angleDegrees * MathHelper.RADIANS_PER_DEGREE, rotationAxis);
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
            return new InstancedMesh<>(BeatCraft.id(shaderSet), texture, arr);
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static QuadMesh loadMesh(Identifier identifier) {
        try {
            JsonUnbakedModel model = modelLoader.beatCraft$loadJsonModel(identifier);

            QuadMesh mesh = new QuadMesh();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(1/32f, new Vector3f());
                Vector3f max = element.to.mul(1/32f, new Vector3f());

                float angleDegrees = 0;
                Direction.Axis axis = Direction.Axis.X;
                Vector3f origin;
                if (element.rotation != null) {
                    angleDegrees = element.rotation.angle();
                    axis = element.rotation.axis();
                    origin = element.rotation.origin().mul(0.5f);
                } else {
                    origin = new Vector3f(0, 0, 0);
                }
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
                    var rotation = new Quaternionf().rotationAxis(angleDegrees * MathHelper.RADIANS_PER_DEGREE, rotationAxis);
                    mesh.transformVertices(start, end, vert -> {
                        vert.sub(origin);
                        vert.rotate(rotation);
                        vert.add(origin);
                    });
                }
            });


            return mesh;
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static SaberItemRenderer.SaberModel loadSaberMesh(Identifier identifier, Identifier texture) {
        try {
            var reader = MinecraftClient.getInstance().getResourceManager().getResource(identifier).orElseThrow().getReader();
            var rawJson = String.join("\n", reader.lines().toList());
            var json = JsonParser.parseString(rawJson).getAsJsonObject();
            var split = identifier.getPath().split("/");
            return loadSectionedMesh(json, split[split.length-1], texture);
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    public static SaberItemRenderer.SaberModel loadSaberMesh(String filePath, HashMap<String, File> textureLookup) {
        try {
            var p = Path.of(filePath);
            var rawJson = Files.readString(p);
            var json = JsonParser.parseString(rawJson).getAsJsonObject();

            var textures = json.getAsJsonObject("textures");
            var parts = textures.get("1").getAsString().split("[/:]");
            var name = parts[parts.length-1];

            var f = textureLookup.get(name);

            if (f == null) {
                BeatCraft.LOGGER.error("Undefined texture: '{}'", name);
                return null;
            }

            var tex = new DynamicTexture(f.getAbsolutePath());
            return loadSectionedMesh(json, p.getFileName().toString(), tex.id());
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to load model json!", e);
            throw new RuntimeException(e);
        }
    }

    private static SaberItemRenderer.SaberModel loadSectionedMesh(JsonObject json, String fileName, Identifier texture) {
        var displayName = JsonUtil.getOrDefault(json, "display_name", JsonElement::getAsString, fileName);
        var authors = JsonUtil.getOrDefault(json, "authors", JsonElement::getAsJsonArray, new JsonArray()).asList().stream().map(JsonElement::getAsString).toList();

        AtomicInteger complexityScore = new AtomicInteger();

        var textureSize = json.getAsJsonArray("texture_size").asList().stream().map(JsonElement::getAsInt).toList();

        var texSize = new Vector2f(textureSize.getFirst(), textureSize.get(1));

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

            var attrs = groupAttrs.getOrDefault(idx, "") + obj.get("name").getAsString();

            var swivel = origins.computeIfAbsent(idx, x -> new Vector3f());

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


            var rawRotation = obj.getAsJsonObject("rotation");
            var angleDegrees = rawRotation.get("angle").getAsFloat();
            var rotationAxis = rawRotation.get("axis").getAsString();
            var rotationOrigin = JsonUtil.getVector3(rawRotation.getAsJsonArray("origin")).div(16f);

            var rotQt = new Quaternionf().rotationAxis(
                angleDegrees * MathHelper.RADIANS_PER_DEGREE,
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

        return new SaberItemRenderer.SaberModel(displayName, authors, meshes, complexityScore.get(), texture);

    }

    private static Vector2f @NotNull [] getUvs(ModelElementFace face) {
        int rotation = face.textureData().rotation;
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

}
