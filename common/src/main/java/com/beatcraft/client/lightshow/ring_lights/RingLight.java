package com.beatcraft.client.lightshow.ring_lights;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMeshInstance;
import com.beatcraft.client.render.instancing.lightshow.light_object.MultiLightObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class RingLight extends MultiLightObject {

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
        super(map, pos, ori, lightMesh, extraStatesCount);
        if (!rings.containsKey(lightMesh)) {
            rings.put(lightMesh, new ArrayList<>());
        }
        rings.get(lightMesh).add(this);
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

}
