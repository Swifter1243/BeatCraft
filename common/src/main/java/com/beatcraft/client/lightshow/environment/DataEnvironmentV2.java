package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.instancing.lightshow.light_object.MultiLightObject;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DataEnvironmentV2 extends EnvironmentV2 {

    private DataEnvironmentV2Layout layout;

    public DataEnvironmentV2(BeatmapController map, DataEnvironmentV2Layout layout) {
        super(map, layout);
    }

    public static BeatmapController initLayout(BeatmapController map, DataEnvironmentV2Layout layout) {

        return map;
    }

    @Override
    public void setup(Object oLayout) {
        super.setup(oLayout);
        if (oLayout instanceof DataEnvironmentV2Layout data) {
            layout = data;

            var leftStaticLights = new HashMap<Integer, LightObject>();
            var leftSpinningLights = new HashMap<Integer, LightObject>();

            var rightStaticLights = new HashMap<Integer, LightObject>();
            var rightSpinningLights = new HashMap<Integer, LightObject>();

            var backLasers = new HashMap<Integer, LightObject>();

            var centerLasers = new HashMap<Integer, LightObject>();

            var ringLights = new HashMap<Integer, LightObject>();
            var innerRings = new ArrayList<LightObject>();
            var outerRings = new ArrayList<LightObject>();

            layout.lights.forEach((mesh, group) -> {
                var ls = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.LeftSpinning);
                var rs = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.RightSpinning);
                var ir = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.InnerRing);
                var or = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.OuterRing);
                var none = group.subGroups.get(DataEnvironmentV2Layout.EventGroup.None);

                if (ls != null) {
                    for (var l : ls) {

                    }
                }
                if (rs != null) {
                    for (var r : rs) {

                    }
                }
                if (ir != null) {
                    for (var i : ir) {

                    }
                }
                if (or != null) {
                    for (var o : or) {

                    }
                }
                if (none != null) {
                    for (var l : none) {

                    }
                }

            });



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
        layout.setup();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        layout.cleanup();
    }

    @Override
    public Environment reset() {
        super.reset();

        return this;
    }
}
