package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV3 extends Difficulty {
    public DifficultyV3(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    @Override
    DifficultyV3 load(JsonObject json) {
        loadNotes(json);

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

}
