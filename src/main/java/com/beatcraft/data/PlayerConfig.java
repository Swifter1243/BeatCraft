package com.beatcraft.data;


import com.beatcraft.BeatCraft;
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

    private int controller_selectedProfile_index = 0;

    private ArrayList<ControllerProfile> profiles = new ArrayList<>();

    private float getFloatOr(JsonObject json, String key, float fallback) {
        if (json.has(key)) {
            try {
                return json.get(key).getAsFloat();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Invalid data type for option '{}'! ", key, e);
            }
        }
        return fallback;
    }

    private Integer getIntOr(JsonObject json, String key, int fallback) {
        if (json.has(key)) {
            try {
                return json.get(key).getAsInt();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Invalid data type for option '{}'! ", key, e);
            }
        }
        return fallback;
    }

    private boolean getBooleanOr(JsonObject json, String key, boolean fallback) {
        if (json.has(key)) {
            try {
                return json.get(key).getAsBoolean();
            } catch (Exception e) {
                BeatCraft.LOGGER.error("Invalid data type for option '{}'! ", key, e);
            }
        }
        return fallback;
    }

    public PlayerConfig(JsonObject json) {
        this(); // set everything to default values

        audio_volume = getFloatOr(json, "audio.volume", audio_volume);
        audio_ambientVolumeScale = getFloatOr(json, "audio.ambient_volume_scale", audio_ambientVolumeScale);
        audio_latency = getIntOr(json, "audio.latency", audio_latency);
        audio_overrideLatency = getBooleanOr(json, "audio.override_latency", audio_overrideLatency);

        quality_smokeGraphics = getBooleanOr(json, "quality.smoke_graphics", quality_smokeGraphics);
        quality_burnMarkTrails = getBooleanOr(json, "quality.burn_mark_trails", quality_burnMarkTrails);

        controller_selectedProfile_index = getIntOr(json, "controller.selectedProfile.index", controller_selectedProfile_index);

    }

    private void writeJson(JsonObject json) {
        json.addProperty("audio.volume", audio_volume);
        json.addProperty("audio.ambient_volume_scale", audio_ambientVolumeScale);
        json.addProperty("audio.latency", audio_latency);
        json.addProperty("audio.override_latency", audio_overrideLatency);

        json.addProperty("quality.smoke_graphics", quality_smokeGraphics);
        json.addProperty("quality.burn_mark_trails", quality_burnMarkTrails);

        json.addProperty("controller.selectedProfile.index", controller_selectedProfile_index);
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
        if (profiles.isEmpty()) {
            return new ControllerProfile();
        } else {
            return profiles.get(Math.min(profiles.size(), this.controller_selectedProfile_index));
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
