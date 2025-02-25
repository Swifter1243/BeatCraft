package com.beatcraft.data;


import com.beatcraft.BeatCraft;
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

public class PlayerConfig {

    public final File configFile = new File("./config/beatcraft/config.json");
    public final File configFolder = new File("./config/beatcraft/");
    private float audio_volume = 1.0f;
    private float audio_ambientVolumeScale = 0.8f;
    private int audio_latency = 0; // measured in milliseconds
    private boolean audio_overrideLatency = false;

    private boolean quality_smokeGraphics = true;
    private boolean quality_burnMarkTrails = true;

    private int controller_selectedProfile_index = -1;

    private final ArrayList<ControllerProfile> profiles = new ArrayList<>();

    private static final ControllerProfile DEFAULT_CONTROLLER_PROFILE = new ControllerProfile();

    public PlayerConfig(JsonObject json) {
        this(); // set everything to default values

        audio_volume = JsonUtil.getOrDefault(json, "audio.volume", JsonElement::getAsFloat, audio_volume);
        audio_ambientVolumeScale = JsonUtil.getOrDefault(json, "audio.ambient_volume_scale", JsonElement::getAsFloat, audio_ambientVolumeScale);
        audio_latency = JsonUtil.getOrDefault(json, "audio.latency", JsonElement::getAsInt, audio_latency);
        audio_overrideLatency = JsonUtil.getOrDefault(json, "audio.override_latency", JsonElement::getAsBoolean, audio_overrideLatency);

        quality_smokeGraphics = JsonUtil.getOrDefault(json, "quality.smoke_graphics", JsonElement::getAsBoolean, quality_smokeGraphics);
        quality_burnMarkTrails = JsonUtil.getOrDefault(json, "quality.burn_mark_trails", JsonElement::getAsBoolean, quality_burnMarkTrails);

        controller_selectedProfile_index = JsonUtil.getOrDefault(json, "controller.selectedProfile.index", JsonElement::getAsInt, controller_selectedProfile_index);

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

        json.addProperty("quality.smoke_graphics", quality_smokeGraphics);
        json.addProperty("quality.burn_mark_trails", quality_burnMarkTrails);

        json.addProperty("controller.selectedProfile.index", controller_selectedProfile_index);

        JsonArray array = new JsonArray();

        profiles.forEach(profile -> {
            profile.writeJson(array);
        });

        json.add("controller.profiles", array);

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

    public void setVolume(float volume) {
        this.audio_volume = volume;
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
    }

    public boolean getOverrideLatency() {
        return audio_overrideLatency;
    }

    public void setLatency(long nanos) {
        audio_latency = (int) (nanos * 1_000_000);
    }

    public void setSmokeRendering(boolean value) {
        quality_smokeGraphics = value;
    }

    public boolean shouldRenderSmoke() {
        return this.quality_smokeGraphics;
    }

    public void setBurnMarkRendering(boolean value) {
        quality_burnMarkTrails = value;
    }

    public boolean shouldRenderBurnMarkTrails() {
        return this.quality_burnMarkTrails;
    }


    public ControllerProfile getActiveControllerProfile() {
        if (profiles.isEmpty() || controller_selectedProfile_index <= -1) {
            return DEFAULT_CONTROLLER_PROFILE;
        } else {
            return profiles.get(this.controller_selectedProfile_index);
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
