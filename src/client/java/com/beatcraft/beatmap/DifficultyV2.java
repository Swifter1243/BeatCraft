package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.beatmap.data.event.AssignTrackParent;
import com.beatcraft.beatmap.data.object.Arc;
import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.beatmap.data.event.RotationEvent;
import com.beatcraft.beatmap.data.object.Obstacle;
import com.beatcraft.lightshow.environment.Environments;
import com.beatcraft.render.object.PhysicalArc;
import com.beatcraft.render.object.PhysicalBombNote;
import com.beatcraft.render.object.PhysicalColorNote;
import com.beatcraft.render.object.PhysicalObstacle;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

public class DifficultyV2 extends Difficulty {
    public DifficultyV2(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }


    DifficultyV2 load(JsonObject json) {
        loadNotesAndBombs(json);
        loadObstacles(json);
        loadArcs(json);
        loadEvents(json);
        loadPointDefinitions(json);
        loadCustomEvents(json);
        loadLightshow(json);
        doPostLoad();
        return this;
    }

    private void loadNotesAndBombs(JsonObject json) {
        JsonArray rawNotes = json.getAsJsonArray("_notes");

        rawNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            int type = obj.get("_type").getAsInt();
            if (type == 3) {
                BombNote note = new BombNote().loadV2(obj, this);
                bombNotes.add(new PhysicalBombNote(note));
            } else {
                ColorNote note = new ColorNote().loadV2(obj, this);
                colorNotes.add(new PhysicalColorNote(note));
            }
        });
    }

    void loadObstacles(JsonObject json) {
        JsonArray rawObstacles = json.getAsJsonArray("_obstacles");

        rawObstacles.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            Obstacle obstacle = new Obstacle().loadV2(obj, this);
            obstacles.add(new PhysicalObstacle(obstacle));
        });
    }

    void loadArcs(JsonObject json) {

        if (json.has("_sliders")) {
            JsonArray rawArcs = json.getAsJsonArray("_sliders");


            rawArcs.forEach(o -> {
                JsonObject obj = o.getAsJsonObject();
                Arc arc = new Arc().loadV2(obj, this);
                arcs.add(new PhysicalArc(arc));
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
                    String name = JsonHelper.getString(pointDefinition, "_name");
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
            case "AnimateTrack" -> animateTracks.add(new AnimateTrack().loadV2(json, this));
            case "AssignPathAnimation" -> assignPathAnimations.add(new AssignPathAnimation().loadV2(json, this));
            case "AssignTrackParent" -> assignTrackParents.add(new AssignTrackParent().loadV2(json, this));
        }
    }

    private void loadLightshow(JsonObject json) {
        lightShowEnvironment = Environments.loadV2(this, json);
    }

}
