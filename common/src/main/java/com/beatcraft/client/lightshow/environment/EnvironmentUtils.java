package com.beatcraft.client.lightshow.environment;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.environment.kaleidoscope.KaleidoscopeEnvironment;
import com.beatcraft.client.lightshow.environment.nice.NiceEnvironment;
import com.beatcraft.client.lightshow.environment.origins.OriginsEnvironment;
import com.beatcraft.client.lightshow.environment.thefirst.TheFirstEnvironment;
import com.beatcraft.client.lightshow.environment.triangle.TriangleEnvironment;
import com.beatcraft.client.lightshow.environment.weave.WeaveEnvironment;
import com.google.gson.JsonObject;

public class EnvironmentUtils {

    public static Environment setupEnvironment(String environment, BeatmapPlayer map) {
        return (switch (environment) {
            case "OriginsEnvironment" -> new OriginsEnvironment(map);
            case "TriangleEnvironment" -> new TriangleEnvironment(map);
            case "NiceEnvironment" -> new NiceEnvironment(map);
            case "KaleidoscopeEnvironment" -> new KaleidoscopeEnvironment(map);
            case "WeaveEnvironment" -> new WeaveEnvironment(map);
            default -> new TheFirstEnvironment(map);
        }).reset();
    }

    public static Environment load(BeatmapPlayer map, JsonObject json) {
        Environment env = setupEnvironment(map.difficulty.getInfo().getEnvironmentName(), map);
        env.loadLightshow(map.difficulty, json);
        return env;
    }

}
