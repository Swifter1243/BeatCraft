package com.beatcraft.lightshow.environment.nice;

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

public class NiceEnvironment extends EnvironmentV2 {


    private RingLightGroup ringLights;

    private SpectrogramTowers leftSpectrogramTowers;
    private SpectrogramTowers rightSpectrogramTowers;

    @Override
    public String getID() {
        return "NiceEnvironment";
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

    private static GlowingCuboid getRunway(boolean isLeft, boolean isCenter) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.03f, -0.03f, isCenter ? 0 : -500),
                        new Vector3f(0.03f, 0.03f, 500)
                ),
                (isCenter ? new Vector3f(2 * sign, 0, 9) :
                new Vector3f(16f * sign, 1.5f, 8)
                ),
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


    // additional side lasers:
    // @ 10 5.5 -14
    // @ 10 2.5 -17
    private static GlowingCuboid getTowerLight3(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.1f, -20, -0.1f),
                new Vector3f(0.1f, 8.5f, 0.1f)
            ),
            new Vector3f(10 * sign, -6, -17),
            new Quaternionf()
        );
    }

    private static GlowingCuboid getTowerLight4(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            new Hitbox(
                new Vector3f(-0.1f, -15, -0.1f),
                new Vector3f(0.1f, 11.5f, 0.1f)
            ),
            new Vector3f(10 * sign, -6, -14),
            new Quaternionf()
        );
    }


    // sky lasers:
    // @  22.5 30  19.5
    // @ -22.5 30  19.5
    // @ -12.5 24 -13.5
    // @  12.5 24 -13.5
    private static GlowingCuboid[] getSkyLasers(boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        // [back, front]
        return new GlowingCuboid[]{
            new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-0.2f, 0, -0.2f),
                    new Vector3f(0.2f, 350, 0.2f)
                ),
                new Vector3f(12.5f * sign, 24, -13.5f),
                new Quaternionf()
            ),
            new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-0.2f, 0, -0.2f),
                    new Vector3f(0.2f, 350, 0.2f)
                ),
                new Vector3f(22.5f * sign, 30, 19.5f),
                new Quaternionf()
            )
        };

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

    private static final float ROTATING_LIGHT_X = 9;
    private static final float ROTATING_LIGHT_Z = 35;
    private static final float MIDDLE_LIGHT_Z = 60;

    private static final float[] angles = new float[]{
        15f * 1.5f,
        15f * 0.5f,
        15f * -0.5f,
        15f * -1.5f,
    };

    @Override
    protected LightGroupV2 setupLeftLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        // TODO: tilted bottom lasers

        for (var angle : angles) {

            var light = new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-300, -0.03f, -0.03f),
                    new Vector3f(300, 0.03f, 0.03f)
                ),
                new Vector3f(0, -10, 45),
                new Quaternionf()//.rotationZ(angle * MathHelper.RADIANS_PER_DEGREE)
            );

            light.setRotation2(
                new Quaternionf().rotationZ(angle * MathHelper.RADIANS_PER_DEGREE)
            );

            rotatingLights.put(lightID++, light);

        }


        var leftRunway = getRunway(true, false);
        var leftTowerLight1 = getTowerLight1(true);
        var leftTowerLight2 = getTowerLight2(true);

        var leftTowerLight3 = getTowerLight3(true);
        var leftTowerLight4 = getTowerLight4(true);

        var skyLasers = getSkyLasers(true);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID++, leftTowerLight1);
        staticLights.put(lightID++, leftTowerLight2);

        staticLights.put(lightID++, leftTowerLight3);
        staticLights.put(lightID++, leftTowerLight4);
        staticLights.put(lightID++, skyLasers[0]);
        staticLights.put(lightID++, skyLasers[1]);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {

        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        // TODO: tilted top lasers


        for (var angle : angles) {

            var light = new GlowingCuboid(
                new Hitbox(
                    new Vector3f(-300, -0.03f, -0.03f),
                    new Vector3f(300, 0.03f, 0.03f)
                ),
                new Vector3f(0, 15, 45),
                new Quaternionf()
            );

            light.setRotation2(
                new Quaternionf().rotationZ(angle * MathHelper.RADIANS_PER_DEGREE)
            );

            rotatingLights.put(lightID++, light);

        }

        var rightRunway = getRunway(false, false);
        var rightTowerLight1 = getTowerLight1(false);
        var rightTowerLight2 = getTowerLight2(false);

        var rightTowerLight3 = getTowerLight3(false);
        var rightTowerLight4 = getTowerLight4(false);

        var skyLasers = getSkyLasers(false);

        staticLights.put(lightID++, rightRunway);
        staticLights.put(lightID++, rightTowerLight1);
        staticLights.put(lightID++, rightTowerLight2);

        staticLights.put(lightID++, rightTowerLight3);
        staticLights.put(lightID++, rightTowerLight4);
        staticLights.put(lightID++, skyLasers[0]);
        staticLights.put(lightID++, skyLasers[1]);

        return new RotatingLightsGroup(rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 4; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var bottomLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(5f, -2, z),
                    new Quaternionf().rotationZ(15 * MathHelper.RADIANS_PER_DEGREE)
            );
            lights.put(lightID++, bottomLeftLaser);

            var bottomRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, -150, -0.05f),
                            new Vector3f(0.05f, 0, 0.05f)
                    ),
                    new Vector3f(-5f, -2, z),
                    new Quaternionf().rotationZ(-15 * MathHelper.RADIANS_PER_DEGREE)
            );
            lights.put(lightID++, bottomRightLaser);
        }

        return new StaticLightsGroup(lights);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 4; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var topLeftLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(5f, -2, z),
                    new Quaternionf().rotationZ(15 * MathHelper.RADIANS_PER_DEGREE)
            );
            lights.put(lightID++, topLeftLaser);

            var topRightLaser = new GlowingCuboid(
                    new Hitbox(
                            new Vector3f(-0.05f, 0, -0.05f),
                            new Vector3f(0.05f, 200, 0.05f)
                    ),
                    new Vector3f(-5f, -2, z),
                    new Quaternionf().rotationZ(-15 * MathHelper.RADIANS_PER_DEGREE)
            );
            lights.put(lightID++, topRightLaser);
        }

        var rRunway = getRunway(false, true);
        var lRunway = getRunway(true, true);

        lights.put(lightID++, lRunway);
        lights.put(lightID++, rRunway);

        var leftSlanted = getSlantedLaser(true);
        var rightSlanted = getSlantedLaser(false);
        lights.put(lightID++, leftSlanted);
        lights.put(lightID++, rightSlanted);

        var chevronLeft = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.1f, -1.6f, -0.1f),
                        new Vector3f(0.1f, 0.035f, 0.1f)
                ),
                new Vector3f(0, 4, 75),
                new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
        );
        lights.put(lightID++, chevronLeft);

        var chevronRight = new GlowingCuboid(
                new Hitbox(
                        new Vector3f(-0.1f, -1.6f, -0.1f),
                        new Vector3f(0.1f, 0.035f, 0.1f)
                ),
                new Vector3f(0, 4, 75),
                new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
        );
        lights.put(lightID++, chevronRight);

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
            ),
            4.25f, 10, 30f, 1.25f
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
    public NiceEnvironment reset() {
        super.reset();
        ringLights.reset();
        return this;
    }
}
