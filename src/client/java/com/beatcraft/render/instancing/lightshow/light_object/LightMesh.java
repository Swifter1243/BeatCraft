package com.beatcraft.render.instancing.lightshow.light_object;

/*
This mesh loads from json using a custom format.

the shader uses 8 color channels and 3 configurable texture channels

each mesh vertex must have data to determine:
position, uv, normal, colorId, materialId, textureId

 */


/*
Mesh format:
{
    "credits": ["string or list of strings"],
    "mesh_format": 1,
    "vertices": [
        [0.0, 1.0, 2.0] // vertices are in meters instead of 16x16 pixel space
    ],
    "named_vertices": {
        "unique_name": [1.0, 1.0, 2.0] // named vertices makes keeping trak of things easier
    },
    "uvs": [...],
    "named_uvs": {...},
    "normals": [...],
    "named_normals": {...},
    "interpolated_vertices": {
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
    "triangles": [
        [
            [v0, uv0, normal0], // vertex 0
            [v1, uv1, normal1], // vertex 1
            [v2, uv2, normal2], // vertex 2
            "light", {"color": 0} // data index/name, the object afterwards overrides data in the named data attributes
        ]
    ],
    "textures": {
        "0": "beatcraft:path/name" // only 0, 1, and 2 are valid texture slots
    },
    "data": {
        "default": { "material": 0, "texture": 0, "color": 0 }, // defines the default for any un-defined data
        "light": { "material": 1 } // missing keys fallback to 'default'
    },
    "cull": false, // whether to do backface culling
    "bloom_pss": true, // defaults to true if absent; set to false to disable the bloom render pass
    "solid_pass": true, // defaults to true; set to false to disable the regular visible pass
    "bloomfog_style": 0 // defaults to 0;
    // 0 = anything that renders to bloom will also render to bloomfog
    // 1 = everything renders to bloomfog. note: the pass that draws to bloomfog will supply a blank texture as the mesh's u_bloomfog sampler
    // 2 = don't render to bloomfog

}

 */

import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LightMesh {

    private static HashMap<String, LightMesh> meshes;


    protected record VertexData(int vertexId, int uvId, int normalId) {

    }

    protected record TriangleData(VertexData a, VertexData b, VertexData c, int colorId, int materialId, int textureId) {

    }

    private final List<Vector3f> vertices;
    private final List<Vector2f> uvs;
    private final List<Vector3f> normals;

    private final List<TriangleData> triangles;
    private final HashMap<Integer, Integer> textures; // textureId: glId
    private final HashMap<Integer, Identifier> unloadedTextures;
    private boolean doBloom = true;
    private boolean doSolid = true;
    private int bloomfogStyle = 0;
    private boolean cullBackfaces = false;

    private boolean loaded = false;

    protected LightMesh(List<Vector3f> vertices, List<Vector2f> uvs, List<Vector3f> normals, List<TriangleData> triangles, HashMap<Integer, Identifier> unloadedTextures) {
        this.vertices = vertices;
        this.uvs = uvs;
        this.normals = normals;
        this.triangles = triangles;
        this.textures = new HashMap<>();
        this.unloadedTextures = unloadedTextures;
    }


    private static class MeshConstructor {
        private ArrayList<Vector3f> vertices = new ArrayList<>();
        private ArrayList<Vector2f> uvs = new ArrayList<>();
        private ArrayList<Vector3f> normals = new ArrayList<>();

        // named Lists map to the index into the un-named list instead of to vertices
        private HashMap<String, Integer> namedVertices = new HashMap<>();
        private HashMap<String, Integer> namedUvs = new HashMap<>();
        private HashMap<String, Integer> namedNormals = new HashMap<>();

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
                    throw new IllegalArgumentException("normal name is not valid");
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

            }
        }

    }

    public static LightMesh load(String name, Identifier source) throws IOException {

        var builder = new MeshConstructor();

        var reader = MinecraftClient.getInstance().getResourceManager().getResource(source).orElseThrow().getReader();
        var rawJson = String.join("\n", reader.lines().toList());
        var json = JsonParser.parseString(rawJson).getAsJsonObject();

        var format = JsonUtil.getOrDefault(json, "mesh_format", JsonElement::getAsInt, 0);

        if (format != 1) {
            throw new IOException("Mesh is not a known format");
        }


        return null;

    }

}
