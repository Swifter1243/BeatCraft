package com.beatcraft.data;


import com.beatcraft.BeatCraft;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/* Settings that are a good idea to have implemented:
audio.volume=1
audio.ambient_volume_scale=0.8
audio.latency=0
audio.override_latency=0

quality.smoke_graphics=true
quality.burn_mark_trails=true
quality.max_cut_sounds=24

controller.selectedProfile.index=0

*/
public class PlayerConfig {

    private float audio_volume = 1.0f;
    private float audio_ambientVolumeScale = 0.8f;
    private int audio_latency = 0; // measured in milliseconds
    private boolean audio_overrideLatency = false;

    private boolean quality_smokeGraphics = true;
    private boolean quality_burnMarkTrails = true;
    private int quality_maxCutSounds = 24;

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
        quality_maxCutSounds = getIntOr(json, "quality.max_cut_sounds", quality_maxCutSounds);

        controller_selectedProfile_index = getIntOr(json, "controller.selectedProfile.index", controller_selectedProfile_index);

    }

    public PlayerConfig() {}

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

    public boolean shouldRenderSmoke() {
        return this.quality_smokeGraphics;
    }

    public boolean shouldRenderBurnMarkTrails() {
        return this.quality_burnMarkTrails;
    }

    public int getMaxCutSounds() {
        return this.quality_maxCutSounds;
    }

    public ControllerProfile getActiveControllerProfile() {
        if (profiles.isEmpty()) {
            return new ControllerProfile();
        } else {
            return profiles.get(Math.min(profiles.size(), this.controller_selectedProfile_index));
        }
    }


}
