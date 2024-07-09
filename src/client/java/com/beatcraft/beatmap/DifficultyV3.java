package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV3 extends Difficulty {
    @Override
    DifficultyV3 load(JsonObject json, Info.SetDifficulty setDifficulty) {
        loadNotes(json, setDifficulty);

        return this;
    }

    void loadNotes(JsonObject json, Info.SetDifficulty setDifficulty) {
        JsonArray rawColorNotes = json.getAsJsonArray("colorNotes");
        rawColorNotes.addAll(json.getAsJsonArray("fakeColorNotes"));

        rawColorNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            ColorNote note = new ColorNote().loadV3(obj, setDifficulty);
            colorNotes.add(new PhysicalColorNote(note));
        });
    }

}
