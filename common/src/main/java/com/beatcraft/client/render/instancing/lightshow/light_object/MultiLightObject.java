package com.beatcraft.client.render.instancing.lightshow.light_object;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.common.data.types.Color;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MultiLightObject extends LightObject {

    public final int stateCount;
    public final LightState[] states;
    public final LightMesh lightMesh;

    public class SubLightController extends LightObject {

        private final int target;

        public SubLightController(BeatmapController map, int target) {
            super(map);
            this.target = target;
        }

        @Override
        public LightObject cloneOffset(Vector3f offset) {
            throw new IllegalStateException("SubLightController was cloned.");
        }

        @Override
        public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {}

        @Override
        public void setBrightness(float value) {
            MultiLightObject.this.states[target].setBrightness(value);
        }

        @Override
        public void setColor(int color) {
            MultiLightObject.this.states[target].setColor(new Color(color));
        }

        @Override
        public void setLightState(LightState state) {
            MultiLightObject.this.states[target].set(state);
        }
    }

    public LightObject[] getControllers() {
        var controllers = new LightObject[stateCount];
        for (int i = 0; i < stateCount; ++i) {
            controllers[i] = new SubLightController(mapController, i);
        }
        return controllers;
    }

    public LightMeshInstance mesh;

    public MultiLightObject(BeatmapController map, Vector3f pos, Quaternionf ori, LightMesh mesh, int additionalStateCount) {
        super(map);
        stateCount = additionalStateCount;
        lightMesh = mesh;
        position = pos;
        orientation = ori;
        this.mesh = new LightMeshInstance(lightMesh);

        states = new LightState[stateCount];

        for (int i = 0; i < stateCount; ++i) {
            states[i] = new LightState(new Color(), 0);
        }
        lightState = new LightState(new Color(), 0);
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new MultiLightObject(
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
