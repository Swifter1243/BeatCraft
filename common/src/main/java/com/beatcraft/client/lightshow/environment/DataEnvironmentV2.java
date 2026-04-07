package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.google.gson.JsonObject;

public class DataEnvironmentV2 extends EnvironmentV2 {

    private final DataEnvironmentV2Layout layout;

    public DataEnvironmentV2(BeatmapController map, DataEnvironmentV2Layout layout) {
        super(map);
        this.layout = layout;
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
        // TODO: load meshes
    }

    @Override
    public void cleanup() {
        super.cleanup();
        // TODO: unload meshes
    }

    @Override
    public Environment reset() {
        super.reset();

        return this;
    }
}
