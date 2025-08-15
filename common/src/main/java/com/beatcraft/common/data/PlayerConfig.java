package com.beatcraft.common.data;


import com.beatcraft.Beatcraft;
import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PlayerConfig {

    public static final int FORMAT = 1;

    public class AudioSettings {
        public float volume = 1.0f;
        public float ambientVolumeScale = 0.8f;
        public int latency = 0;
        public boolean overrideLatency = false;

        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("volume", volume);
            json.addProperty("ambient_volume_scale", ambientVolumeScale);
            json.addProperty("latency", latency);
            json.addProperty("override_latency", overrideLatency);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            volume = JsonUtil.getOrDefault(json, "audio.volume", JsonElement::getAsFloat, volume);
            ambientVolumeScale = JsonUtil.getOrDefault(json, "audio.ambient_volume_scale", JsonElement::getAsFloat, ambientVolumeScale);
            latency = JsonUtil.getOrDefault(json, "audio.latency", JsonElement::getAsInt, latency);
            overrideLatency = JsonUtil.getOrDefault(json, "audio.override_latency", JsonElement::getAsBoolean, overrideLatency);
        }

        public void parse$v1(JsonObject json) {
            volume = JsonUtil.getOrDefault(json, "volume", JsonElement::getAsFloat, volume);
            ambientVolumeScale = JsonUtil.getOrDefault(json, "ambient_volume_scale", JsonElement::getAsFloat, ambientVolumeScale);
            latency = JsonUtil.getOrDefault(json, "latency", JsonElement::getAsInt, latency);
            overrideLatency = JsonUtil.getOrDefault(json, "override_latency", JsonElement::getAsBoolean, overrideLatency);
        }
    }

    public class QualitySettings {
        public boolean doBloomfog = true;
        public boolean doBloom = true;
        public boolean doMirror = true;
        public boolean skyFog = true;
        public boolean smokeGraphics = true;
        public boolean burnMarkTrails = true;
        public boolean sparkParticles = true;

        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("bloomfog", doBloomfog);
            json.addProperty("bloom", doBloom);
            json.addProperty("mirror", doMirror);
            json.addProperty("sky_fog", skyFog);
            json.addProperty("smoke_graphics", smokeGraphics);
            json.addProperty("burn_mark_trails", burnMarkTrails);
            json.addProperty("spark_particles", sparkParticles);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            doBloomfog = JsonUtil.getOrDefault(json, "quality.bloomfog", JsonElement::getAsBoolean, doBloomfog);
            doBloom = JsonUtil.getOrDefault(json, "quality.bloom", JsonElement::getAsBoolean, doBloom);
            doMirror = JsonUtil.getOrDefault(json, "quality.mirror", JsonElement::getAsBoolean, doMirror);
            skyFog = JsonUtil.getOrDefault(json, "quality.sky_fog", JsonElement::getAsBoolean, skyFog);
            smokeGraphics = JsonUtil.getOrDefault(json, "quality.smoke_graphics", JsonElement::getAsBoolean, smokeGraphics);
            burnMarkTrails = JsonUtil.getOrDefault(json, "quality.burn_mark_trails", JsonElement::getAsBoolean, burnMarkTrails);
            sparkParticles = JsonUtil.getOrDefault(json, "quality.spark_particles", JsonElement::getAsBoolean, sparkParticles);
        }

        public void parse$v1(JsonObject json) {
            doBloomfog = JsonUtil.getOrDefault(json, "bloomfog", JsonElement::getAsBoolean, doBloomfog);
            doBloom = JsonUtil.getOrDefault(json, "bloom", JsonElement::getAsBoolean, doBloom);
            doMirror = JsonUtil.getOrDefault(json, "mirror", JsonElement::getAsBoolean, doMirror);
            skyFog = JsonUtil.getOrDefault(json, "sky_fog", JsonElement::getAsBoolean, skyFog);
            smokeGraphics = JsonUtil.getOrDefault(json, "smoke_graphics", JsonElement::getAsBoolean, smokeGraphics);
            burnMarkTrails = JsonUtil.getOrDefault(json, "burn_mark_trails", JsonElement::getAsBoolean, burnMarkTrails);
            sparkParticles = JsonUtil.getOrDefault(json, "spark_particles", JsonElement::getAsBoolean, sparkParticles);
        }
    }

    public class ControllerSettings {
        private static final ControllerProfile DEFAULT_CONTROLLER_PROFILE = new ControllerProfile();
        public int selectedProfile = -1;
        public final ArrayList<ControllerProfile> profiles = new ArrayList<>();

        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("selected_profile", selectedProfile);

            var arr = new JsonArray();

            for (var profile : profiles) {
                profile.writeJson(arr);
            }

            json.add("profiles", arr);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            selectedProfile = JsonUtil.getOrDefault(json, "controller.selectedProfile.index", JsonElement::getAsInt, selectedProfile);
            parseProfiles$v1(json.getAsJsonArray("controller.profiles"));
        }

        public void parse$v1(JsonObject json) {
            selectedProfile = JsonUtil.getOrDefault(json, "selected_profile", JsonElement::getAsInt, selectedProfile);
            parseProfiles$v1(json.getAsJsonArray("profiles"));
        }

        private void parseProfiles$v1(JsonArray profiles) {
            for (var data : profiles) {
                var profile = new ControllerProfile(data.getAsJsonObject());
                this.profiles.add(profile);
            }
        }

    }

    public enum HealthStyle {
        Classic,
        Hearts
    }

    public class Preferences {
        public boolean reducedDebris = false;
        public int trailIntensity = 30;
        public HealthStyle healthStyle = HealthStyle.Hearts;
        public String selectedSaber = "#builtin:default";

        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("reduced_debris", reducedDebris);
            json.addProperty("trail_intensity", trailIntensity);
            json.addProperty("health_style", healthStyle.ordinal());
            json.addProperty("selected_saber", selectedSaber);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            reducedDebris = JsonUtil.getOrDefault(json, "option.reduced_debris", JsonElement::getAsBoolean, reducedDebris);
            trailIntensity = JsonUtil.getOrDefault(json, "option.trail_intensity", JsonElement::getAsInt, trailIntensity);
            healthStyle = HealthStyle.values()[Math.clamp(JsonUtil.getOrDefault(json, "option.health_style", JsonElement::getAsInt, 0), 0, HealthStyle.values().length)];
            // skip selected saber... user will have to re-select
        }

        public void parse$v1(JsonObject json) {
            reducedDebris = JsonUtil.getOrDefault(json, "reduced_debris", JsonElement::getAsBoolean, reducedDebris);
            trailIntensity = JsonUtil.getOrDefault(json, "trail_intensity", JsonElement::getAsInt, trailIntensity);
            healthStyle = HealthStyle.values()[Math.clamp(JsonUtil.getOrDefault(json, "health_style", JsonElement::getAsInt, 0), 0, HealthStyle.values().length)];
            selectedSaber = JsonUtil.getOrDefault(json, "selected_saber", JsonElement::getAsString, selectedSaber);
        }
    }

    public class DebugSettings {

        public class LightshowSettings {
            public boolean renderEvents = false;
            public float beatSpacing = 8;
            public float lookAheadDistance = 16;
            public float lookBehindDistance = 8;

            public JsonObject getJson() {
                var json = new JsonObject();

                json.addProperty("render_events", renderEvents);
                json.addProperty("look_ahead", lookAheadDistance);
                json.addProperty("look_behind", lookBehindDistance);
                json.addProperty("beat_spacing", beatSpacing);

                return json;
            }

            public PlayerConfig parent() {
                return PlayerConfig.this;
            }

            public void parse$old(JsonObject json) {
                renderEvents = JsonUtil.getOrDefault(json, "debug.lightshow.render_events", JsonElement::getAsBoolean, renderEvents);
                lookAheadDistance = JsonUtil.getOrDefault(json, "debug.lightshow.look_ahead", JsonElement::getAsFloat, lookAheadDistance);
                lookBehindDistance = JsonUtil.getOrDefault(json, "debug.lightshow.look_behind", JsonElement::getAsFloat, lookBehindDistance);
                beatSpacing = JsonUtil.getOrDefault(json, "debug.lightshow.beat_spacing", JsonElement::getAsFloat, beatSpacing);
            }

            public void parse$v1(JsonObject json) {
                renderEvents = JsonUtil.getOrDefault(json, "render_events", JsonElement::getAsBoolean, renderEvents);
                lookAheadDistance = JsonUtil.getOrDefault(json, "look_ahead", JsonElement::getAsFloat, lookAheadDistance);
                lookBehindDistance = JsonUtil.getOrDefault(json, "look_behind", JsonElement::getAsFloat, lookBehindDistance);
                beatSpacing = JsonUtil.getOrDefault(json, "beat_spacing", JsonElement::getAsFloat, beatSpacing);
            }

        }

        public class BeatmapSettings {

            public boolean renderArcSplines = false;
            public boolean renderHitboxes = false;
            public boolean renderSaberColliders = false;
            public boolean renderBeatmapPosition = false;


            public JsonObject getJson() {
                var json = new JsonObject();

                json.addProperty("arcs", renderArcSplines);
                json.addProperty("hitboxes", renderHitboxes);
                json.addProperty("sabers", renderSaberColliders);
                json.addProperty("map_position", renderBeatmapPosition);

                return json;
            }

            public PlayerConfig parent() {
                return PlayerConfig.this;
            }

            public void parse$v1(JsonObject json) {
                renderArcSplines = JsonUtil.getOrDefault(json, "arcs", JsonElement::getAsBoolean, renderArcSplines);
                renderHitboxes = JsonUtil.getOrDefault(json, "hitboxes", JsonElement::getAsBoolean, renderHitboxes);
                renderSaberColliders = JsonUtil.getOrDefault(json, "sabers", JsonElement::getAsBoolean, renderSaberColliders);
                renderBeatmapPosition = JsonUtil.getOrDefault(json, "map_position", JsonElement::getAsBoolean, renderBeatmapPosition);

            }
        }


        public final LightshowSettings lightshow = this.new LightshowSettings();
        public final BeatmapSettings beatmap = this.new BeatmapSettings();

        public DebugSettings() {

        }

        public JsonObject getJson() {
            var json = new JsonObject();

            json.add("lightshow", lightshow.getJson());
            json.add("beatmap", beatmap.getJson());

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            lightshow.parse$old(json);
            // no beatmap settings stored in old config
        }

        public void parse$v1(JsonObject json) {
            lightshow.parse$v1(json.getAsJsonObject("lightshow"));
            beatmap.parse$v1(json.getAsJsonObject("beatmap"));
        }

    }

    public final File configFile = new File("./config/beatcraft/config.json");
    public final File configFolder = new File("./config/beatcraft/");

    public final AudioSettings audio = this.new AudioSettings();
    public final QualitySettings quality = this.new QualitySettings();
    public final ControllerSettings controller = this.new ControllerSettings();
    public final Preferences preferences = this.new Preferences();
    public final DebugSettings debug = this.new DebugSettings();

    public PlayerConfig(JsonObject json) {
        this(); // load default values

        if (json.has("version")) {
            var ver = json.get("version").getAsInt();

            if (ver == 1) {
                audio.parse$v1(json.getAsJsonObject("audio"));
                quality.parse$v1(json.getAsJsonObject("quality"));
                controller.parse$v1(json.getAsJsonObject("controller"));
                preferences.parse$v1(json.getAsJsonObject("preferences"));
                debug.parse$v1(json.getAsJsonObject("debug"));
            } else {
                Beatcraft.LOGGER.warn("Unrecognized config format version: {}, using default config values", ver);
            }

        } else {
            audio.parse$old(json);
            quality.parse$old(json);
            controller.parse$old(json);
            preferences.parse$old(json);
            debug.parse$old(json);
        }

    }

    private void writeJson(JsonObject json) {
        json.addProperty("version", FORMAT);

        json.add("audio", audio.getJson());
        json.add("quality", quality.getJson());
        json.add("controller", controller.getJson());
        json.add("preferences", preferences.getJson());
        json.add("debug", debug.getJson());
    }

    public PlayerConfig() {

        if (!configFile.exists()) {
            boolean ignored = configFolder.mkdirs();
        }

        if (!configFile.exists()) {
            try {
                boolean ignored = configFile.createNewFile();
                writeToFile();
            } catch (IOException e) {
                Beatcraft.LOGGER.error("Error creating player config file: ", e);
            }
        }
    }

    public static PlayerConfig loadFromFile() {
        try {
            String jsonString = Files.readString(Path.of("./config/beatcraft/config.json"));
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            return new PlayerConfig(json);
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to load player config! ", e);
            return new PlayerConfig();
        }
    }

    public void writeToFile() {
        try {
            JsonObject json = new JsonObject();
            writeJson(json);

            Files.writeString(Path.of("./config/beatcraft/config.json"), json.toString());
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to save player config ", e);
        }
    }

}