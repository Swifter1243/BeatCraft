package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DataEnvironmentV2Layout {

    protected enum IdGroup {
        LEFT_LASERS,
        RIGHT_LASERS,
        CENTER_LASERS,
        BACK_LASERS,
        RING_LIGHTS;

        static IdGroup fromString(String s) {
            return switch (s) {
                case "left-lasers" -> LEFT_LASERS;
                case "right-lasers" -> RIGHT_LASERS;
                case "center-lasers" -> CENTER_LASERS;
                case "back-lasers" -> BACK_LASERS;
                case "ring-lights" -> RING_LIGHTS;
                default -> throw new RuntimeException("Invalid id group: " + s);
            };
        }
    }

    protected enum EventGroup {
        None,
        OuterRing,
        InnerRing,
        LeftSpinning,
        RightSpinning;

        static EventGroup fromString(String s) {
            return switch (s) {
                case "outer-ring" -> OuterRing;
                case "inner-ring" -> InnerRing;
                case "left-spinning" -> LeftSpinning;
                case "right-spinning" -> RightSpinning;
                default -> throw new RuntimeException("Invalid event group: " + s);
            };
        }
    }

    protected static class IdIter implements Iterable<IdIter.Id>, Iterator<IdIter.Id> {

        record Id(IdGroup group, int id) {}

        protected final Object[] arr;

        protected IdGroup currentGroup = null;
        protected int idx = 0;

        IdIter(JsonArray data) {
            arr = new Object[data.size()];
            for (var i = 0; i < data.size(); ++i) {
                var v = data.get(i).getAsJsonPrimitive();
                if (v.isString()) {
                    var group = IdGroup.fromString(v.getAsString());
                    arr[i] = group;
                    if (currentGroup == null) {
                        currentGroup = group;
                    }
                } else {
                    var x = v.getAsInt();
                    arr[i] = x;
                }
            }
        }

        @Override
        public @NotNull Iterator<Id> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return idx < arr.length;
        }

        @Override
        public Id next() {
            while (true) {
                var x = arr[idx++];
                if (x instanceof IdGroup group) {
                    currentGroup = group;
                }
                else if (x instanceof Integer i) {
                    return new Id(currentGroup, i);
                }
            }
        }
    }

    protected static class MeshRef {
        protected int refs = 0;
        public final LightMesh mesh;

        public MeshRef(LightMesh mesh) {
            this.mesh = mesh;
        }

        public void load() {
            if (refs++ == 0) {
                mesh.buildMesh();
            }
        }
        public void unload() {
            if (refs > 1) {
                --refs;
            } else if (refs == 1) {
                refs = 0;
                mesh.cleanup();
            }
        }
    }

    protected static class LightGroup {
        protected static class SubGroup {
            protected IdIter ids = null;
            protected ArrayList<Integer> idOffsets = new ArrayList<>();
            protected Vector3f position = new Vector3f();
            protected Quaternionf rotation = new Quaternionf();

            protected int count = 1;
            protected Vector3f offset = new Vector3f();
            protected Quaternionf rotOffset = new Quaternionf();

            protected Vector3f spinAxis = new Vector3f(0, 1, 0);

            protected float[] anglesRadians = new float[0];
            protected float[] deltasRadians = new float[0];
        }

        protected final HashMap<EventGroup, ArrayList<SubGroup>> subGroups = new HashMap<>();

        protected void addPlacement(JsonObject json) {
            var rawType = json.get("type");
            var ids = new IdIter(json.getAsJsonArray("ids"));

            EventGroup type;
            if (rawType == null) {
                type = EventGroup.None;
            } else {
                type = EventGroup.fromString(rawType.getAsString());
            }
            if (!subGroups.containsKey(type)) {
                subGroups.put(type, new ArrayList<>());
            }
            var ls = subGroups.get(type);
            var sub = new SubGroup();
            sub.ids = ids;

            if (type != EventGroup.None) {
                var pos = json.getAsJsonArray("position");
                var off = json.getAsJsonArray("offset");
                var count = json.get("count");
                var rot = json.getAsJsonArray("rotation");
                var rotOff = json.getAsJsonArray("rotation-offset");
                var idOffsets = json.getAsJsonArray("id-step");

                if (pos != null) {
                    sub.position = JsonUtil.getVector3(pos);
                }
                if (off != null) {
                    sub.offset = JsonUtil.getVector3(off);
                }
                if (count != null) {
                    sub.count = count.getAsInt();
                }
                if (rot != null) {
                    sub.rotation = JsonUtil.getQuaternion(rot);
                }
                if (rotOff != null) {
                    sub.rotation = JsonUtil.getQuaternion(rotOff);
                }
                if (idOffsets == null) {
                    sub.idOffsets.add(null);
                } else {
                    for (var x : idOffsets) {
                        sub.idOffsets.add(x.getAsInt());
                    }
                }
            } else {
                sub.idOffsets.add(null);
            }

            if (type == EventGroup.InnerRing || type == EventGroup.OuterRing) {
                var angles = json.getAsJsonArray("angles");
                var deltas = json.getAsJsonArray("deltas");

                sub.anglesRadians = new float[angles.size()];
                sub.deltasRadians = new float[deltas.size()];

                for (var i = 0; i < angles.size(); ++i) {
                    sub.anglesRadians[i] = angles.get(i).getAsFloat() * Mth.DEG_TO_RAD;
                }
                for (var i = 0; i < deltas.size(); ++i) {
                    sub.deltasRadians[i] = angles.get(i).getAsFloat() * Mth.DEG_TO_RAD;
                }

            } else if (type == EventGroup.LeftSpinning || type == EventGroup.RightSpinning) {
                var axis = json.getAsJsonArray("axis");
                if (axis != null) {
                    sub.spinAxis = JsonUtil.getVector3(axis);
                }
            }

            ls.add(sub);
        }

    }

    protected static HashMap<ResourceLocation, MeshRef> meshes;

    protected final ResourceLocation envId;
    protected final ArrayList<MeshRef> meshRefs = new ArrayList<>();

    protected final ArrayList<LightMesh> statics = new ArrayList<>();
    protected final HashMap<LightMesh, LightGroup> lights = new HashMap<>();

    public DataEnvironmentV2Layout(ResourceLocation envId) throws IOException {
        this.envId = envId;
        load();
    }

    public void load() throws IOException {
        var rm = Minecraft.getInstance().getResourceManager();
        var reader = rm.getResource(envId).orElseThrow().openAsReader();
        var json = JsonParser.parseReader(reader).getAsJsonObject();

        var layout = json.getAsJsonObject("layout").entrySet();

        for (var entry : layout) {
            var id = entry.getKey();
            var data = entry.getValue().getAsJsonObject();
            var parts = id.split(":", 2);
            var namespace = parts[0];
            var path = "environments/" + parts[1];
            var loc = ResourceLocation.tryBuild(namespace, path);
            var meshRef = loadMesh(loc);
            meshRefs.add(meshRef);

            if (data.isEmpty()) {
                statics.add(meshRef.mesh);
            } else {
                var placements = data.getAsJsonArray("placements");
                var ids = data.getAsJsonArray("ids");
                if (!lights.containsKey(meshRef.mesh)) {
                    lights.put(meshRef.mesh, new LightGroup());
                }
                var group = lights.get(meshRef.mesh);
                if (placements != null) {
                    for (var placement : placements) {
                        group.addPlacement(placement.getAsJsonObject());
                    }
                } else if (ids != null) {
                    group.addPlacement(data);
                } else {
                    throw new RuntimeException("data should specify 'placement' or 'ids'");
                }
            }

        }

    }

    protected MeshRef loadMesh(ResourceLocation loc) throws IOException {
        if (meshes.containsKey(loc)) {
            return meshes.get(loc);
        }
        var mesh = LightMesh.load(loc.toString(), loc);
        var ref = new MeshRef(mesh);
        meshes.put(loc, ref);
        return ref;
    }

    public void setup() {
        for (var ref : meshRefs) {
            ref.load();
        }
    }

    public void cleanup() {
        for (var ref : meshRefs) {
            ref.unload();
        }
    }

}
