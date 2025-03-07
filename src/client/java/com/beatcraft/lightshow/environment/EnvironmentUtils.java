package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.environment.thefirst.TheFirstEnvironment;
import com.google.gson.JsonObject;

public class EnvironmentUtils {
    public static EnvironmentV2 theFirst = null;

    public static Environment setupEnvironment(String environment) {
        return (switch (environment) {
            default -> theFirst == null ? theFirst = new TheFirstEnvironment() : theFirst;
        }).reset();
    }


    public static Environment loadV2(Difficulty difficulty, JsonObject json) {
        Environment env = setupEnvironment(difficulty.getInfo().getEnvironmentName());


        if (env instanceof EnvironmentV2 env2) {
            env2.loadLightshow(difficulty, json);
        }

        return env;
    }


}
