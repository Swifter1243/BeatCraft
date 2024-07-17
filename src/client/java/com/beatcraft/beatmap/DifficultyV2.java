package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.beatmap.data.event.RotationEvent;
import com.beatcraft.render.object.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

public class DifficultyV2 extends Difficulty {
    public DifficultyV2(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }


    DifficultyV2 load(JsonObject json) {
        loadNotesAndBombs(json);
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
                // TODO: Handle bombs
            } else {
                ColorNote note = new ColorNote().loadV2(obj, this);
                colorNotes.add(new PhysicalColorNote(note));
            }
        });
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
            case "AnimateTrack" -> {
                animateTracks.add(new AnimateTrack().loadV2(json, this));
                return;
            }
            case "AssignPathAnimation" -> {
                // TODO: Implement
                return;
            }
        }
    }
}
