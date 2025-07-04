package com.beatcraft.data;


import com.beatcraft.BeatCraft;
import com.beatcraft.data.types.CycleStack;
import com.beatcraft.utils.JsonUtil;
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

    public final File configFile = new File("./config/beatcraft/config.json");
    public final File configFolder = new File("./config/beatcraft/");

    private float audio_volume = 1.0f;
    private float audio_ambientVolumeScale = 0.8f;
    private int audio_latency = 0; // measured in milliseconds
    private boolean audio_overrideLatency = false;

    private boolean quality_doBloomfog = true;
    private boolean quality_doBloom = true;
    private boolean quality_doMirror = true;
    private boolean quality_skyFog = true;
    private boolean quality_smokeGraphics = true;
    private boolean quality_burnMarkTrails = true;
    private boolean quality_sparkParticles = true;

    private final ArrayList<String> activeModifiers = new ArrayList<>();

    private int controller_selectedProfile_index = -1;

    private final ArrayList<ControllerProfile> profiles = new ArrayList<>();

    private static final ControllerProfile DEFAULT_CONTROLLER_PROFILE = new ControllerProfile();

    private boolean option_reducedDebris = false;
    private int option_trailIntensity = 30;

    public enum HealthStyle {
        Classic,
        Hearts
    }

    private HealthStyle option_healthStyle = HealthStyle.Hearts;

    private boolean setting_placeEnvironmentStructures = true;

    private boolean debug_lightshow_doEventRendering = false;
    private float debug_lightshow_beatSpacing = 8f;
    private float debug_lightshow_lookAheadDistance = 16f;
    private float debug_lightshow_lookBehindDistance = 8f;

    private String option_saber_name = "Default Saber";
    private List<String> option_saber_authors = List.of("BeatCraft");

    public PlayerConfig(JsonObject json) {
        this(); // set everything to default values

        audio_volume = JsonUtil.getOrDefault(json, "audio.volume", JsonElement::getAsFloat, audio_volume);
        audio_ambientVolumeScale = JsonUtil.getOrDefault(json, "audio.ambient_volume_scale", JsonElement::getAsFloat, audio_ambientVolumeScale);
        audio_latency = JsonUtil.getOrDefault(json, "audio.latency", JsonElement::getAsInt, audio_latency);
        audio_overrideLatency = JsonUtil.getOrDefault(json, "audio.override_latency", JsonElement::getAsBoolean, audio_overrideLatency);

        quality_doBloomfog = JsonUtil.getOrDefault(json, "quality.bloomfog", JsonElement::getAsBoolean, quality_doBloomfog);
        quality_doBloom = JsonUtil.getOrDefault(json, "quality.bloom", JsonElement::getAsBoolean, quality_doBloom);
        quality_doMirror = JsonUtil.getOrDefault(json, "quality.mirror", JsonElement::getAsBoolean, quality_doMirror);
        quality_skyFog = JsonUtil.getOrDefault(json, "quality.sky_fog", JsonElement::getAsBoolean, quality_skyFog);

        quality_smokeGraphics = JsonUtil.getOrDefault(json, "quality.smoke_graphics", JsonElement::getAsBoolean, quality_smokeGraphics);
        quality_burnMarkTrails = JsonUtil.getOrDefault(json, "quality.burn_mark_trails", JsonElement::getAsBoolean, quality_burnMarkTrails);
        quality_sparkParticles = JsonUtil.getOrDefault(json, "quality.spark_particles", JsonElement::getAsBoolean, quality_sparkParticles);

        option_reducedDebris = JsonUtil.getOrDefault(json, "option.reduced_debris", JsonElement::getAsBoolean, option_reducedDebris);
        option_trailIntensity = JsonUtil.getOrDefault(json, "option.trail_intensity", JsonElement::getAsInt, option_trailIntensity);
        option_healthStyle = HealthStyle.values()[Math.clamp(JsonUtil.getOrDefault(json, "option.health_style", JsonElement::getAsInt, 0), 0, HealthStyle.values().length-1)];

        debug_lightshow_doEventRendering = JsonUtil.getOrDefault(json, "debug.lightshow.render_events", JsonElement::getAsBoolean, debug_lightshow_doEventRendering);
        debug_lightshow_lookAheadDistance = JsonUtil.getOrDefault(json, "debug.lightshow.look_ahead", JsonElement::getAsFloat, debug_lightshow_lookAheadDistance);
        debug_lightshow_lookBehindDistance = JsonUtil.getOrDefault(json, "debug.lightshow.look_behind", JsonElement::getAsFloat, debug_lightshow_lookBehindDistance);
        debug_lightshow_beatSpacing = JsonUtil.getOrDefault(json, "debug.lightshow.beat_spacing", JsonElement::getAsFloat, debug_lightshow_beatSpacing);

        var custom_saber_model = JsonUtil.getOrDefault(json, "option.selected_saber_model", JsonElement::getAsJsonObject, new JsonObject());

        option_saber_name = JsonUtil.getOrDefault(custom_saber_model, "name", JsonElement::getAsString, option_saber_name);
        var defaultArray = new JsonArray();
        for (var auth : option_saber_authors) {
            defaultArray.add(auth);
        }
        option_saber_authors = JsonUtil.getOrDefault(custom_saber_model, "authors", JsonElement::getAsJsonArray, defaultArray)
            .asList().stream().map(JsonElement::getAsString).toList();

        CycleStack.updateTrailSize(option_trailIntensity);

        if (json.has("active_modifiers")) {
            JsonArray rawModifiers = json.getAsJsonArray("active_modifiers");
            rawModifiers.forEach(mod -> {

                activeModifiers.add(mod.getAsString());
            });
        }

        controller_selectedProfile_index = JsonUtil.getOrDefault(json, "controller.selectedProfile.index", JsonElement::getAsInt, controller_selectedProfile_index);

        setting_placeEnvironmentStructures = JsonUtil.getOrDefault(json, "setting.placeEnvironmentStructures", JsonElement::getAsBoolean, setting_placeEnvironmentStructures);

        if (json.has("controller.profiles")) {
            JsonArray rawProfiles = json.getAsJsonArray("controller.profiles");
            rawProfiles.forEach(rawProfile -> {
                JsonObject profileData = rawProfile.getAsJsonObject();

                ControllerProfile profile = new ControllerProfile(profileData);

                profiles.add(profile);
            });
        }

    }

    private void writeJson(JsonObject json) {
        json.addProperty("audio.volume", audio_volume);
        json.addProperty("audio.ambient_volume_scale", audio_ambientVolumeScale);
        json.addProperty("audio.latency", audio_latency);
        json.addProperty("audio.override_latency", audio_overrideLatency);

        json.addProperty("quality.bloomfog", quality_doBloomfog);
        json.addProperty("quality.bloom", quality_doBloom);
        json.addProperty("quality.mirror", quality_doMirror);
        json.addProperty("quality.sky_fog", quality_skyFog);

        json.addProperty("quality.smoke_graphics", quality_smokeGraphics);
        json.addProperty("quality.burn_mark_trails", quality_burnMarkTrails);
        json.addProperty("quality.spark_particles", quality_sparkParticles);

        json.addProperty("option.reduced_debris", option_reducedDebris);
        json.addProperty("option.trail_intensity", option_trailIntensity);
        json.addProperty("option.health_style", option_healthStyle.ordinal());

        json.addProperty("debug.lightshow.render_events", debug_lightshow_doEventRendering);
        json.addProperty("debug.lightshow.look_ahead", debug_lightshow_lookAheadDistance);
        json.addProperty("debug.lightshow.look_behind", debug_lightshow_lookBehindDistance);
        json.addProperty("debug.lightshow.beat_spacing", debug_lightshow_beatSpacing);

        json.addProperty("controller.selectedProfile.index", controller_selectedProfile_index);

        json.addProperty("setting.placeEnvironmentStructures", setting_placeEnvironmentStructures);

        var auths = new JsonArray();
        for (var auth : option_saber_authors) {
            auths.add(auth);
        }
        var selected_model = new JsonObject();
        selected_model.addProperty("name", option_saber_name);
        selected_model.add("authors", auths);

        json.add("option.selected_saber_model", selected_model);

        JsonArray array = new JsonArray();

        profiles.forEach(profile -> {
            profile.writeJson(array);
        });

        json.add("controller.profiles", array);

        JsonArray array2 = new JsonArray();

        activeModifiers.forEach(array2::add);

        json.add("active_modifiers", array2);

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
                BeatCraft.LOGGER.error("Error creating player config file: ", e);
            }
        }
    }

    public void setModifier(String modifier, boolean active) {
        if (active && !activeModifiers.contains(modifier)) {
            activeModifiers.add(modifier);
        } else if (!active) {
            activeModifiers.remove(modifier);
        }
        writeToFile();
    }

    public List<String> getActiveModifiers() {
        return activeModifiers;
    }

    public boolean isModifierActive(String modifier) {
        return activeModifiers.contains(modifier);
    }

    public void setVolume(float volume) {
        this.audio_volume = volume;
        writeToFile();
    }

    public float getVolume() {
        return this.audio_volume;
    }

    public float getAmbientAudioScale() {
        return this.audio_ambientVolumeScale;
    }

    public int getLatency() {
        if (audio_overrideLatency) {
            return this.audio_latency;
        }
        return 0;
    }

    public void setOverrideLatency(boolean enabled) {
        audio_overrideLatency = enabled;
        writeToFile();
    }

    public boolean getOverrideLatency() {
        return audio_overrideLatency;
    }

    public void setLatency(long nanos) {
        audio_latency = (int) (nanos * 1_000_000);
        writeToFile();
    }

    public void setSmokeRendering(boolean value) {
        quality_smokeGraphics = value;
        writeToFile();
    }

    public boolean shouldRenderSmoke() {
        return this.quality_smokeGraphics;
    }

    public void setBurnMarkRendering(boolean value) {
        quality_burnMarkTrails = value;
        writeToFile();
    }

    public boolean shouldRenderBurnMarkTrails() {
        return this.quality_burnMarkTrails;
    }

    public void setReducedDebris(boolean value) {
        option_reducedDebris = value;
        writeToFile();
    }

    public boolean isReducedDebris() {
        return option_reducedDebris;
    }

    public void setSparkParticles(boolean value) {
        quality_sparkParticles = value;
        writeToFile();
    }

    public boolean doSparkParticles() {
        return quality_sparkParticles;
    }

    public boolean doEnvironmentPlacing() {
        return setting_placeEnvironmentStructures;
    }

    public void setEnvironmentPlacing(boolean value) {
        setting_placeEnvironmentStructures = value;
        writeToFile();
    }

    public void setTrailIntensity(int value) {
        option_trailIntensity = value;
        CycleStack.updateTrailSize(value);
        writeToFile();
    }

    public int getTrailIntensity() {
        return option_trailIntensity;
    }

    public void setBloomfogEnabled(boolean value) {
        quality_doBloomfog = value;
        writeToFile();
    }

    public boolean doBloomfog() {
        return quality_doBloomfog;
    }

    public void setBloomEnabled(boolean value) {
        quality_doBloom = value;
        writeToFile();
    }

    public boolean doBloom() {
        return quality_doBloom;
    }

    public void setMirrorEnabled(boolean value) {
        quality_doMirror = value;
        writeToFile();
    }

    public boolean doMirror() {
        return quality_doMirror;
    }

    public void setSkyFogEnabled(boolean value) {
        quality_skyFog = value;
        writeToFile();
    }

    public boolean doSkyFog() {
        return quality_skyFog;
    }

    public HealthStyle getHealthStyle() {
        return option_healthStyle;
    }

    public void setHealthStyle(HealthStyle style) {
        option_healthStyle = style;
        writeToFile();
    }

    public void setHealthStyle(int style) {
        option_healthStyle = HealthStyle.values()[Math.clamp(style, 0, HealthStyle.values().length-1)];
        writeToFile();
    }

    public void setLightshowEventRendering(boolean state) {
        debug_lightshow_doEventRendering = state;
        writeToFile();
    }

    public boolean doLightshowEventRendering() {
        return debug_lightshow_doEventRendering;
    }

    public float getDebugLightshowLookAhead() {
        return debug_lightshow_lookAheadDistance;
    }

    public void setDebugLightshowLookAhead(float value) {
        debug_lightshow_lookAheadDistance = Math.max(1, value);
        writeToFile();
    }

    public float getDebugLightshowLookBehind() {
        return debug_lightshow_lookBehindDistance;
    }

    public void setDebugLightshowLookBehind(float value) {
        debug_lightshow_lookBehindDistance = Math.max(1, value);
        writeToFile();
    }

    public float getDebugLightshowBeatSpacing() {
        return debug_lightshow_beatSpacing;
    }

    public void setDebugLightshowBeatSpacing(float value) {
        debug_lightshow_beatSpacing = Math.max(1, value);
        writeToFile();
    }

    public String getSelectedSaberModelName() {
        return option_saber_name;
    }

    public List<String> getSelectedSaberModelAuthors() {
        return option_saber_authors;
    }

    public void setSelectedSaberModelName(String name) {
        option_saber_name = name;
        writeToFile();
    }

    public void setSelectedSaberModelAuthors(List<String> authors) {
        option_saber_authors = authors;
        writeToFile();
    }

    // Controller profiles
    public ControllerProfile getActiveControllerProfile() {
        if (profiles.isEmpty() || controller_selectedProfile_index <= -1) {
            return DEFAULT_CONTROLLER_PROFILE;
        } else {
            return profiles.get(this.controller_selectedProfile_index);
        }
    }

    public int getSelectedControllerProfileIndex() {
        return controller_selectedProfile_index;
    }

    public void selectProfile(int index) {
        controller_selectedProfile_index = Math.clamp(index, -1, profiles.size()-1);
    }

    public int getProfileCount() {
        return profiles.size();
    }

    public void addProfile() {
        ControllerProfile profile = new ControllerProfile();
        profiles.add(profile);
    }

    public void deleteControllerProfile(int index) {
        if (0 <= index && index < profiles.size()) {
            profiles.remove(index);
        }
    }


    public static PlayerConfig loadFromFile() {
        try {
            String jsonString = Files.readString(Path.of("./config/beatcraft/config.json"));
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            return new PlayerConfig(json);
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to load player config! ", e);
            return new PlayerConfig();
        }
    }

    public void writeToFile() {
        try {
            JsonObject json = new JsonObject();
            writeJson(json);

            Files.writeString(Path.of("./config/beatcraft/config.json"), json.toString());
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to save player config ", e);
        }
    }

}
