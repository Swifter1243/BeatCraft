package com.beatcraft.client.lightshow.environment;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.spectrogram.SpectrogramTowers;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

public class DataEnvironmentV2Layout {

    protected enum IdGroup {
        LEFT_LASERS,
        RIGHT_LASERS,
        CENTER_LASERS,
        BACK_LASERS,
        RING_LIGHTS;

        static IdGroup fromString(String s) {
            return switch (s) {
                case "left-lasers", "left-lights" -> LEFT_LASERS;
                case "right-lasers", "right-lights" -> RIGHT_LASERS;
                case "center-lasers", "center-lights" -> CENTER_LASERS;
                case "back-lasers", "back-lights" -> BACK_LASERS;
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

        public record Id(IdGroup group, int id) {}

        protected final Object[] arr;

        protected IdGroup currentGroup = null;
        protected int idx = 0;
        protected int length = 0;

        IdIter(Object[] arr, IdGroup group, int length) {
            this.arr = arr;
            this.currentGroup = group;
            this.length = length;
        }

        IdIter() {
            arr = new Object[0];
            currentGroup = IdGroup.CENTER_LASERS;
        }

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
                    ++length;
                    var x = v.getAsInt();
                    arr[i] = x;
                }
            }
        }

        @Override
        public @NotNull Iterator<Id> iterator() {
            return new IdIter(this.arr, this.currentGroup, this.length);
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
            protected Quaternionf orientation = new Quaternionf();

            protected int count = 1;
            protected Vector3f offset = new Vector3f();
            protected Quaternionf rotOffset = new Quaternionf();
            protected Quaternionf oriOffset = new Quaternionf();

            protected Vector3f spinAxis = new Vector3f(0, 1, 0);

            protected float[] anglesRadians = new float[0];
            protected float[] deltasRadians = new float[0];
            protected float startAngleRadians = 0;
            protected float startDeltaRadians = 0;
        }

        protected final HashMap<EventGroup, ArrayList<SubGroup>> subGroups = new HashMap<>();

        protected void addPlacement(JsonObject json) {
            var rawType = json.get("type");
            IdIter ids;
            var rawIds = json.getAsJsonArray("ids");
            if (rawIds != null) {
                ids = new IdIter(rawIds);
            } else {
                ids = new IdIter();
            }

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

            var pos = json.getAsJsonArray("position");
            var off = json.getAsJsonArray("offset");
            var count = json.get("count");
            var rot = json.getAsJsonArray("rotation");
            var rotOff = json.getAsJsonArray("rotation-offset");
            var ori = json.getAsJsonArray("orientation");
            var oriOff = json.getAsJsonArray("orientation-offset");
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
                sub.rotOffset = JsonUtil.getQuaternion(rotOff);
            }
            if (ori != null) {
                sub.orientation = JsonUtil.getQuaternion(ori);
            }
            if (oriOff != null) {
                sub.oriOffset = JsonUtil.getQuaternion(oriOff);
            }
            if (idOffsets == null) {
                for (var i = 0; i < ids.length; ++i) {
                    sub.idOffsets.add(0);
                }
            } else {
                for (var x : idOffsets) {
                    sub.idOffsets.add(x.getAsInt());
                }
            }

            if (type == EventGroup.InnerRing || type == EventGroup.OuterRing) {
                var angles = json.getAsJsonArray("angles");
                var deltas = json.getAsJsonArray("deltas");
                var starts = json.getAsJsonArray("start");

                if (starts != null) {
                    sub.startAngleRadians = starts.get(0).getAsFloat() * Mth.DEG_TO_RAD;
                    sub.startDeltaRadians = starts.get(1).getAsFloat() * Mth.DEG_TO_RAD;
                }

                sub.anglesRadians = new float[angles.size()];
                sub.deltasRadians = new float[deltas.size()];

                for (var i = 0; i < angles.size(); ++i) {
                    sub.anglesRadians[i] = angles.get(i).getAsFloat() * Mth.DEG_TO_RAD;
                }
                for (var i = 0; i < deltas.size(); ++i) {
                    sub.deltasRadians[i] = deltas.get(i).getAsFloat() * Mth.DEG_TO_RAD;
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

    protected record SpectrogramPair(
        SpectrogramTowers a,
        @Nullable SpectrogramTowers b
    ) {}

    protected record SpectrogramData(
        Vector3f position,
        Quaternionf rotation,
        Vector3f offset,
        int count,
        SpectrogramTowers.TowerStyle style,
        boolean halfSplit,
        float levelModifier,
        float baseHeight,
        Function<Float, Float> easing,
        @Nullable Vector4f mirror
    ) {

        private static Vector3f mirrorVector(Vector3f v, Vector4f plane) {
            Vector3f normal = new Vector3f(plane.x, plane.y, plane.z);
            float dist = v.dot(normal) - plane.w;
            return v.sub(normal.mul(2 * dist, new Vector3f()), new Vector3f());
        }

        private static Quaternionf mirrorQuaternion(Quaternionf q, Vector4f plane) {
            Vector3f normal = new Vector3f(plane.x, plane.y, plane.z);

            java.util.function.UnaryOperator<Vector3f> reflect = (v) -> {
                float dot = v.dot(normal);
                return v.sub(normal.mul(2 * dot, new Vector3f()), new Vector3f());
            };

            Vector3f right = reflect.apply(q.transform(new Vector3f(1, 0, 0)));
            Vector3f up = reflect.apply(q.transform(new Vector3f(0, 1, 0)));
            Vector3f forward = reflect.apply(q.transform(new Vector3f(0, 0, 1)));

            right.negate();

            Matrix3f mat = new Matrix3f(
                right.x,   right.y,   right.z,
                up.x,      up.y,      up.z,
                forward.x, forward.y, forward.z
            );

            return mat.getNormalizedRotation(new Quaternionf());
        }

        protected SpectrogramPair build(BeatmapController map, File f) {
            var a = new SpectrogramTowers(
                map,
                position,
                rotation,
                offset,
                count,
                f,
                style,
                halfSplit
            );
            a.levelModifier = levelModifier;
            a.baseHeight = baseHeight;
            a.levelEasing = easing;
            if (mirror == null) {
                return new SpectrogramPair(a, null);
            } else {
                var b = a.copyTo(
                    mirrorVector(position, mirror),
                    mirrorQuaternion(rotation, mirror)
                );
                b.levelModifier = levelModifier;
                b.baseHeight = baseHeight;
                b.levelEasing = easing;
                return new SpectrogramPair(a, b);
            }
        }

    }

    protected static final HashMap<ResourceLocation, MeshRef> meshes = new HashMap<>();

    protected final ResourceLocation envId;
    protected final ArrayList<MeshRef> meshRefs = new ArrayList<>();

    protected final ArrayList<LightMesh> statics = new ArrayList<>();
    protected final HashMap<LightMesh, LightGroup> lights = new HashMap<>();
    protected float[] fogHeights = new float[]{-50, -30};
    protected float[][] mirrorTris = new float[0][];
    protected SpectrogramData spectrogramData = null;

    public DataEnvironmentV2Layout(ResourceLocation envId) throws IOException {
        this.envId = envId;
        load();
    }

    public void load() throws IOException {
        var rm = Minecraft.getInstance().getResourceManager();
        var reader = rm.getResource(envId).orElseThrow().openAsReader();
        var json = JsonParser.parseReader(reader).getAsJsonObject();

        var layout = json.getAsJsonObject("layout").entrySet();
        var fog = json.getAsJsonArray("fog-heights");
        var mirrorId = json.get("mirror");
        var spect = json.getAsJsonObject("spectrogram");

        for (var entry : layout) {
            var id = entry.getKey();
            var data = entry.getValue().getAsJsonObject();
            var parts = id.split(":", 2);
            var namespace = parts[0];
            var path = "environments/" + parts[1] + ".json";
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
                } else {
                    group.addPlacement(data);
                }
            }

        }

        if (fog != null) {
            fogHeights[0] = fog.get(0).getAsFloat();
            fogHeights[1] = fog.get(1).getAsFloat();
        }

        if (mirrorId != null) {
            var id = mirrorId.getAsString();
            var parts = id.split(":", 2);
            var namespace = parts[0];
            var path = "environments/" + parts[1] + ".json";
            var loc = ResourceLocation.tryBuild(namespace, path);
            var reader2 = rm.getResource(loc).orElseThrow().openAsReader();
            var mirror = JsonParser.parseReader(reader2).getAsJsonArray();
            mirrorTris = new float[mirror.size()][];
            for (var i = 0; i < mirror.size(); ++i) {
                var tri = mirror.get(i).getAsJsonArray();
                mirrorTris[i] = new float[]{
                    tri.get(0).getAsFloat(),
                    0,
                    tri.get(1).getAsFloat(),
                };
            }
        }

        if (spect != null) {
            spectrogramData = new SpectrogramData(
                JsonUtil.getOrDefault(spect, "position", JsonUtil::getVector3, new Vector3f()),
                JsonUtil.getOrDefault(spect, "rotation", JsonUtil::getQuaternion, new Quaternionf()),
                JsonUtil.getOrDefault(spect, "offset", JsonUtil::getVector3, new Vector3f(0, 0, 2)),
                JsonUtil.getOrDefault(spect, "count", JsonElement::getAsInt, 127),
                JsonUtil.getOrDefault(spect, "style", (x) -> SpectrogramTowers.TowerStyle.fromString(x.getAsString()), SpectrogramTowers.TowerStyle.Cuboid),
                JsonUtil.getOrDefault(spect, "half-split", JsonElement::getAsBoolean, true),
                JsonUtil.getOrDefault(spect, "level-modifier", JsonElement::getAsFloat, 1f),
                JsonUtil.getOrDefault(spect, "base-height", JsonElement::getAsFloat, 0f),
                Easing.getEasing(JsonUtil.getOrDefault(spect, "easing", JsonElement::getAsString, "easeLinear")),
                JsonUtil.getOrDefault(spect, "mirror", JsonUtil::getVector4, null)
            );
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
