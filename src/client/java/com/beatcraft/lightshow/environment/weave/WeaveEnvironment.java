package com.beatcraft.lightshow.environment.weave;

import com.beatcraft.lightshow.environment.EnvironmentV3;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV3;
import com.beatcraft.lightshow.environment.lightgroup.OrientableLightGroup;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.TransformEvent;
import com.beatcraft.lightshow.event.handlers.LightGroupEventHandlerV3;
import com.beatcraft.lightshow.event.handlers.TransformEventHandlerV3;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.lights.FloodLight;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeaveEnvironment extends EnvironmentV3 {
    /*
     * Environment notes:
     * 4 major groups
     * each with 4 subgroups
     *
     * 0,0 is at x:7ish y:-0.125ish, z:8
     *
     *
     */

    private LightGroupV3 outerSquareLightGroupBL;
    private LightGroupV3 outerSquareLightGroupBR;
    private LightGroupV3 outerSquareLightGroupTL;
    private LightGroupV3 outerSquareLightGroupTR;

    private LightGroupV3 innerSquareLightGroupBL;
    private LightGroupV3 innerSquareLightGroupBR;
    private LightGroupV3 innerSquareLightGroupTL;
    private LightGroupV3 innerSquareLightGroupTR;

    private LightGroupV3 sideSquareLightGroupBL;
    private LightGroupV3 sideSquareLightGroupBR;
    private LightGroupV3 sideSquareLightGroupTL;
    private LightGroupV3 sideSquareLightGroupTR;

    private LightGroupV3 distantSquareLightGroupT;
    private LightGroupV3 distantSquareLightGroupB;
    private LightGroupV3 distantSquareLightGroupL;
    private LightGroupV3 distantSquareLightGroupR;

    HashMap<Integer[], Pair<LightGroupEventHandlerV3, TransformEventHandlerV3>> eventGroups;

    @Override
    public String getID() {
        return "WeaveEnvironment";
    }

    @Override
    public void setup() {

    }

    private List<LightObject> stackLights(LightObject light, Vector3f step, int count) {
        var result = new ArrayList<LightObject>();
        result.add(light);
        for (int i = 0; i < count; i++) {
            light = light.cloneOffset(step);
            result.add(light);
        }
        return result;
    }

    private void setupOuterLights() {
        int lightID = 0;
        var lights = new HashMap<Integer, LightObject>();

        for (var light : stackLights(new FloodLight(0.25f, 0.075f, 30, 14, 0.1f, new Vector3f(7, -0.125f, 8), new Quaternionf()), new Vector3f(0, 0, 1), 7)) {
            lights.put(lightID++, light);
        }

        outerSquareLightGroupBL = new OrientableLightGroup(lights);



    }

    private void setupInnerLights() {

    }

    private void setupSideLights() {

    }

    private void setupDistantLights() {

    }

    @Override
    public WeaveEnvironment reset() {
        return this;
    }

    @Override
    protected int getLightCount(int group) {
        return 0;
    }

    @Override
    protected void linkEvents(int group, int lightID, List<LightEventV3> lightEvents, List<TransformEvent> transformEvents) {



    }


    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
        outerSquareLightGroupBL.render(matrices, camera);
    }
}
