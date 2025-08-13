package com.beatcraft.common.data;


import com.beatcraft.Beatcraft;
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
    }

    public class QualitySettings {
        public boolean doBloomfog = true;
        public boolean doBloom = true;
        public boolean doMirror = true;
        public boolean skyFog = true;
        public boolean smokeGraphics = true;
        public boolean burnMarkTrails = true;
        public boolean sparkParticles = true;
    }

    public class ControllerSettings {
        private static final ControllerProfile DEFAULT_CONTROLLER_PROFILE = new ControllerProfile();
        public int selectedProfile = -1;
        public final ArrayList<ControllerProfile> profiles = new ArrayList<>();

    }

    public enum HealthStyle {
        Classic,
        Hearts
    }

    public class Preferences {
        public boolean reducedDebris = false;
        public int trailIntensity = 30;
        public HealthStyle healthStyle = HealthStyle.Hearts;
        public String selectedSaberName = "Default Saber";
        public List<String> selectedSaberAuthors = List.of("Beatcraft");
    }

    public class DebugSettings {

        public class LightshowSettings {
            public boolean renderEvents = false;
            public float beatSpacing = 8;
            public float lookAheadDistance = 16;
            public float lookBehindDistance = 8;
        }

        public final LightshowSettings lightshow = this.new LightshowSettings();

        public DebugSettings() {

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
        this(); // set everything to default values

        if (json.has("version")) {
            var ver = json.get("version").getAsInt();

            if (ver == FORMAT) {

            } else {

            }

        } else {
            // parse pre 0.8.4 config
        }

    }

    private void writeJson(JsonObject json) {

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