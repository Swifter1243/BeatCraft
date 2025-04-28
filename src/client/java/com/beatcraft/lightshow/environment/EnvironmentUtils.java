package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.environment.thefirst.TheFirstEnvironment;
import com.beatcraft.lightshow.environment.weave.WeaveEnvironment;
import com.google.gson.JsonObject;

public class EnvironmentUtils {
    public static EnvironmentV2 theFirst = null;
    public static EnvironmentV3 weave = null;

    public static Environment setupEnvironment(String environment) {
        return (switch (environment) {
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
