package com.beatcraft.lightshow.environment.triangle;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.lightshow.environment.EnvironmentV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.lightshow.spectrogram.SpectrogramTowers;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.lights.GlowingCuboid;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.HashMap;

public class TriangleEnvironment extends EnvironmentV2 {


    private RingLightGroup ringLights;

    private SpectrogramTowers leftSpectrogramTowers;
    private SpectrogramTowers rightSpectrogramTowers;

    @Override
    public String getID() {
        return "TriangleEnvironment";
    }

    @Override
    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        super.loadLightshow(difficulty, json);

        var f = new File(difficulty.getInfo().getSongFilename());

        leftSpectrogramTowers = new SpectrogramTowers(
            new Vector3f(13.5f, -5f, -80.5f),
            new Quaternionf(),
            new Vector3f(0, 0, 2),
            127,
            f,
            SpectrogramTowers.TowerStyle.Cuboid,
            true
        );
        rightSpectrogramTowers = leftSpectrogramTowers.copyTo(
            new Vector3f(-13.5f, -5f, -80.5f),
            new Quaternionf()
        );
        leftSpectrogramTowers.levelModifier = 0.75f;
        rightSpectrogramTowers.levelModifier = 0.75f;
        leftSpectrogramTowers.levelEasing = Easing::easeOutExpo;
        rightSpectrogramTowers.levelEasing = Easing::easeOutExpo;


    }

    private static GlowingCuboid getRunway(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -0.03f, 0),
                        new Vector3f(0.03f, 0.03f, 500)
                ),
                new Vector3f(2f * sign, 0, 8),
                new Quaternionf()
        );
    }
    private static GlowingCuboid getTowerLight1(boolean isLeft) {
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

    private static GlowingCuboid getTowerLight2(boolean isLeft) {
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



    // sky lasers:
    // @  22.5 30  19.5
    // @ -22.5 30  19.5
    // @ -12.5 24 -13.5
    // @  12.5 24 -13.5
    private static GlowingCuboid getSkyLasers(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.2f, 0, -0.2f),
                new Vector3f(0.2f, 350, 0.2f)
            ),
            new Vector3f(22.5f * sign, 30, 19.5f),
            new Quaternionf()
        );

    }

    // slanted lasers:
    // @  3.5 -64 -0.5 ->  3.5 32 -34.5 // distance: 102, angle: 19.8 degrees
    // @ -x ^^^
    private static GlowingCuboid getSlantedLaser(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.2f, 0, -0.2f),
                new Vector3f(0.2f, 102, 0.2f)
            ),
            new Vector3f(3.5f * sign, -64, -0.5f),
            new Quaternionf().rotationX(-19.8f * MathHelper.RADIANS_PER_DEGREE)
        );
    }

    private static final float ROTATING_LIGHT_X = 25;
    private static final float ROTATING_LIGHT_Y = -6;
    private static final float ROTATING_LIGHT_Z = 47.5f;
    private static final float MIDDLE_LIGHT_Z = 27.5f;

    @Override
    protected LightGroupV2 setupLeftLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, 0, 0.5f);
        var left1 = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -10, -0.03f),
                        new Vector3f(0.03f, 800, 0.03f)
                ),
                new Vector3f(ROTATING_LIGHT_X, ROTATING_LIGHT_Y, ROTATING_LIGHT_Z),
                new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
        );
        var left2 = left1.cloneOffset(offset);
        var left3 = left2.cloneOffset(offset);
        var left4 = left3.cloneOffset(offset);
        var left5 = left4.cloneOffset(offset);
        var left6 = left5.cloneOffset(offset);
        var left7 = left6.cloneOffset(offset);

        rotatingLights.put(lightID++, left1);
        rotatingLights.put(lightID++, left2);
        rotatingLights.put(lightID++, left3);
        rotatingLights.put(lightID++, left4);
        rotatingLights.put(lightID++, left5);
        rotatingLights.put(lightID++, left6);
        rotatingLights.put(lightID++, left7);



        var leftRunway = getRunway(true);
        var leftTowerLight1 = getTowerLight1(true);
        var leftTowerLight2 = getTowerLight2(true);

        var skyLaser = getSkyLasers(true);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID++, leftTowerLight1);
        staticLights.put(lightID++, leftTowerLight2);

        staticLights.put(lightID++, skyLaser);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {

        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, 0, 0.5f);
        var right1 = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -10, -0.03f),
                        new Vector3f(0.03f, 800, 0.03f)
                ),
                new Vector3f(-ROTATING_LIGHT_X, ROTATING_LIGHT_Y, ROTATING_LIGHT_Z),
                new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
        );
        var right2 = right1.cloneOffset(offset);
        var right3 = right2.cloneOffset(offset);
        var right4 = right3.cloneOffset(offset);
        var right5 = right4.cloneOffset(offset);
        var right6 = right5.cloneOffset(offset);
        var right7 = right6.cloneOffset(offset);

        rotatingLights.put(lightID++, right1);
        rotatingLights.put(lightID++, right2);
        rotatingLights.put(lightID++, right3);
        rotatingLights.put(lightID++, right4);
        rotatingLights.put(lightID++, right5);
        rotatingLights.put(lightID++, right6);
        rotatingLights.put(lightID++, right7);

        var rightRunway = getRunway(false);
        var rightTowerLight1 = getTowerLight1(false);
        var rightTowerLight2 = getTowerLight2(false);

        var skyLaser = getSkyLasers(false);

        staticLights.put(lightID++, rightRunway);
        staticLights.put(lightID++, rightTowerLight1);
        staticLights.put(lightID++, rightTowerLight2);

        staticLights.put(lightID++, skyLaser);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 5; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var bottomLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(2f, -2, z),
                    new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 30 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, bottomLeftLaser);

            var bottomRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(-2f, -2, z),
                    new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 30 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, bottomRightLaser);
        }

        return new StaticLightsGroup(lights);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 5; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var topLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(3f, -1, z),
                    new Quaternionf().rotationYXZ(-20 * MathHelper.RADIANS_PER_DEGREE, 30 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, topLeftLaser);

            var topRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(-3f, -1, z),
                    new Quaternionf().rotationYXZ(20 * MathHelper.RADIANS_PER_DEGREE, 30 * MathHelper.RADIANS_PER_DEGREE, 0)
            );
            lights.put(lightID++, topRightLaser);
        }

        // TODO: order these slanted lasers correctly
        var leftSlanted = getSlantedLaser(true);
        var rightSlanted = getSlantedLaser(false);
        lights.put(lightID++, leftSlanted);
        lights.put(lightID++, rightSlanted);

        return new StaticLightsGroup(lights);
    }


    private static final float ringRadius = 27;
    private static final float lightLength = 6;
    private static final float lightSize = 0.2f;

    @Override
    protected LightGroupV2 setupRingLights() {
        ringLights = new RingLightGroup(
            InnerRing::getInstance,
            OuterRing::new,
            () -> new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-lightLength/2, -lightSize, -lightSize),
                    new Vector3f(lightLength/2, lightSize, lightSize)
                ),
                new Vector3f(0, ringRadius-(lightSize+0.01f), lightSize),
                new Quaternionf()
            )
        );
        return ringLights;
    }


    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);

        var t = BeatmapPlayer.getCurrentSeconds();

        leftSpectrogramTowers.render(t);
        rightSpectrogramTowers.render(t);

    }

    @Override
    public TriangleEnvironment reset() {
        super.reset();
        ringLights.reset();
        return this;
    }
}
