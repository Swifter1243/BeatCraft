package com.beatcraft.lightshow.environment.kaleidoscope;

import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.render.instancing.lightshow.light_object.LightMeshObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

public class RingSpike extends LightObject {

    // use default light state + 7 extra
    private final LightState[] states = new LightState[7];

    public class SubLightController extends LightObject {

        private final int target;

        public SubLightController(int target) {
            this.target = target;
        }

        @Override
        public LightObject cloneOffset(Vector3f offset) {
            // sub-controller should never be cloned
            throw new IllegalStateException("SubLightController was cloned.");
        }

        @Override
        public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
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
    }


    /// This returns controllers for all the additional lights on the ring spike
    public LightObject[] getControllers() {

        var rightTip = new SubLightController(0);
        var leftRing = new SubLightController(1);
        var rightRing = new SubLightController(2);
        var left = new SubLightController(3);
        var right = new SubLightController(4);
        var leftBack = new SubLightController(5);
        var rightBack = new SubLightController(6);

        return new LightObject[]{
            rightTip,
            leftRing, rightRing,
            left, right,
            leftBack, rightBack
        };

    }

    private LightMeshObject mesh;

    public RingSpike(Vector3f pos, Quaternionf ori) {
        position = pos;
        orientation = ori;

        for (int i = 0; i < 7; i++) {
            states[i] = new LightState(new Color(), 0);
        }
        lightState = new LightState(new Color(), 0);
    }

    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new RingSpike(
            position.add(offset, new Vector3f()),
            new Quaternionf(orientation)
        );
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        mesh.setColor(0, lightState);
        for (int i = 0; i < 7; i++) {
            mesh.setColor(i+1, states[i]);
        }
    }

    @Override
    public void setBrightness(float value) {
        lightState.setBrightness(value);
    }

    @Override
    public void setColor(int color) {
        lightState.setColor(new Color(color));
    }

}
