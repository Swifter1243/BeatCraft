package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV4 extends Difficulty {
    @Override
    DifficultyV4 load(JsonObject json, Info.SetDifficulty setDifficulty) {
        loadNotes(json, setDifficulty);

        return this;
    }

    void loadNotes(JsonObject json, Info.SetDifficulty setDifficulty) {
        JsonArray rawColorNotes = json.getAsJsonArray("colorNotes");
        rawColorNotes.addAll(json.getAsJsonArray("fakeColorNotes"));
        JsonArray rawColorNotesData = json.getAsJsonArray("colorNoteData");
        rawColorNotesData.addAll(json.getAsJsonArray("fakeColorNoteData"));

        rawColorNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            int lutIndex = obj.get("i").getAsInt();
            JsonObject lut = rawColorNotesData.get(lutIndex).getAsJsonObject();

            ColorNote note = new ColorNote().loadV4(obj, lut, setDifficulty);
            colorNotes.add(new PhysicalColorNote(note));
        });
    }
}
