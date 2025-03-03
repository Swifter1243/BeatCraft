package com.beatcraft.lightshow.environment;

import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.the_first.RotatingLightsGroup;
import com.beatcraft.lightshow.environment.the_first.StaticLightsGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.lights.GlowingCuboid;
import com.beatcraft.logic.Hitbox;
import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Environments {

    public static EnvironmentV2 theFirst = null;

    /*
    * Environment notes:
    * back-lights are ~33 degrees down from vertical
    *
    * The First:
    * 4 lights on left and right each.
    * ~55 degrees down from vertical
    * spin speed 1 = 1/2 rev in 9 seconds
    * spin speed 9 = 1/2 rev in 1 second
    * current rotation is randomized on speed event
    *
    * side lights: roughly at z 16 and 20 and x +- 20. further z is ~2.5 blocks higher starting at Y ~-15 or 20 and maybe 30-50 tall
    *
    * ring rotation offsets (roughly): 0, 2, 5, 10, 15 or randomly between -10 and 10 degrees
    *
    * aboveTrackX: includes chevron and continues the 10 lasers from belowTrackX
    *
    */

    public static Environment setupEnvironment(String environment) {
        return switch (environment) {
            default -> setupTheFirst();
        };
    }

    public static Environment setupTheFirst() {
        if (theFirst != null) return theFirst;

        theFirst = new EnvironmentV2();

        // left/right rotating light groups
        var left1 = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.03f, -10, -0.03f),
                new Vector3f(0.03f, 800, 0.03f)
            ),
            new Vector3f(15, 2, 30),
            new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
        );

        var right1 = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.03f, -10, -0.03f),
                new Vector3f(0.03f, 800, 0.03f)
            ),
            new Vector3f(-15, 2, 30),
            new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
        );

        var offset = new Vector3f(0, -0.01f, 5);

        var left2 = left1.cloneOffset(offset);
        var left3 = left2.cloneOffset(offset);
        var left4 = left3.cloneOffset(offset);

        var right2 = right1.cloneOffset(offset);
        var right3 = right2.cloneOffset(offset);
        var right4 = right3.cloneOffset(offset);

        var leftRunway = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.03f, -0.03f, 0),
                new Vector3f(0.03f, 0.03f, 500)
            ),
            new Vector3f(3.5f, 0, 8),
            new Quaternionf()
        );

        var rightRunway = leftRunway.cloneOffset(new Vector3f(-7, 0, 0));

        var leftTowerLight1 = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.1f, -20, -0.1f),
                new Vector3f(0.1f, 8.5f, 0.1f)
            ),
            new Vector3f(20, 0, 16),
            new Quaternionf()
        );

        var leftTowerLight2 = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.1f, -15, -0.1f),
                new Vector3f(0.1f, 11.5f, 0.1f)
            ),
            new Vector3f(20, 0, 19),
            new Quaternionf()
        );

        var rightTowerLight1 = leftTowerLight1.cloneOffset(new Vector3f(-40, 0, 0));
        var rightTowerLight2 = leftTowerLight2.cloneOffset(new Vector3f(-40, 0, 0));


        LightGroupV2 lg = new RotatingLightsGroup(List.of(left1, left2, left3, left4), List.of(leftRunway, leftTowerLight1, leftTowerLight2));
        LightGroupV2 rg = new RotatingLightsGroup(List.of(right1, right2, right3, right4), List.of(rightRunway, rightTowerLight1, rightTowerLight2));

        theFirst.bindLightGroup(EventGroup.LEFT_LASERS, lg);
        theFirst.bindLightGroup(EventGroup.RIGHT_LASERS, rg);

        theFirst.bindLightGroup(EventGroup.LEFT_ROTATING_LASERS, lg);
        theFirst.bindLightGroup(EventGroup.RIGHT_ROTATING_LASERS, rg);

        // above/below track X

        // roughly 58 degrees on X, 33 on Z

        var bottomLeftLaser = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.05f, -150, -0.05f),
                new Vector3f(0.05f, 0, 0.05f)
            ),
            new Vector3f(2f, -2, 45),
            new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
        );

        var topLeftLaser = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.05f, 0, -0.05f),
                new Vector3f(0.05f, 150, 0.05f)
            ),
            new Vector3f(2f, -2, 45),
            new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
        );

        var bottomRightLaser = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.05f, -150, -0.05f),
                new Vector3f(0.05f, 0, 0.05f)
            ),
            new Vector3f(-2f, -2, 45),
            new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
        );

        var topRightLaser = new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.05f, 0, -0.05f),
                new Vector3f(0.05f, 150, 0.05f)
            ),
            new Vector3f(-2f, -2, 45),
            new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
        );

        offset = new Vector3f(0, 0, 10);

        ArrayList<LightObject> l1 = new ArrayList<>(List.of(bottomLeftLaser));
        ArrayList<LightObject> l2 = new ArrayList<>(List.of(topLeftLaser));
        ArrayList<LightObject> r1 = new ArrayList<>(List.of(bottomRightLaser));
        ArrayList<LightObject> r2 = new ArrayList<>(List.of(topRightLaser));

        for (int i = 0; i < 5; i++) {
            l1.add(((GlowingCuboid) l1.getLast()).cloneOffset(offset));
            l2.add(((GlowingCuboid) l2.getLast()).cloneOffset(offset));
            r1.add(((GlowingCuboid) r1.getLast()).cloneOffset(offset));
            r2.add(((GlowingCuboid) r2.getLast()).cloneOffset(offset));
        }

        var bg = new StaticLightsGroup(l1);
        var tg = new StaticLightsGroup(l2);

        bg.lights.addAll(r1);
        tg.lights.addAll(r2);

        theFirst.bindLightGroup(EventGroup.BACK_LASERS, bg);
        theFirst.bindLightGroup(EventGroup.CENTER_LASERS, tg);

        return theFirst;
    }


    public static Environment loadV2(Difficulty difficulty, JsonObject json) {
        Environment env = setupEnvironment(difficulty.getInfo().getEnvironmentName());


        if (env instanceof EnvironmentV2 env2) {
            env2.loadLightshow(difficulty, json);
        }

        return env;
    }


}
