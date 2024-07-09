package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.ColorNote;
import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV4 extends Difficulty {
    public DifficultyV4(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    @Override
    DifficultyV4 load(JsonObject json) {
        loadNotes(json);

        return this;
    }

    void loadNotes(JsonObject json) {
        JsonArray rawColorNotes = json.getAsJsonArray("colorNotes");
        JsonArray rawColorNotesData = json.getAsJsonArray("colorNoteData");

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("fakeColorNotes")) {
                rawColorNotes.addAll(customData.getAsJsonArray("fakeColorNotes"));
            }
            if (customData.has("fakeColorNoteData")) {
                rawColorNotesData.addAll(customData.getAsJsonArray("fakeColorNoteData"));
            }
        }

        rawColorNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            int lutIndex = obj.get("i").getAsInt();
            JsonObject lut = rawColorNotesData.get(lutIndex).getAsJsonObject();

            ColorNote note = new ColorNote().loadV4(obj, lut, this);
            colorNotes.add(new PhysicalColorNote(note));
        });
    }
}
