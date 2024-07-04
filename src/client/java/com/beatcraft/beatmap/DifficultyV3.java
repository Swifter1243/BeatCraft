package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV3 extends Difficulty {
    public DifficultyV3 load(JsonObject json, Info.SetDifficulty setDifficulty) {
        JsonArray rawColorNotes = json.getAsJsonArray("colorNotes");

        rawColorNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            ColorNote note = new ColorNote().load(obj, setDifficulty);
            colorNotes.add(new PhysicalColorNote(note));
        });

        return this;
    }
}
