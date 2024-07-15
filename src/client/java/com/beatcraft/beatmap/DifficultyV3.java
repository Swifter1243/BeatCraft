package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.beatmap.data.RotationEvent;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV3 extends Difficulty {
    public DifficultyV3(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    DifficultyV3 load(JsonObject json) {
        loadNotes(json);
        loadBasicEvents(json);
        loadRotationEvents(json);
        doPostLoad();
        return this;
    }

    void loadNotes(JsonObject json) {
        JsonArray rawColorNotes = json.getAsJsonArray("colorNotes");

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("fakeColorNotes")) {
                rawColorNotes.addAll(customData.getAsJsonArray("fakeColorNotes"));
            }
        }

        rawColorNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            ColorNote note = new ColorNote().loadV3(obj, this);
            colorNotes.add(new PhysicalColorNote(note));
        });
    }

    void loadBasicEvents(JsonObject json) {
        JsonArray events = json.getAsJsonArray("basicBeatmapEvents");

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            EventGroup group = EventGroup.fromType(obj.get("et").getAsInt());

            if (group == EventGroup.EARLY_ROTATION) {
                rotationEvents.add(new RotationEvent(true).fromBasicEventV3(obj, this));
            } else if (group == EventGroup.LATE_ROTATION) {
                rotationEvents.add(new RotationEvent(false).fromBasicEventV3(obj, this));
            }
        });
    }

    void loadRotationEvents(JsonObject json) {
        JsonArray events = json.getAsJsonArray("rotationEvents");

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

            boolean early = obj.get("e").getAsInt() == 1;
            rotationEvents.add(new RotationEvent(early).loadV3(obj, this));
        });
    }
}
