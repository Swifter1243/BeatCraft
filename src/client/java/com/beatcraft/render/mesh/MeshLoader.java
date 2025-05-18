package com.beatcraft.render.mesh;

import com.beatcraft.BeatCraft;
import com.beatcraft.mixin_utils.ModelLoaderAccessor;
import com.beatcraft.render.instancing.InstancedMesh;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeshLoader {

    public static InstancedMesh COLOR_NOTE_INSTANCED_MESH;
    public static InstancedMesh BOMB_NOTE_INSTANCED_MESH;
    public static InstancedMesh CHAIN_HEAD_NOTE_INSTANCED_MESH;
    public static InstancedMesh CHAIN_LINK_NOTE_INSTANCED_MESH;


    public static QuadMesh COLOR_NOTE_MESH;
    public static QuadMesh CHAIN_HEAD_MESH;
    public static QuadMesh CHAIN_LINK_MESH;
    public static QuadMesh BOMB_MESH;

    public static TriangleMesh COLOR_NOTE_RENDER_MESH;
    public static TriangleMesh CHAIN_HEAD_RENDER_MESH;
    public static TriangleMesh CHAIN_LINK_RENDER_MESH;
    public static TriangleMesh NOTE_ARROW_RENDER_MESH;
    public static TriangleMesh NOTE_DOT_RENDER_MESH;
    public static TriangleMesh CHAIN_DOT_RENDER_MESH;
    public static TriangleMesh BOMB_RENDER_MESH;

    public static final Identifier NOTE_TEXTURE = BeatCraft.id("textures/gameplay_objects/color_note.png");
    public static final Identifier ARROW_TEXTURE = BeatCraft.id("textures/gameplay_objects/arrow.png");

    private static ModelLoaderAccessor modelLoader;

    public static void loadGameplayMeshes(ModelLoaderAccessor modelLoader) {

        MeshLoader.modelLoader = modelLoader;

        COLOR_NOTE_MESH = loadMesh(BeatCraft.id("item/color_note"));
        COLOR_NOTE_MESH.texture = NOTE_TEXTURE;
        CHAIN_HEAD_MESH = loadMesh(BeatCraft.id("item/color_note_chain_head"));
        CHAIN_HEAD_MESH.texture = NOTE_TEXTURE;
        CHAIN_LINK_MESH = loadMesh(BeatCraft.id("item/color_note_chain_link"));
        CHAIN_LINK_MESH.texture = NOTE_TEXTURE;
        BOMB_MESH = loadMesh(BeatCraft.id("item/bomb_note"));
        BOMB_MESH.texture = NOTE_TEXTURE;

        COLOR_NOTE_INSTANCED_MESH = loadInstancedMesh(BeatCraft.id("item/color_note"));

        var arrow_mesh = loadMesh(BeatCraft.id("item/note_arrow"));
        arrow_mesh.texture = ARROW_TEXTURE;
        var dot_mesh = loadMesh(BeatCraft.id("item/note_dot"));
        dot_mesh.texture = ARROW_TEXTURE;
        var chain_dot_mesh = loadMesh(BeatCraft.id("item/chain_note_dot"));
        chain_dot_mesh.texture = ARROW_TEXTURE;

        COLOR_NOTE_RENDER_MESH = COLOR_NOTE_MESH.toTriangleMesh();
        CHAIN_HEAD_RENDER_MESH = CHAIN_HEAD_MESH.toTriangleMesh();
        CHAIN_LINK_RENDER_MESH = CHAIN_LINK_MESH.toTriangleMesh();
        BOMB_RENDER_MESH = BOMB_MESH.toTriangleMesh();

        NOTE_ARROW_RENDER_MESH = arrow_mesh.toTriangleMesh();
        NOTE_DOT_RENDER_MESH = dot_mesh.toTriangleMesh();
        CHAIN_DOT_RENDER_MESH = chain_dot_mesh.toTriangleMesh();

    }

    public static InstancedMesh loadInstancedMesh(Identifier identifier) {
        try {
            JsonUnbakedModel model = modelLoader.beatCraft$loadJsonModel(identifier);
            var vertices = new ArrayList<Triplet<Vector3f, Vector2f, Vector3f>>();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(1/32f, new Vector3f());
                Vector3f max = element.to.mul(1/32f, new Vector3f());

                float angleDegrees;
                Direction.Axis axis;
                Vector3f origin;
                if (element.rotation != null) {
                    angleDegrees = element.rotation.angle();
                    axis = element.rotation.axis();
                    origin = element.rotation.origin().mul(0.5f);
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
                        new Triplet<>(verts.get(0), uvs[0], new Vector3f(normal)),
                        new Triplet<>(verts.get(1), uvs[1], new Vector3f(normal)),
                        new Triplet<>(verts.get(2), uvs[2], new Vector3f(normal)),
                        new Triplet<>(verts.get(3), uvs[3], normal)
                    ));

                });

            });


            Triplet<Vector3f, Vector2f, Vector3f>[] arr = new Triplet[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                arr[i] = vertices.get(i);
            }
            return new InstancedMesh(BeatCraft.id("shaders/instanced/color_note"), arr);
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
