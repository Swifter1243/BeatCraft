package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.lights.GlowingCuboid;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class TheFirstEnvironment extends EnvironmentV2 {
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

    private static GlowingCuboid getRunway(boolean isLeft)
    {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -0.03f, 0),
                        new Vector3f(0.03f, 0.03f, 500)
                ),
                new Vector3f(3.5f * sign, 0, 8),
                new Quaternionf()
        );
    }
    private static GlowingCuboid getTowerLight1(boolean isLeft)
    {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.1f, -20, -0.1f),
                        new Vector3f(0.1f, 8.5f, 0.1f)
                ),
                new Vector3f(20 * sign, 0, 16),
                new Quaternionf()
        );
    }
    private static GlowingCuboid getTowerLight2(boolean isLeft)
    {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.1f, -15, -0.1f),
                        new Vector3f(0.1f, 11.5f, 0.1f)
                ),
                new Vector3f(20 * sign, 0, 19),
                new Quaternionf()
        );
    }

    @Override
    protected LightGroupV2 setupLeftLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, -0.01f, 5);
        var left1 = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -10, -0.03f),
                        new Vector3f(0.03f, 800, 0.03f)
                ),
                new Vector3f(15, 2, 30),
                new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
        );
        var left2 = left1.cloneOffset(offset);
        var left3 = left2.cloneOffset(offset);
        var left4 = left3.cloneOffset(offset);

        rotatingLights.put(lightID++, left1);
        rotatingLights.put(lightID++, left2);
        rotatingLights.put(lightID++, left3);
        rotatingLights.put(lightID++, left4);

        var leftRunway = getRunway(true);
        var leftTowerLight1 = getTowerLight1(true);
        var leftTowerLight2 = getTowerLight2(true);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID++, leftTowerLight1);
        staticLights.put(lightID++, leftTowerLight2);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, -0.01f, 5);
        var right1 = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -10, -0.03f),
                        new Vector3f(0.03f, 800, 0.03f)
                ),
                new Vector3f(-15, 2, 30),
                new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
        );
        var right2 = right1.cloneOffset(offset);
        var right3 = right2.cloneOffset(offset);
        var right4 = right3.cloneOffset(offset);

        rotatingLights.put(lightID++, right1);
        rotatingLights.put(lightID++, right2);
        rotatingLights.put(lightID++, right3);
        rotatingLights.put(lightID++, right4);

        var rightRunway = getRunway(false);
        var rightTowerLight1 = getTowerLight1(false);
        var rightTowerLight2 = getTowerLight2(false);

        staticLights.put(lightID++, rightRunway);
        staticLights.put(lightID++, rightTowerLight1);
        staticLights.put(lightID++, rightTowerLight2);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 6; i++)
        {
            float z = 45 + i * 10;
            var bottomLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(2f, -2, z),
                    new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, bottomLeftLaser);

            var bottomRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(-2f, -2, z),
                    new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, bottomRightLaser);
        }

        return new StaticLightsGroup(lights);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 6; i++)
        {
            float z = 45 + i * 10;
            var topLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(2f, -2, z),
                    new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, topLeftLaser);

            var topRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(-2f, -2, z),
                    new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 60 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, topRightLaser);
        }

        var chevronLeft = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.06f, -1.5f, -0.06f),
                        new Vector3f(0.06f, 0.03f, 0.06f)
                ),
                new Vector3f(),
                new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
        );
        lights.put(lightID++, chevronLeft);

        var chevronRight = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.06f, -1.5f, -0.06f),
                        new Vector3f(0.06f, 0.03f, 0.06f)
                ),
                new Vector3f(),
                new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
        );
        lights.put(lightID++, chevronRight);

        return new StaticLightsGroup(lights);
    }

    @Override
    protected LightGroupV2 setupRingLights() {
        // TODO
        return new RingLightGroup();
    }
}
