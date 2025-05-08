package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.environment.nice.NiceEnvironment;
import com.beatcraft.lightshow.environment.origins.OriginsEnvironment;
import com.beatcraft.lightshow.environment.thefirst.TheFirstEnvironment;
import com.beatcraft.lightshow.environment.triangle.TriangleEnvironment;
import com.beatcraft.lightshow.environment.weave.WeaveEnvironment;
import com.google.gson.JsonObject;

public class EnvironmentUtils {
    public static EnvironmentV2 theFirst = null;
    public static EnvironmentV2 origins = null;
    public static EnvironmentV2 triangle = null;
    public static EnvironmentV2 nice = null;
    public static EnvironmentV3 weave = null;

    public static Environment setupEnvironment(String environment) {
        nice = null;
        return (switch (environment) {
            case "OriginsEnvironment" -> origins == null ? origins = new OriginsEnvironment() : origins;
            case "TriangleEnvironment" -> triangle == null ? triangle = new TriangleEnvironment() : triangle;
            case "NiceEnvironment" -> nice == null ? nice = new NiceEnvironment() : nice;
            case "WeaveEnvironment" -> weave == null ? weave = new WeaveEnvironment() : weave;
            default -> theFirst == null ? theFirst = new TheFirstEnvironment() : theFirst;
        }).reset();
    }


    public static Environment load(Difficulty difficulty, JsonObject json) {
        Environment env = setupEnvironment(difficulty.getInfo().getEnvironmentName());

        env.loadLightshow(difficulty, json);

        return env;
    }

}
