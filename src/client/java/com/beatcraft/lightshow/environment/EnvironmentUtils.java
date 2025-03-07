package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.lightshow.environment.the_first.Chevron;
import com.beatcraft.lightshow.environment.the_first.TheFirstEnvironment;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.lights.GlowingCuboid;
import com.beatcraft.logic.Hitbox;
import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentUtils {
    public static EnvironmentV2 theFirst = null;

    public static Environment setupEnvironment(String environment) {
        return switch (environment) {
            default -> theFirst == null ? theFirst = new TheFirstEnvironment() : theFirst;
        };
    }


    public static Environment loadV2(Difficulty difficulty, JsonObject json) {
        Environment env = setupEnvironment(difficulty.getInfo().getEnvironmentName());


        if (env instanceof EnvironmentV2 env2) {
            env2.loadLightshow(difficulty, json);
        }

        return env;
    }


}
