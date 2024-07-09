package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.Info;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public abstract class Difficulty {
    public Info info;
    public Info.SetDifficulty setDifficulty;

    public Difficulty(Info info, Info.SetDifficulty setDifficulty) {
        this.info = info;
        this.setDifficulty = setDifficulty;
    }

    public ArrayList<PhysicalColorNote> colorNotes = new ArrayList<>();

    abstract Difficulty load(JsonObject json);
}
