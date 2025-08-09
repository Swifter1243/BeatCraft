package com.beatcraft.client.lightshow.environment.origins;

import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.lightshow.environment.EnvironmentV2;
import com.beatcraft.client.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.client.lightshow.environment.lightgroup.RingLightGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.RotatingLightsGroup;
import com.beatcraft.client.lightshow.environment.lightgroup.StaticLightsGroup;
import com.beatcraft.client.lightshow.environment.thefirst.OuterRing;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.spectrogram.SpectrogramTowers;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.render.lights.GlowingCuboid;
import com.beatcraft.client.render.lights.ParticleCloudLight;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.io.File;
import java.util.HashMap;

public class OriginsEnvironment extends EnvironmentV2 {

    @Override
    public String getID() {
        return "OriginsEnvironment";
    }

    private RingLightGroup ringLights;

    private SpectrogramTowers leftSpectrogramTowers;
    private SpectrogramTowers rightSpectrogramTowers;

    public OriginsEnvironment(BeatmapPlayer map) {
        super(map);
    }

    @Override
    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        super.loadLightshow(difficulty, json);

        var f = new File(difficulty.getInfo().getSongFilename());

        leftSpectrogramTowers = new SpectrogramTowers(
            mapController,
            new Vector3f(114f, -71f, -80.5f),
            new Quaternionf().rotateZ(45 * Mth.DEG_TO_RAD).rotateLocalY(-12.5f * Mth.DEG_TO_RAD),
            new Vector3f(0, 0, 2),
            127,
            f,
            SpectrogramTowers.TowerStyle.Cuboid,
            true
        );
        rightSpectrogramTowers = leftSpectrogramTowers.copyTo(
            new Vector3f(-114f, -71f, -80.5f),
            new Quaternionf().rotateZ(-45 * Mth.DEG_TO_RAD).rotateLocalY(12.5f * Mth.DEG_TO_RAD)
        );
        leftSpectrogramTowers.levelModifier = 0.75f;
        rightSpectrogramTowers.levelModifier = 0.75f;
        leftSpectrogramTowers.baseHeight = 100;
        rightSpectrogramTowers.baseHeight = 100;
        leftSpectrogramTowers.levelEasing = Easing::easeOutExpo;
        rightSpectrogramTowers.levelEasing = Easing::easeOutExpo;


    }


    private static GlowingCuboid getRunway(BeatmapPlayer map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;

        return new GlowingCuboid(
            map,
            new Hitbox(
                new Vector3f(-0.03f, -0.03f, 0),
                new Vector3f(0.03f, 0.03f, 500)
            ),
            new Vector3f(2.5f * sign, 0, 8),
            new Quaternionf()
        );
    }


    private static ParticleCloudLight getParticles(BeatmapPlayer map, boolean isLeft) {
        int sign = isLeft ? 1 : -1;
        var rotation = new Quaternionf().rotationY(12.5f * Mth.DEG_TO_RAD * -sign);

        var cl = new ParticleCloudLight(
            map,
            new Vector3f(12 * sign, -5, 20),
            rotation,
            new Hitbox(
                new Vector3f(-4, -5, -35),
                new Vector3f(4, 3, 40)
            ),
            0.01f,
            4, 4,
            new Vector3f(0, 0, 2f)
        );

        cl.addParticleSpawners(new OriginsParticleSpawner(map));

        return cl;

    }


    private static final float ROTATING_LIGHT_X = 20.5f;
    private static final float ROTATING_LIGHT_Z = 35;
    private static final float MIDDLE_LIGHT_Z = 60;


    @Override
    protected LightGroupV2 setupLeftLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, 0, 2.25f);
        var left1 = new GlowingCuboid(
            mapController,
            new Hitbox(
                new Vector3f(-0.03f, 0, -0.03f),
                new Vector3f(0.03f, 800, 0.03f)
            ),
            new Vector3f(ROTATING_LIGHT_X, -5f, ROTATING_LIGHT_Z),
            new Quaternionf().rotationZ(55 * Mth.DEG_TO_RAD)
        );
        var left2 = left1.cloneOffset(offset);
        var left3 = left2.cloneOffset(offset);
        var left4 = left3.cloneOffset(offset);
        var left5 = left4.cloneOffset(offset);

        rotatingLights.put(lightID++, left1);
        rotatingLights.put(lightID++, left2);
        rotatingLights.put(lightID++, left3);
        rotatingLights.put(lightID++, left4);
        rotatingLights.put(lightID++, left5);

        var leftRunway = getRunway(mapController, true);

        var leftParticles = getParticles(mapController, true);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID, leftParticles);

        return new RotatingLightsGroup(mapController, rotatingLights, staticLights);
    }

    @Override
    protected LightGroupV2 setupRightLasers() {
        HashMap<Integer, LightObject> rotatingLights = new HashMap<>();
        HashMap<Integer, LightObject> staticLights = new HashMap<>();
        int lightID = 1;

        var offset = new Vector3f(0, 0, 2.25f);
        var left1 = new GlowingCuboid(
            mapController,
            new Hitbox(
                new Vector3f(-0.03f, 0, -0.03f),
                new Vector3f(0.03f, 800, 0.03f)
            ),
            new Vector3f(-ROTATING_LIGHT_X, -5f, ROTATING_LIGHT_Z),
            new Quaternionf().rotationZ(-55 * Mth.DEG_TO_RAD)
        );
        var left2 = left1.cloneOffset(offset);
        var left3 = left2.cloneOffset(offset);
        var left4 = left3.cloneOffset(offset);
        var left5 = left4.cloneOffset(offset);

        rotatingLights.put(lightID++, left1);
        rotatingLights.put(lightID++, left2);
        rotatingLights.put(lightID++, left3);
        rotatingLights.put(lightID++, left4);
        rotatingLights.put(lightID++, left5);

        var leftRunway = getRunway(mapController, false);

        var leftParticles = getParticles(mapController, false);

        staticLights.put(lightID++, leftRunway);
        staticLights.put(lightID, leftParticles);

        return new RotatingLightsGroup(mapController, rotatingLights, staticLights);
    }

    private static final Pair<Vector3f, Quaternionf>[] bottomLights = new Pair[]{
        new Pair<>(new Vector3f( 1.5f, -0.25f, 20), new Quaternionf().rotationZ(-12.5f * Mth.DEG_TO_RAD)),
        new Pair<>(new Vector3f(-1.5f, -0.25f, 20), new Quaternionf().rotationZ(12.5f * Mth.DEG_TO_RAD)),
        new Pair<>(new Vector3f( 1.5f, -0.25f, 28), new Quaternionf().rotationZ(-12.5f * Mth.DEG_TO_RAD)),
        new Pair<>(new Vector3f(-1.5f, -0.25f, 28), new Quaternionf().rotationZ(12.5f * Mth.DEG_TO_RAD))
    };

    @Override
    protected LightGroupV2 setupBackLasers() {
        var lights = new HashMap<Integer, LightObject>();

        int lightID = 1;

        for (var pair : bottomLights) {
            lights.put(lightID++, new GlowingCuboid(
                mapController,
                new Hitbox(
                    new Vector3f(-0.03f, -100, -0.03f),
                    new Vector3f(0.03f, 0, 0.03f)
                ),
                pair.getA(), pair.getB()
            ));
        }

        return new StaticLightsGroup(mapController, lights);
    }

    @Override
    protected LightGroupV2 setupCenterLasers() {
        var lightID = 1;
        var lights = new HashMap<Integer, LightObject>();

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



    private static final float ringRadius = 30;
    private static final float lightLength = 12;
    private static final float lightSize = 0.2f;

    @Override
    protected LightGroupV2 setupRingLights() {
        ringLights = new RingLightGroup(
            mapController,
            (b) -> null,
            (m) -> OuterRing.getLightsOnly(mapController, m),
            () -> new GlowingCuboid(
                mapController,
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
    public void render(PoseStack matrices, Camera camera) {
        super.render(matrices, camera);

        var t = mapController.currentSeconds;

        leftSpectrogramTowers.render(t);
        rightSpectrogramTowers.render(t);

    }

    @Override
    public OriginsEnvironment reset() {
        super.reset();
        ringLights.reset();
        return this;
    }
}
