package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.beatmap.data.RotationEvent;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV2 extends Difficulty {
    public DifficultyV2(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }


    DifficultyV2 load(JsonObject json) {
        loadNotesAndBombs(json);
        loadEvents(json);
        doPostLoad();
        return this;
    }

    void loadNotesAndBombs(JsonObject json) {
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

    void loadEvents(JsonObject json) {
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
}
