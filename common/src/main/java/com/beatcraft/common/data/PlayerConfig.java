package com.beatcraft.common.data;


import com.beatcraft.Beatcraft;
import com.beatcraft.common.data.types.CycleStack;
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


public class PlayerConfig {

    public static class Option<T> {
        private T value;
        public final String description;
        public final String note;

        public Option(T value, String description, String note) {
            this.value = value;
            this.description = description;
            this.note = note;
        }

        public void set(T val) {
            value = val;
        }
        public T get() {
            return value;
        }
    }

    public static final int FORMAT = 1;

    public class AudioSettings {
        public Option<Float> volume = new Option<>(1.0f, "Beatmap song volume", "");
        public Option<Float> ambientVolumeScale = new Option<>(0.8f, "", "Not implemented");
        public Option<Integer> latency = new Option<>(0, "Audio delay (ms)", "");
        public Option<Boolean> overrideLatency = new Option<>(false, "Should latency be accounted for", "");

        public float volume() { return volume.get(); }
        public void volume(float set) { volume.set(set); }

        public float ambientVolumeScale() { return ambientVolumeScale.get(); }
        public void ambientVolumeScale(float set) { ambientVolumeScale.set(set); }

        public int latency() { return latency.get(); }
        public void latency(int set) { latency.set(set); }

        public boolean overrideLatency() { return overrideLatency.get(); }
        public void overrideLatency(boolean set) { overrideLatency.set(set); }


        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("volume", volume.get());
            json.addProperty("ambient_volume_scale", ambientVolumeScale.get());
            json.addProperty("latency", latency.get());
            json.addProperty("override_latency", overrideLatency.get());

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            if (json == null) return;
            volume.value = Math.clamp(JsonUtil.getOrDefault(json, "audio.volume", JsonElement::getAsFloat, volume.value), 0f, 1f);
            ambientVolumeScale.value = Math.clamp(JsonUtil.getOrDefault(json, "audio.ambient_volume_scale", JsonElement::getAsFloat, ambientVolumeScale.value), 0f, 1f);
            latency.value = Math.min(0, JsonUtil.getOrDefault(json, "audio.latency", JsonElement::getAsInt, latency.value));
            overrideLatency.value = JsonUtil.getOrDefault(json, "audio.override_latency", JsonElement::getAsBoolean, overrideLatency.value);
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            volume.value = JsonUtil.getOrDefault(json, "volume", JsonElement::getAsFloat, volume.value);
            ambientVolumeScale.value = JsonUtil.getOrDefault(json, "ambient_volume_scale", JsonElement::getAsFloat, ambientVolumeScale.value);
            latency.value = JsonUtil.getOrDefault(json, "latency", JsonElement::getAsInt, latency.value);
            overrideLatency.value = JsonUtil.getOrDefault(json, "override_latency", JsonElement::getAsBoolean, overrideLatency.value);
        }
    }

    public class QualitySettings {

        public enum MirrorQuality {
            EXACT,
            SCUFFED,
        }

        public Option<Boolean> doBloomfog = new Option<>(true, "Toggles the Bloomfog render effect", "[Medium performance impact]");
        public Option<Boolean> doBloom = new Option<>(true, "Toggles Bloom post effect", "[Low performance impact]");
        public Option<Boolean> doMirror = new Option<>(true, "Toggles Mirror render effect", "[Performance varies]");
        public Option<MirrorQuality> mirrorQuality = new Option<>(MirrorQuality.EXACT, "Controls mirror rendering method", "EXACT: [Medium-Extreme performance impact]\nSCUFFED: [Medium performance impact]");
        public Option<Integer> mirrorLimit = new Option<>(1, "How many unique mirrors should be rendered?", "1: [Medium performance impact]\n2: [High performance impact]\n3+: [Extreme performance impact]");
        public Option<Boolean> skyFog = new Option<>(true, "Replace minecraft sky with a black, starless skybox", "[No performance impact]");
        public Option<Boolean> smokeGraphics = new Option<>(true, "Whether to render smoke around the player", "[Minimal performance impact]");
        public Option<Boolean> burnMarkTrails = new Option<>(true, "Saber burn marks", "NOT IMPLEMENTED");
        public Option<Boolean> sparkParticles = new Option<>(true, "Saber cut particles", "[Minimal performance impact]");

        public boolean doBloomfog() { return doBloomfog.get(); }
        public void doBloomfog(boolean set) { doBloomfog.set(set); }

        public boolean doBloom() { return doBloom.get(); }
        public void doBloom(boolean set) { doBloom.set(set); }

        public boolean doMirror() { return doMirror.get(); }
        public void doMirror(boolean set) { doMirror.set(set); }

        public MirrorQuality mirrorQuality() { return mirrorQuality.get(); }
        public void mirrorQuality(MirrorQuality set) { mirrorQuality.set(set); }

        public int mirrorLimit() { return mirrorLimit.get(); }
        public void mirrorLimit(int set) { mirrorLimit.set(set); }

        public boolean skyFog() { return skyFog.get(); }
        public void skyFog(boolean set) { skyFog.set(set); }

        public boolean smokeGraphics() { return smokeGraphics.get(); }
        public void smokeGraphics(boolean set) { smokeGraphics.set(set); }

        public boolean burnMarkTrails() { return burnMarkTrails.get(); }
        public void burnMarkTrails(boolean set) { burnMarkTrails.set(set); }

        public boolean sparkParticles() { return sparkParticles.get(); }
        public void sparkParticles(boolean set) { sparkParticles.set(set); }


        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("bloomfog", doBloomfog.value);
            json.addProperty("bloom", doBloom.value);
            json.addProperty("mirror", doMirror.value);
            json.addProperty("mirror_limit", mirrorLimit.value);
            json.addProperty("mirror_quality", mirrorQuality.value.ordinal());
            json.addProperty("sky_fog", skyFog.value);
            json.addProperty("smoke_graphics", smokeGraphics.value);
            json.addProperty("burn_mark_trails", burnMarkTrails.value);
            json.addProperty("spark_particles", sparkParticles.value);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            if (json == null) return;
            doBloomfog.value = JsonUtil.getOrDefault(json, "quality.bloomfog", JsonElement::getAsBoolean, doBloomfog.value);
            doBloom.value = JsonUtil.getOrDefault(json, "quality.bloom", JsonElement::getAsBoolean, doBloom.value);
            doMirror.value = JsonUtil.getOrDefault(json, "quality.mirror", JsonElement::getAsBoolean, doMirror.value);
            skyFog.value = JsonUtil.getOrDefault(json, "quality.sky_fog", JsonElement::getAsBoolean, skyFog.value);
            smokeGraphics.value = JsonUtil.getOrDefault(json, "quality.smoke_graphics", JsonElement::getAsBoolean, smokeGraphics.value);
            burnMarkTrails.value = JsonUtil.getOrDefault(json, "quality.burn_mark_trails", JsonElement::getAsBoolean, burnMarkTrails.value);
            sparkParticles.value = JsonUtil.getOrDefault(json, "quality.spark_particles", JsonElement::getAsBoolean, sparkParticles.value);
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            doBloomfog.value = JsonUtil.getOrDefault(json, "bloomfog", JsonElement::getAsBoolean, doBloomfog.value);
            doBloom.value = JsonUtil.getOrDefault(json, "bloom", JsonElement::getAsBoolean, doBloom.value);
            doMirror.value = JsonUtil.getOrDefault(json, "mirror", JsonElement::getAsBoolean, doMirror.value);
            mirrorLimit.value = Math.min(1, JsonUtil.getOrDefault(json, "mirror_limit", JsonElement::getAsInt, mirrorLimit.value));
            mirrorQuality.value = MirrorQuality.values()[Math.clamp(JsonUtil.getOrDefault(json, "mirror_quality", JsonElement::getAsInt, mirrorQuality.value.ordinal()), 0, MirrorQuality.values().length)];
            skyFog.value = JsonUtil.getOrDefault(json, "sky_fog", JsonElement::getAsBoolean, skyFog.value);
            smokeGraphics.value = JsonUtil.getOrDefault(json, "smoke_graphics", JsonElement::getAsBoolean, smokeGraphics.value);
            burnMarkTrails.value = JsonUtil.getOrDefault(json, "burn_mark_trails", JsonElement::getAsBoolean, burnMarkTrails.value);
            sparkParticles.value = JsonUtil.getOrDefault(json, "spark_particles", JsonElement::getAsBoolean, sparkParticles.value);
        }
    }

    public class ControllerSettings {
        private static final ControllerProfile DEFAULT_CONTROLLER_PROFILE = new ControllerProfile();
        public int selectedProfile = -1;
        public final ArrayList<ControllerProfile> profiles = new ArrayList<>();

        public ControllerProfile activeProfile() {
            if (selectedProfile == -1) {
                return DEFAULT_CONTROLLER_PROFILE;
            }
            return profiles.get(selectedProfile);
        }

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
            if (json == null) return;
            selectedProfile = JsonUtil.getOrDefault(json, "controller.selectedProfile.index", JsonElement::getAsInt, selectedProfile);
            parseProfiles$v1(json.getAsJsonArray("controller.profiles"));
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            selectedProfile = JsonUtil.getOrDefault(json, "selected_profile", JsonElement::getAsInt, selectedProfile);
            parseProfiles$v1(json.getAsJsonArray("profiles"));
        }

        private void parseProfiles$v1(JsonArray profiles) {
            for (var data : profiles) {
                var profile = new ControllerProfile(data.getAsJsonObject());
                this.profiles.add(profile);
            }
        }

        public int activeIndex() {
            return selectedProfile;
        }

        public void selectProfile(int index) {
            selectedProfile = Math.clamp(index, -1, profiles.size()-1);
        }

        public void addProfile() {
            profiles.add(new ControllerProfile());
        }

        public int getProfileCount() {
            return profiles.size();
        }

    }

    public enum HealthStyle {
        /// Classic beat saber look
        Classic,
        /// Minecraft style health bar
        Hearts
    }

    public class Preferences {
        public Option<Boolean> reducedDebris = new Option<>(false, "Whether to spawn debris from notes", "[No performance impact]");
        public Option<Integer> trailIntensity = new Option<>(30, "How many frames to save for saber trails", "[No performance impact]");
        public Option<HealthStyle> healthStyle = new Option<>(HealthStyle.Hearts, "Energy bar style", "Classic: the classic beat saber look\nHearts: minecraft hearts are used instead of a bar");
        public Option<String> selectedSaber = new Option<>("#builtin:default", "", "");

        public boolean reducedDebris() { return reducedDebris.get(); }
        public void reducedDebris(boolean set) { reducedDebris.set(set); }

        public int trailIntensity() { return trailIntensity.get(); }
        public void trailIntensity(int set) {
            trailIntensity.set(set);
            CycleStack.updateTrailSize(set);
        }

        public HealthStyle healthStyle() { return healthStyle.get(); }
        public void healthStyle(HealthStyle set) { healthStyle.set(set); }
        public void healthStyle(int set) { healthStyle(HealthStyle.values()[set]); }

        public String selectedSaber() { return selectedSaber.get(); }
        public void selectedSaber(String set) { selectedSaber.set(set); }


        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("reduced_debris", reducedDebris.value);
            json.addProperty("trail_intensity", trailIntensity.value);
            json.addProperty("health_style", healthStyle.value.ordinal());
            json.addProperty("selected_saber", selectedSaber.value);

            return json;
        }

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public void parse$old(JsonObject json) {
            if (json == null) return;
            reducedDebris.value = JsonUtil.getOrDefault(json, "option.reduced_debris", JsonElement::getAsBoolean, reducedDebris.value);
            trailIntensity.value = JsonUtil.getOrDefault(json, "option.trail_intensity", JsonElement::getAsInt, trailIntensity.value);
            healthStyle.value = HealthStyle.values()[Math.clamp(JsonUtil.getOrDefault(json, "option.health_style", JsonElement::getAsInt, healthStyle.value.ordinal()), 0, HealthStyle.values().length)];
            // skip selected saber... user will have to re-select
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            reducedDebris.value = JsonUtil.getOrDefault(json, "reduced_debris", JsonElement::getAsBoolean, reducedDebris.value);
            trailIntensity.value = JsonUtil.getOrDefault(json, "trail_intensity", JsonElement::getAsInt, trailIntensity.value);
            healthStyle.value = HealthStyle.values()[Math.clamp(JsonUtil.getOrDefault(json, "health_style", JsonElement::getAsInt, healthStyle.value.ordinal()), 0, HealthStyle.values().length)];
            selectedSaber.value = JsonUtil.getOrDefault(json, "selected_saber", JsonElement::getAsString, selectedSaber.value);
        }
    }

    public class DebugSettings {

        public class LightshowSettings {
            public Option<Boolean> renderEvents = new Option<>(false, "Light/Color events", "");
            public Option<Float> beatSpacing = new Option<>(8f, "space between beats in blocks (meters)", "");
            public Option<Float> lookAheadDistance = new Option<>(16f, "Distance in beats to start rendering events", "");
            public Option<Float> lookBehindDistance = new Option<>(8f, "Distance in beats to stop rendering events", "");

            public boolean renderEvents() { return renderEvents.get(); }
            public void renderEvents(boolean set) { renderEvents.set(set); }

            public float beatSpacing() { return beatSpacing.get(); }
            public void beatSpacing(float set) { beatSpacing.set(set); }

            public float lookAheadDistance() { return lookAheadDistance.get(); }
            public void lookAheadDistance(float set) { lookAheadDistance.set(set); }

            public float lookBehindDistance() { return lookBehindDistance.get(); }
            public void lookBehindDistance(float set) { lookBehindDistance.set(set); }


            public JsonObject getJson() {
                var json = new JsonObject();

                json.addProperty("render_events", renderEvents.value);
                json.addProperty("look_ahead", lookAheadDistance.value);
                json.addProperty("look_behind", lookBehindDistance.value);
                json.addProperty("beat_spacing", beatSpacing.value);

                return json;
            }

            public PlayerConfig parent() {
                return PlayerConfig.this;
            }

            public void parse$old(JsonObject json) {
                if (json == null) return;
                renderEvents.value = JsonUtil.getOrDefault(json, "debug.lightshow.render_events", JsonElement::getAsBoolean, renderEvents.value);
                lookAheadDistance.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "debug.lightshow.look_ahead", JsonElement::getAsFloat, lookAheadDistance.value));
                lookBehindDistance.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "debug.lightshow.look_behind", JsonElement::getAsFloat, lookBehindDistance.value));
                beatSpacing.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "debug.lightshow.beat_spacing", JsonElement::getAsFloat, beatSpacing.value));
            }

            public void parse$v1(JsonObject json) {
                if (json == null) return;
                renderEvents.value = JsonUtil.getOrDefault(json, "render_events", JsonElement::getAsBoolean, renderEvents.value);
                lookAheadDistance.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "look_ahead", JsonElement::getAsFloat, lookAheadDistance.value));
                lookBehindDistance.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "look_behind", JsonElement::getAsFloat, lookBehindDistance.value));
                beatSpacing.value = Math.min(0.01f, JsonUtil.getOrDefault(json, "beat_spacing", JsonElement::getAsFloat, beatSpacing.value));
            }

        }

        public class BeatmapSettings {

            public Option<Boolean> renderArcSplines = new Option<>(false, "Arc splines and control points", "");
            public Option<Boolean> renderHitboxes = new Option<>(false, "Hitboxes for Notes and bombs", "");
            public Option<Boolean> renderSaberColliders = new Option<>(false, "Saber hitboxes, other debug info for scoring", "");
            public Option<Boolean> renderBeatmapPosition = new Option<>(false, "Render beatmap origin", "");

            public boolean renderArcSplines() { return renderArcSplines.get(); }
            public void renderArcSplines(boolean set) { renderArcSplines.set(set); }
            public boolean renderHitboxes() { return renderHitboxes.get(); }
            public void renderHitboxes(boolean set) { renderHitboxes.set(set); }
            public boolean renderSaberColliders() { return renderSaberColliders.get(); }
            public void renderSaberColliders(boolean set) { renderSaberColliders.set(set); }
            public boolean renderBeatmapPosition() { return renderBeatmapPosition.get(); }
            public void renderBeatmapPosition(boolean set) { renderBeatmapPosition.set(set); }

            public JsonObject getJson() {
                var json = new JsonObject();

                json.addProperty("arcs", renderArcSplines.value);
                json.addProperty("hitboxes", renderHitboxes.value);
                json.addProperty("sabers", renderSaberColliders.value);
                json.addProperty("map_position", renderBeatmapPosition.value);

                return json;
            }

            public PlayerConfig parent() {
                return PlayerConfig.this;
            }

            public void parse$v1(JsonObject json) {
                if (json == null) return;
                renderArcSplines.value = JsonUtil.getOrDefault(json, "arcs", JsonElement::getAsBoolean, renderArcSplines.value);
                renderHitboxes.value = JsonUtil.getOrDefault(json, "hitboxes", JsonElement::getAsBoolean, renderHitboxes.value);
                renderSaberColliders.value = JsonUtil.getOrDefault(json, "sabers", JsonElement::getAsBoolean, renderSaberColliders.value);
                renderBeatmapPosition.value = JsonUtil.getOrDefault(json, "map_position", JsonElement::getAsBoolean, renderBeatmapPosition.value);

            }
        }


        ///  Settings related to lightshow debugging
        public final LightshowSettings lightshow = this.new LightshowSettings();

        /// Settings related to beatmap debugging
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
            if (json == null) return;
            lightshow.parse$old(json);
            // no beatmap settings stored in old config
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            lightshow.parse$v1(json.getAsJsonObject("lightshow"));
            beatmap.parse$v1(json.getAsJsonObject("beatmap"));
        }

    }

    public class MiscSettings {

        public Option<Boolean> allowBeatmapSharing = new Option<>(false, "If enabled, map info will be sent to the server and other players will be able to download maps from you (including custom maps you have that aren't on beatsaver)", "");

        public PlayerConfig parent() {
            return PlayerConfig.this;
        }

        public JsonObject getJson() {
            var json = new JsonObject();

            json.addProperty("share_beatmaps", allowBeatmapSharing.value);

            return json;
        }

        public void parse$v1(JsonObject json) {
            if (json == null) return;
            allowBeatmapSharing.value = JsonUtil.getOrDefault(json, "share_beatmaps", JsonElement::getAsBoolean, allowBeatmapSharing.value);
        }


    }

    public final File configFile = new File("./config/beatcraft/config.json");
    public final File configFolder = new File("./config/beatcraft/");

    public final AudioSettings audio = this.new AudioSettings();
    public final QualitySettings quality = this.new QualitySettings();
    public final ControllerSettings controller = this.new ControllerSettings();
    public final Preferences preferences = this.new Preferences();
    public final DebugSettings debug = this.new DebugSettings();
    public final MiscSettings misc = this.new MiscSettings();

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
                misc.parse$v1(json.getAsJsonObject("misc"));
            } else {
                Beatcraft.LOGGER.warn("Unrecognized config format version: {}, using default config values", ver);
            }

        } else {
            audio.parse$old(json);
            quality.parse$old(json);
            controller.parse$old(json);
            preferences.parse$old(json);
            debug.parse$old(json);
            // No misc settings
        }

        CycleStack.updateTrailSize(preferences.trailIntensity());

    }

    private void writeJson(JsonObject json) {
        json.addProperty("version", FORMAT);

        json.add("audio", audio.getJson());
        json.add("quality", quality.getJson());
        json.add("controller", controller.getJson());
        json.add("preferences", preferences.getJson());
        json.add("debug", debug.getJson());
        json.add("misc", misc.getJson());
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