package com.beatcraft.client.lightshow.ring_lights;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMeshInstance;
import com.beatcraft.common.data.types.Color;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class RingLight extends LightObject {

    private final int stateCount;
    private final LightState[] states;
    private final LightMesh lightMesh;

    public class SubLightController extends LightObject {

        private final int target;

        public SubLightController(BeatmapController map, int target) {
            super(map);
            this.target = target;
        }

        @Override
        public LightObject cloneOffset(Vector3f offset) {
            // sub-controller should never be cloned
            throw new IllegalStateException("SubLightController was cloned.");
        }

        @Override
        public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
            // sub-controller does not render
        }

        @Override
        public void setBrightness(float value) {
            RingLight.this.states[target].setBrightness(value);
        }

        @Override
        public void setColor(int color) {
            RingLight.this.states[target].setColor(new Color(color));
        }

        @Override
        public void setLightState(LightState state) {
            RingLight.this.states[target].set(state);
        }
    }

    public LightObject[] getControllers() {
        var controllers = new LightObject[stateCount];
        for (int i = 0; i < stateCount; ++i) {
            controllers[i] = new SubLightController(mapController, i);
        }
        return controllers;
    }

    private LightMeshInstance mesh;

    private static final HashMap<LightMesh, ArrayList<RingLight>> rings = new HashMap<>();

    public static void clearInstances() {
        for (var arr : rings.values()) {
            arr.clear();
        }
    }

    public static void clearInstances(LightMesh key) {
        var arr = rings.get(key);
        if (arr != null) arr.clear();
    }

    public static void reload() {
        for (var key : rings.keySet()) {
            for (var ring : rings.get(key)) {
                ring.mesh = new LightMeshInstance(key);
            }
        }
    }


    public RingLight(BeatmapController map, Vector3f pos, Quaternionf ori, LightMesh lightMesh, int extraStatesCount) {
        super(map);
        if (!rings.containsKey(lightMesh)) {
            rings.put(lightMesh, new ArrayList<>());
        }
        rings.get(lightMesh).add(this);
        position = pos;
        orientation = ori;
        this.lightMesh = lightMesh;
        this.stateCount = extraStatesCount;

        mesh = new LightMeshInstance(lightMesh);

        states = new LightState[extraStatesCount];

        for (int i = 0; i < stateCount; ++i) {
            states[i] = new LightState(new Color(), 0);
        }
        lightState = new LightState(new Color(), 0);
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new RingLight(
            mapController,
            position.add(offset, new Vector3f()),
            new Quaternionf(orientation),
            lightMesh,
            stateCount
        );
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var mat = createTransformMatrix(matrices.last().pose(), false, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);
        mesh.transform.set(mat);

        mesh.setColor(0, lightState);
        for (int i = 0; i < stateCount; i++) {
            mesh.setColor(i+1, states[i]);
        }
        mesh.draw(mapController.worldPosition);
    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }

    @Override
    public void setLightState(LightState state) {
        lightState.set(state);
    }

}
