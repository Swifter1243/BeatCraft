package com.beatcraft.client.beatmap.data;

import com.beatcraft.client.animation.base_providers.BaseProviderHandler;
import com.beatcraft.client.beatmap.BeatmapPlayer;
import com.beatcraft.client.beatmap.data.event.AnimateTrack;
import com.beatcraft.client.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.client.beatmap.data.event.AssignTrackParent;
import com.beatcraft.client.beatmap.data.event.RotationEvent;
import com.beatcraft.client.beatmap.object.data.Arc;
import com.beatcraft.client.beatmap.object.data.BombNote;
import com.beatcraft.client.beatmap.object.data.ColorNote;
import com.beatcraft.client.beatmap.object.data.Obstacle;
import com.beatcraft.client.beatmap.object.physical.PhysicalArc;
import com.beatcraft.client.beatmap.object.physical.PhysicalBombNote;
import com.beatcraft.client.beatmap.object.physical.PhysicalColorNote;
import com.beatcraft.client.beatmap.object.physical.PhysicalObstacle;
import com.beatcraft.client.lightshow.environment.EnvironmentUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class DifficultyV2 extends Difficulty {
    public DifficultyV2(BeatmapPlayer controller, Info info, Info.SetDifficulty setDifficulty) {
        super(controller, info, setDifficulty);
    }


    public DifficultyV2 load(JsonObject json) {
        loadLightshow(json);
        mapController.baseProvider.setupStaticProviders(getSetDifficulty().getColorScheme());
        loadNotesAndBombs(json);
        loadObstacles(json);
        loadArcs(json);
        loadEvents(json);
        loadPointDefinitions(json);
        loadCustomEvents(json);
        doPostLoad();
        return this;
    }

    private void loadNotesAndBombs(JsonObject json) {
        JsonArray rawNotes = json.getAsJsonArray("_notes");

        rawNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            int type = obj.get("_type").getAsInt();
            if (type == 3) {
                BombNote note = new BombNote(mapController).loadV2(obj, this);
                bombNotes.add(new PhysicalBombNote(mapController, note));
            } else {
                ColorNote note = new ColorNote(mapController).loadV2(obj, this);
                colorNotes.add(new PhysicalColorNote(mapController, note));
            }
        });
    }

    void loadObstacles(JsonObject json) {
        JsonArray rawObstacles = json.getAsJsonArray("_obstacles");

        rawObstacles.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            Obstacle obstacle = new Obstacle(mapController).loadV2(obj, this);
            obstacles.add(new PhysicalObstacle(mapController, obstacle));
        });
    }

    void loadArcs(JsonObject json) {

        if (json.has("_sliders")) {
            JsonArray rawArcs = json.getAsJsonArray("_sliders");


            rawArcs.forEach(o -> {
                JsonObject obj = o.getAsJsonObject();
                Arc arc = new Arc(mapController).loadV2(obj, this);
                arcs.add(new PhysicalArc(mapController, arc));
            });

        }

    }

    private void loadEvents(JsonObject json) {
        JsonArray events = json.getAsJsonArray("_events");

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            EventGroup group = EventGroup.fromType(obj.get("_type").getAsInt());

            if (group == EventGroup.EARLY_ROTATION) {
                rotationEvents.add(new RotationEvent(true).loadV2(obj, this));
            } else if (group == EventGroup.LATE_ROTATION) {
                rotationEvents.add(new RotationEvent(false).loadV2(obj, this));
            }
        });
    }

    private void loadPointDefinitions(JsonObject json) {
        if (json.has("_customData")) {
            JsonObject customData = json.getAsJsonObject("_customData");
            if (customData.has("_pointDefinitions")) {
                JsonArray pointDefinitions = customData.getAsJsonArray("_pointDefinitions");
                pointDefinitions.forEach(x -> {
                    JsonObject pointDefinition = x.getAsJsonObject();
                    String name = GsonHelper.getAsString(pointDefinition, "_name");
                    JsonArray points = pointDefinition.getAsJsonArray("_points");
                    this.pointDefinitions.put(name, points);
                });
            }
        }
    }

    private void loadCustomEvents(JsonObject json) {
        if (json.has("_customData")) {
            JsonObject customData = json.getAsJsonObject("_customData");
            if (customData.has("_customEvents")) {
                JsonArray customEvents = customData.getAsJsonArray("_customEvents");
                customEvents.forEach(o -> loadCustomEvent(o.getAsJsonObject()));
            }
        }
    }

    private void loadCustomEvent(JsonObject json) {
        String type = json.get("_type").getAsString();
        switch (type) {
            case "AnimateTrack" -> animateTracks.add(new AnimateTrack(mapController).loadV2(json, this));
            case "AssignPathAnimation" -> assignPathAnimations.add(new AssignPathAnimation(mapController).loadV2(json, this));
            case "AssignTrackParent" -> assignTrackParents.add(new AssignTrackParent().loadV2(json, this));
        }
    }

    private void loadLightshow(JsonObject json) {
        lightShowEnvironment = EnvironmentUtils.load(this.mapController, json);
    }

}
