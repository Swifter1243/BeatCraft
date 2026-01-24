package com.beatcraft.client.lightshow.environment.nice;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.environment.EnvironmentV2;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.client.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.spectrogram.SpectrogramTowers;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.environment.NiceRenderer;
import com.beatcraft.client.render.lights.GlowingCuboid;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.HashMap;

public class NiceEnvironment extends EnvironmentV2 {

    private final NiceRenderer renderer = new NiceRenderer();

    private RingLightGroup ringLights;

    private SpectrogramTowers leftSpectrogramTowers;
    private SpectrogramTowers rightSpectrogramTowers;

    @Override
    public String getID() {
        return "NiceEnvironment";
    }

    public NiceEnvironment(BeatmapController map) {
        super(map);
    }

    @Override
    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        super.loadLightshow(difficulty, json);

        var f = new File(difficulty.getInfo().getSongFilename());

        leftSpectrogramTowers = new SpectrogramTowers(
            mapController,
            new Vector3f(13.5f, -405f, -80.5f),
            new Quaternionf(),
            new Vector3f(0, 0, 2),
            127,
            f,
            SpectrogramTowers.TowerStyle.Cuboid,
            true
        );
        rightSpectrogramTowers = leftSpectrogramTowers.copyTo(
            new Vector3f(-13.5f, -405f, -80.5f),
            new Quaternionf()
        );
        leftSpectrogramTowers.levelModifier = 0.75f;
        rightSpectrogramTowers.levelModifier = 0.75f;
        leftSpectrogramTowers.levelEasing = Easing::easeOutExpo;
        rightSpectrogramTowers.levelEasing = Easing::easeOutExpo;
        leftSpectrogramTowers.baseHeight = 400;
        rightSpectrogramTowers.baseHeight = 400;

    }

    private static GlowingCuboid getRunway(BeatmapController map, boolean isLeft, boolean isCenter) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
            new Hitbox(
                new Vector3f(-0.03f, -0.03f, isCenter ? 0 : -500),
                new Vector3f(0.03f, 0.03f, 500)
            ),
            (isCenter ? new Vector3f(2 * sign, 0, 8) :
            new Vector3f(16f * sign, 1.5f, 8)
            ),
            new Quaternionf()
        );
    }
    private static GlowingCuboid getTowerLight1(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
            new Hitbox(
                new Vector3f(-0.1f, -20, -0.1f),
                new Vector3f(0.1f, 8.5f, 0.1f)
            ),
            new Vector3f(20 * sign, 0, 16),
            new Quaternionf()
        );
    }

    private static GlowingCuboid getTowerLight2(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
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
    private static GlowingCuboid getTowerLight3(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
            new Hitbox(
                new Vector3f(-0.1f, -20, -0.1f),
                new Vector3f(0.1f, 8.5f, 0.1f)
            ),
            new Vector3f(10 * sign, -6, -17),
            new Quaternionf()
        );
    }

    private static GlowingCuboid getTowerLight4(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
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
    private static GlowingCuboid[] getSkyLasers(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        // [back, front]
        return new GlowingCuboid[]{
            new GlowingCuboid(
                map,
                new Hitbox(
                    new Vector3f(-0.2f, 0, -0.2f),
                    new Vector3f(0.2f, 350, 0.2f)
                ),
                new Vector3f(12.5f * sign, 24, -13.5f),
                new Quaternionf()
            ),
            new GlowingCuboid(
                map,
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
    private static GlowingCuboid getSlantedLaser(BeatmapController map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
            new Hitbox(
                new Vector3f(-0.2f, 0, -0.2f),
                new Vector3f(0.2f, 102, 0.2f)
            ),
            new Vector3f(3.5f * sign, -64, -0.5f),
            new Quaternionf().rotationX(-19.8f * Mth.DEG_TO_RAD)
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
                mapController,
                new Hitbox(
                    new Vector3f(-300, -0.03f, -0.03f),
                    new Vector3f(300, 0.03f, 0.03f)
                ),
                new Vector3f(0, -10, 45),
                new Quaternionf()//.rotationZ(angle * MathHelper.RADIANS_PER_DEGREE)
            );

            light.setRotation2(
                new Quaternionf().rotationZ(angle * Mth.DEG_TO_RAD)
            );

            rotatingLights.put(lightID++, light);

        }


        var leftRunway = getRunway(mapController, true, false);
        var leftTowerLight1 = getTowerLight1(mapController, true);
        var leftTowerLight2 = getTowerLight2(mapController, true);

        var leftTowerLight3 = getTowerLight3(mapController, true);
        var leftTowerLight4 = getTowerLight4(mapController, true);

        var skyLasers = getSkyLasers(mapController, true);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID++, leftTowerLight1);
        staticLights.put(lightID++, leftTowerLight2);

        staticLights.put(lightID++, leftTowerLight3);
        staticLights.put(lightID++, leftTowerLight4);
        staticLights.put(lightID++, skyLasers[0]);
        staticLights.put(lightID++, skyLasers[1]);

        return new RotatingLightsGroup(mapController, rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {

        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        // TODO: tilted top lasers


        for (var angle : angles) {

            var light = new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-300, -0.03f, -0.03f),
                    new Vector3f(300, 0.03f, 0.03f)
                ),
                new Vector3f(0, 15, 45),
                new Quaternionf()
            );

            light.setRotation2(
                new Quaternionf().rotationZ(angle * Mth.DEG_TO_RAD)
            );

            rotatingLights.put(lightID++, light);

        }

        var rightRunway = getRunway(mapController, false, false);
        var rightTowerLight1 = getTowerLight1(mapController, false);
        var rightTowerLight2 = getTowerLight2(mapController, false);

        var rightTowerLight3 = getTowerLight3(mapController, false);
        var rightTowerLight4 = getTowerLight4(mapController, false);

        var skyLasers = getSkyLasers(mapController, false);

        staticLights.put(lightID++, rightRunway);
        staticLights.put(lightID++, rightTowerLight1);
        staticLights.put(lightID++, rightTowerLight2);

        staticLights.put(lightID++, rightTowerLight3);
        staticLights.put(lightID++, rightTowerLight4);
        staticLights.put(lightID++, skyLasers[0]);
        staticLights.put(lightID++, skyLasers[1]);

        return new RotatingLightsGroup(mapController, rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupBackLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 4; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var bottomLeftLaser = new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-0.05f, -150, -0.05f),
                    new Vector3f(0.05f, 0, 0.05f)
                ),
                new Vector3f(5f, -2, z),
                new Quaternionf().rotationZ(15 * Mth.DEG_TO_RAD)
            );
            lights.put(lightID++, bottomLeftLaser);

            var bottomRightLaser = new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-0.05f, -150, -0.05f),
                    new Vector3f(0.05f, 0, 0.05f)
                ),
                new Vector3f(-5f, -2, z),
                new Quaternionf().rotationZ(-15 * Mth.DEG_TO_RAD)
            );
            lights.put(lightID++, bottomRightLaser);
        }

        return new StaticLightsGroup(mapController, lights);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        HashMap<Integer, LightObject> lights = new HashMap<>();
        int lightID = 1;

        for (int i = 0; i < 4; i++) {
            float z = MIDDLE_LIGHT_Z + i * 10;
            var topLeftLaser = new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-0.05f, 0, -0.05f),
                    new Vector3f(0.05f, 200, 0.05f)
                ),
                new Vector3f(5f, -2, z),
                new Quaternionf().rotationZ(15 * Mth.DEG_TO_RAD)
            );
            lights.put(lightID++, topLeftLaser);

            var topRightLaser = new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-0.05f, 0, -0.05f),
                    new Vector3f(0.05f, 200, 0.05f)
                ),
                new Vector3f(-5f, -2, z),
                new Quaternionf().rotationZ(-15 * Mth.DEG_TO_RAD)
            );
            lights.put(lightID++, topRightLaser);
        }

        var rRunway = getRunway(mapController, false, true);
        var lRunway = getRunway(mapController, true, true);

        lights.put(lightID++, lRunway);
        lights.put(lightID++, rRunway);

        var leftSlanted = getSlantedLaser(mapController, true);
        var rightSlanted = getSlantedLaser(mapController, false);
        lights.put(lightID++, leftSlanted);
        lights.put(lightID++, rightSlanted);

        var chevronLeft = new GlowingCuboid(
            mapController,
            new Hitbox(
                new Vector3f(-0.1f, -1.6f, -0.1f),
                new Vector3f(0.1f, 0.035f, 0.1f)
            ),
            new Vector3f(0, 4, 75),
            new Quaternionf().rotationZ(55 * Mth.DEG_TO_RAD)
        );
        lights.put(lightID++, chevronLeft);

        var chevronRight = new GlowingCuboid(
            mapController,
            new Hitbox(
                new Vector3f(-0.1f, -1.6f, -0.1f),
                new Vector3f(0.1f, 0.035f, 0.1f)
            ),
            new Vector3f(0, 4, 75),
            new Quaternionf().rotationZ(-55 * Mth.DEG_TO_RAD)
        );
        lights.put(lightID++, chevronRight);

        return new StaticLightsGroup(mapController, lights);
    }


    private static final float ringRadius = 27;
    private static final float lightLength = 6;
    private static final float lightSize = 0.2f;

    @Override
    protected LightGroupV2 setupRingLights() {
        ringLights = new RingLightGroup(
            mapController,
            (ignored) -> new InnerRing(mapController),
            (mod) -> new OuterRing(mapController, mod),
            () -> new GlowingCuboid(
                mapController,
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
    public void render(PoseStack matrices, Camera camera, float alpha) {
        super.render(matrices, camera, alpha);

        var t = mapController.currentSeconds;

        leftSpectrogramTowers.render(t);
        rightSpectrogramTowers.render(t);

        renderer.renderEnv(matrices, camera, mapController, alpha);
    }

    @Override
    public NiceEnvironment reset() {
        super.reset();
        ringLights.reset();
        return this;
    }
}
