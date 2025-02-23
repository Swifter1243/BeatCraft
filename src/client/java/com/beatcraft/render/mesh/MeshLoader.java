package com.beatcraft.render.mesh;

import com.beatcraft.BeatCraft;
import com.beatcraft.mixin_utils.ModelLoaderAccessor;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;

public class MeshLoader {

    public static QuadMesh COLOR_NOTE_MESH;
    public static QuadMesh CHAIN_HEAD_MESH;
    public static QuadMesh CHAIN_LINK_MESH;

    public static final Identifier NOTE_TEXTURE = Identifier.of(BeatCraft.MOD_ID, "textures/gameplay_objects/color_note.png");

    public static void loadGameplayMeshes(ModelLoaderAccessor modelLoader) {

        COLOR_NOTE_MESH = loadMesh(Identifier.of(BeatCraft.MOD_ID, "item/color_note"), modelLoader);
        COLOR_NOTE_MESH.texture = NOTE_TEXTURE;
        CHAIN_HEAD_MESH = loadMesh(Identifier.of(BeatCraft.MOD_ID, "item/color_note_chain_head"), modelLoader);
        CHAIN_HEAD_MESH.texture = NOTE_TEXTURE;
        CHAIN_LINK_MESH = loadMesh(Identifier.of(BeatCraft.MOD_ID, "item/color_note_chain_link"), modelLoader);
        CHAIN_LINK_MESH.texture = NOTE_TEXTURE;
    }


    public static QuadMesh loadMesh(Identifier identifier, ModelLoaderAccessor modelLoader) {
        try {
            JsonUnbakedModel model = modelLoader.beatCraft$loadJsonModel(identifier);

            QuadMesh mesh = new QuadMesh();

            model.getElements().forEach(element -> {
                Vector3f min = element.from.mul(1/32f, new Vector3f());
                Vector3f max = element.to.mul(1/32f, new Vector3f());
                mesh.addPermutedVertices(min, max);
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
