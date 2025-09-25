package com.beatcraft.client.lightshow.environment.kaleidoscope;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMeshInstance;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class RingSpike extends LightObject {

    // use default light state + 7 extra
    private final LightState[] states = new LightState[7];

    public RingSpike(BeatmapController map) {
        super(map);
    }

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
            RingSpike.this.states[target].setBrightness(value);
        }

        @Override
        public void setColor(int color) {
            RingSpike.this.states[target].setColor(new Color(color));
        }

        @Override
        public void setLightState(LightState state) {
            RingSpike.this.states[target].set(state);
        }
    }


    /// This returns controllers for all the additional lights on the ring spike
    public LightObject[] getControllers() {

        var rightTip = new SubLightController(mapController, 0);
        var leftRing = new SubLightController(mapController, 1);
        var rightRing = new SubLightController(mapController, 2);
        var left = new SubLightController(mapController, 3);
        var right = new SubLightController(mapController, 4);
        var leftBack = new SubLightController(mapController, 5);
        var rightBack = new SubLightController(mapController, 6);

        return new LightObject[]{
            rightTip,
            leftRing, rightRing,
            left, right,
            leftBack, rightBack
        };

    }

    private LightMeshInstance mesh;

    private static ArrayList<RingSpike> spikes = new ArrayList<>();

    public static void clearInstances() {
        spikes.clear();
    }

    public static void reload() {
        for (var spike : spikes) {
            spike.mesh = new LightMeshInstance(MeshLoader.KALEIDOSCOPE_SPIKE);
        }
    }

    public RingSpike(BeatmapController map, Vector3f pos, Quaternionf ori) {
        super(map);
        spikes.add(this);
        position = pos;
        orientation = ori;

        mesh = new LightMeshInstance(MeshLoader.KALEIDOSCOPE_SPIKE);

        for (int i = 0; i < 7; i++) {
            states[i] = new LightState(new Color(), 0);
        }
        lightState = new LightState(new Color(), 0);
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new RingSpike(
            mapController,
            position.add(offset, new Vector3f()),
            new Quaternionf(orientation)
        );
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var mat = createTransformMatrix(matrices.last().pose(), false, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);
        mesh.transform.set(mat);

        mesh.setColor(0, lightState);
        for (int i = 0; i < 7; i++) {
            mesh.setColor(i+1, states[i]);
        }
        mesh.draw();
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
