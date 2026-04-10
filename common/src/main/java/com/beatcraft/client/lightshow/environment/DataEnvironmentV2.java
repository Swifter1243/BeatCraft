package com.beatcraft.client.lightshow.environment;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.EventGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.client.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.ring_lights.RingLightHandler;
import com.beatcraft.client.lightshow.spectrogram.SpectrogramTowers;
import com.beatcraft.client.render.instancing.lightshow.light_object.MultiLightObject;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DataEnvironmentV2 extends EnvironmentV2 {

    private DataEnvironmentV2Layout layout;

    private float[] fogHeights;
    private float[][] mirrorTris;

    private SpectrogramTowers leftSpectrogramTowers;
    private SpectrogramTowers rightSpectrogramTowers;

    public DataEnvironmentV2(BeatmapController map, DataEnvironmentV2Layout layout) {
        super(map, layout);
    }

    @Override
    public void setup(Object oLayout) {
        if (oLayout instanceof DataEnvironmentV2Layout data) {
            layout = data;

            fogHeights = layout.fogHeights;
            mirrorTris = layout.mirrorTris;

            var leftSpinningLights = new HashMap<Integer, LightObject>();
            var leftStaticLights = new HashMap<Integer, LightObject>();

            var rightStaticLights = new HashMap<Integer, LightObject>();
            var rightSpinningLights = new HashMap<Integer, LightObject>();

            var backLights = new HashMap<Integer, LightObject>();

            var centerLights = new HashMap<Integer, LightObject>();

            var ringLights = new HashMap<Integer, LightObject>();
            var innerRings = new ArrayList<LightObject>();
            var outerRings = new ArrayList<LightObject>();

            var innerRingPos = new Vector3f();
            var innerRingGap = 5f;
            var innerAngles = new float[]{0};
            var innerDeltas = new float[]{0};

            var outerRingPos = new Vector3f();
            var outerRingGap = 5f;
            var outerAngles = new float[]{0};
            var outerDeltas = new float[]{0};
            var innerStartA = 0f;
            var innerStartD = 0f;
            var outerStartA = 0f;
            var outerStartD = 0f;

            var leftSpinAxis = new Vector3f(0, 1, 0);
            var rightSpinAxis = new Vector3f(0, 1, 0);

            var statics = new ArrayList<LightObject>();

            for (var entry : layout.lights.entrySet()) {
                var mesh = entry.getKey();
                var group = entry.getValue();
                var ls = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.LeftSpinning);
                var rs = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.RightSpinning);
                var ir = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.InnerRing);
                var or = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.OuterRing);
                var none = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.None);

                if (ls != null) {
                    for (var l : ls) {
                        leftSpinAxis = l.spinAxis;
                        var pos = new Vector3f(l.position);
                        var rot = new Quaternionf(l.rotation);
                        var ori = new Quaternionf(l.orientation);
                        if (l.count <= 0) {
                            l.count = 1;
                        }
                        for (var i = 0; i < l.count; ++i) {
                            var base = new MultiLightObject(mapController, new Vector3f(pos), new Quaternionf(rot), mesh, Math.max(0, l.ids.length-1));
                            base.setOrientation(new Quaternionf(ori));
                            var extra = base.getControllers();
                            var idx = 0;
                            for (var id : l.ids) {

                                LightObject c;
                                if (idx == 0) {
                                    c = base;
                                } else {
                                    c = extra[idx-1];
                                }
                                var num = id.id() + l.idOffsets.get(idx) * i;
                                leftSpinningLights.put(num, c);
                                var group1 = switch (id.group()) {
                                    case LEFT_LASERS -> leftStaticLights;
                                    case RIGHT_LASERS -> rightStaticLights;
                                    case CENTER_LASERS -> centerLights;
                                    case BACK_LASERS -> backLights;
                                    case RING_LIGHTS -> ringLights;
                                };
                                group1.put(num, c);

                                ++idx;
                            }

                            if (idx == 0) {
                                statics.add(base);
                            }

                            pos.add(l.offset);
                            rot.mul(l.rotOffset);
                            ori.mul(l.oriOffset);
                        }
                    }
                }
                if (rs != null) {
                    for (var r : rs) {
                        rightSpinAxis = r.spinAxis;
                        var pos = new Vector3f(r.position);
                        var rot = new Quaternionf(r.rotation);
                        var ori = new Quaternionf(r.orientation);
                        if (r.count <= 0) {
                            r.count = 1;
                        }
                        for (var i = 0; i < r.count; ++i) {
                            var base = new MultiLightObject(mapController, new Vector3f(pos), new Quaternionf(rot), mesh, Math.max(0, r.ids.length-1));
                            base.setOrientation(new Quaternionf(ori));
                            var extra = base.getControllers();
                            var idx = 0;
                            for (var id : r.ids) {

                                LightObject c;
                                if (idx == 0) {
                                    c = base;
                                } else {
                                    c = extra[idx-1];
                                }
                                var num = id.id() + r.idOffsets.get(idx) * i;
                                rightSpinningLights.put(num, c);
                                var group1 = switch (id.group()) {
                                    case LEFT_LASERS -> leftStaticLights;
                                    case RIGHT_LASERS -> rightStaticLights;
                                    case CENTER_LASERS -> centerLights;
                                    case BACK_LASERS -> backLights;
                                    case RING_LIGHTS -> ringLights;
                                };
                                group1.put(num, c);

                                ++idx;
                            }

                            if (idx == 0) {
                                statics.add(base);
                            }

                            pos.add(r.offset);
                            rot.mul(r.rotOffset);
                            ori.mul(r.oriOffset);
                        }
                    }
                }
                if (ir != null) {
                    for (var l : ir) {
                        innerRingPos.set(l.position);
                        innerRingGap = l.offset.z;
                        innerAngles = l.anglesRadians;
                        innerDeltas = l.deltasRadians;
                        innerStartA = l.startAngleRadians;
                        innerStartD = l.startDeltaRadians;

                        var rot = new Quaternionf(l.rotation);
                        var ori = new Quaternionf(l.orientation);

                        for (var i = 0; i < l.count; ++i) {
                            var base = new MultiLightObject(mapController, new Vector3f(), new Quaternionf(rot), mesh, Math.max(0, l.ids.length-1));
                            base.setOrientation(new Quaternionf(ori));
                            var extra = base.getControllers();
                            var idx = 0;
                            innerRings.add(base);
                            for (var id : l.ids) {

                                LightObject c;
                                if (idx == 0) {
                                    c = base;
                                } else {
                                    c = extra[idx-1];
                                }
                                var num = id.id() + l.idOffsets.get(idx) * i;
                                var group2 = switch (id.group()) {
                                    case LEFT_LASERS -> leftStaticLights;
                                    case RIGHT_LASERS -> rightStaticLights;
                                    case CENTER_LASERS -> centerLights;
                                    case BACK_LASERS -> backLights;
                                    case RING_LIGHTS -> ringLights;
                                };
                                group2.put(num, c);

                                ++idx;
                            }

                            rot.mul(l.rotOffset);
                            ori.mul(l.oriOffset);
                        }
                    }
                }
                if (or != null) {
                    for (var l : or) {
                        outerRingPos.set(l.position);
                        outerRingGap = l.offset.z;
                        outerAngles = l.anglesRadians;
                        outerDeltas = l.deltasRadians;
                        outerStartA = l.startAngleRadians;
                        outerStartD = l.startDeltaRadians;

                        var rot = new Quaternionf(l.rotation);
                        var ori = new Quaternionf(l.orientation);

                        for (var i = 0; i < l.count; ++i) {
                            var base = new MultiLightObject(mapController, new Vector3f(), new Quaternionf(rot), mesh, Math.max(0, l.ids.length-1));
                            base.setOrientation(new Quaternionf(ori));
                            var extra = base.getControllers();
                            var idx = 0;
                            outerRings.add(base);
                            for (var id : l.ids) {

                                LightObject c;
                                if (idx == 0) {
                                    c = base;
                                } else {
                                    c = extra[idx-1];
                                }
                                var num = id.id() + l.idOffsets.get(idx) * i;
                                var group2 = switch (id.group()) {
                                    case LEFT_LASERS -> leftStaticLights;
                                    case RIGHT_LASERS -> rightStaticLights;
                                    case CENTER_LASERS -> centerLights;
                                    case BACK_LASERS -> backLights;
                                    case RING_LIGHTS -> ringLights;
                                };
                                group2.put(num, c);

                                ++idx;
                            }

                            rot.mul(l.rotOffset);
                            ori.mul(l.oriOffset);
                        }
                    }
                }
                if (none != null) {
                    for (var l : none) {
                        var pos = new Vector3f(l.position);
                        var rot = new Quaternionf(l.rotation);
                        var ori = new Quaternionf(l.orientation);
                        if (l.count <= 0) {
                            l.count = 1;
                        }
                        for (var i = 0; i < l.count; ++i) {
                            var base = new MultiLightObject(mapController, new Vector3f(pos), new Quaternionf(rot), mesh, Math.max(0, l.ids.length-1));
                            base.setOrientation(new Quaternionf(ori));
                            var extra = base.getControllers();
                            var idx = 0;
                            for (var id : l.ids) {

                                LightObject c;
                                if (idx == 0) {
                                    c = base;
                                } else {
                                    c = extra[idx-1];
                                }
                                var num = id.id() + l.idOffsets.get(idx) * i;
                                var group2 = switch (id.group()) {
                                    case LEFT_LASERS -> leftStaticLights;
                                    case RIGHT_LASERS -> rightStaticLights;
                                    case CENTER_LASERS -> centerLights;
                                    case BACK_LASERS -> backLights;
                                    case RING_LIGHTS -> ringLights;
                                };
                                group2.put(num, c);

                                ++idx;
                            }

                            pos.add(l.offset);
                            rot.mul(l.rotOffset);
                            ori.mul(l.oriOffset);
                        }
                    }
                }

            }

            for (var mesh : layout.statics) {
                var base = new MultiLightObject(mapController, new Vector3f(), new Quaternionf(), mesh, 0);
                statics.add(base);
            }

            lightGroups = new HashMap<>();
            uniqueGroups = new ArrayList<>();

            var leftLasers = new RotatingLightsGroup(mapController, leftSpinningLights, leftStaticLights, leftSpinAxis);
            bindLightGroup(EventGroup.LEFT_LASERS, leftLasers);
            bindLightGroup(EventGroup.LEFT_ROTATING_LASERS, leftLasers);

            var rightLasers = new RotatingLightsGroup(mapController, rightSpinningLights, rightStaticLights, rightSpinAxis);
            bindLightGroup(EventGroup.RIGHT_LASERS, rightLasers);
            bindLightGroup(EventGroup.RIGHT_ROTATING_LASERS, rightLasers);

            var backLasers = new StaticLightsGroup(mapController, backLights);
            bindLightGroup(EventGroup.BACK_LASERS, backLasers);

            var centerLasers = new StaticLightsGroup(mapController, centerLights);
            centerLasers.unmappedLights = statics;
            bindLightGroup(EventGroup.CENTER_LASERS, centerLasers);

            var rings = new RingLightHandler(
                mapController,
                ringLights,
                innerRings,
                innerRingPos,
                innerRingGap,
                new RingLightHandler.PresetPositions(
                    innerAngles,
                    innerDeltas
                ),
                outerRings,
                outerRingPos,
                outerRingGap,
                new RingLightHandler.PresetPositions(
                    outerAngles,
                    outerDeltas
                ),
                new float[]{innerStartA, innerStartD, outerStartA, outerStartD}
            );
            bindLightGroup(EventGroup.RING_LIGHTS, rings);
            bindLightGroup(EventGroup.RING_SPIN, rings);
            bindLightGroup(EventGroup.RING_ZOOM, rings);

        } else {
            throw new RuntimeException("setup expected Layout object");
        }
    }

    @Override
    protected LightGroupV2 setupLeftLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        return null;
    }

    @Override
    protected LightGroupV2 setupRingLights() {
        return null;
    }

    @Override
    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        super.loadLightshow(difficulty, json);

        if (layout.spectrogramData != null) {
            var f = new File(difficulty.getInfo().getSongFilename());

            var pair = layout.spectrogramData.build(mapController, f);
            leftSpectrogramTowers = pair.a();
            rightSpectrogramTowers = pair.b();
        }

        layout.setup();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        layout.cleanup();
    }

    @Override
    public float[] getFogHeights() {
        return fogHeights;
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha) {
        super.render(matrices, camera, alpha);

        if (BeatcraftClient.playerConfig.quality.renderEnvironment()) {
            var matrices2 = new PoseStack();
            matrices2.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            matrices2.mulPose(matrices.last().pose());
            mapController.mirrorHandler.recordCall((buffer, cameraPos, invCameraRotation) -> {
                renderMesh(buffer, matrices2, alpha, mirrorTris);
            });
        }
        var t = mapController.currentSeconds;

        if (leftSpectrogramTowers != null) {
            leftSpectrogramTowers.render(t);
        }
        if (rightSpectrogramTowers != null) {
            rightSpectrogramTowers.render(t);
        }

    }

    @Override
    public Environment reset() {
        super.reset();

        return this;
    }

    private static final Color BLACK = new Color(0, 0, 0, 1);

    private static void renderMesh(BufferBuilder buffer, PoseStack matrices, float alpha, float[][] mesh) {
        var black = BLACK.toARGB(alpha);
        var mat4 = matrices.last();

        var vert = MemoryPool.newVector3f();

        for (var vertex : mesh) {
            mat4.pose().transformPosition(vertex[0], vertex[1], vertex[2], vert);
            buffer.addVertex(vert.x, vert.y, vert.z).setColor(black);
        }

        MemoryPool.release(vert);

    }
}
