package com.beatcraft.client.lightshow.environment;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.environment.big_mirror.BigMirrorEnvironment;
import com.beatcraft.client.lightshow.environment.origins.OriginsEnvironment;
import com.beatcraft.client.lightshow.environment.triangle.TriangleEnvironment;
import com.beatcraft.client.lightshow.environment.weave.WeaveEnvironment;
import com.google.gson.JsonObject;

import java.io.IOException;

public class EnvironmentUtils {

    private static DataEnvironmentV2Layout theFirst = null;
    private static DataEnvironmentV2Layout triangle;
    private static DataEnvironmentV2Layout kaleidoscope;
    private static DataEnvironmentV2Layout nice;


    public static void reload() throws IOException {
        theFirst = new DataEnvironmentV2Layout(Beatcraft.id("environments/thefirst/env.json"));
        triangle = new DataEnvironmentV2Layout(Beatcraft.id("environments/triangle/env.json"));
        kaleidoscope = new DataEnvironmentV2Layout(Beatcraft.id("environments/kaleidoscope/env.json"));
        nice = new DataEnvironmentV2Layout(Beatcraft.id("environments/nice/env.json"));
    }

    public static Environment setupEnvironment(String environment, BeatmapController map) {
        if (theFirst == null) {
            try {
                reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return (switch (environment) {
            case "OriginsEnvironment" -> new OriginsEnvironment(map);
            case "TriangleEnvironment" -> new DataEnvironmentV2(map, triangle);
            case "NiceEnvironment" -> new DataEnvironmentV2(map, nice);
            case "KaleidoscopeEnvironment" -> new DataEnvironmentV2(map, kaleidoscope);
            case "WeaveEnvironment" -> new WeaveEnvironment(map);
            case "BigMirrorEnvironment" -> new BigMirrorEnvironment(map);
            default -> new DataEnvironmentV2(map, theFirst);
        }).reset();
    }

    public static Environment load(BeatmapController map, JsonObject json) {
        Environment env = setupEnvironment(map.difficulty.getInfo().getEnvironmentName(), map);
        env.loadLightshow(map.difficulty, json);
        if (env.meshes != null) env.meshes.load();
        return env;
    }

}
