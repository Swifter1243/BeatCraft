package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.render.object.PhysicalBombNote;
import com.beatcraft.render.object.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DifficultyV4 extends Difficulty {
    public DifficultyV4(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    DifficultyV4 load(JsonObject json) {
        loadNotes(json);
        loadBombs(json);
        loadObstacles(json);
        loadChains(json);
        loadArcs(json);
        //loadBasicEvents(json);
        //loadRotationEvents(json);
        //loadPointDefinitions(json);
        //loadCustomEvents(json);
        doPostLoad();
        return this;
    }

    void loadNotes(JsonObject json) {
        JsonArray noteMetaData = json.getAsJsonArray("colorNotesData");

        // customData needs to be implemented if it's even a thing for v4

        JsonArray noteData = json.getAsJsonArray("colorNotes");

        noteData.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            ColorNote note = new ColorNote().loadV4(obj, noteMetaData, this);
            colorNotes.add(new PhysicalColorNote(note));
        });

    }

    void loadBombs(JsonObject json) {
        JsonArray bombMetaData = json.getAsJsonArray("bombNotesData");

        JsonArray bombData = json.getAsJsonArray("bombNotes");

        bombData.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            BombNote bomb = new BombNote().loadV4(obj, bombMetaData, this);
            bombNotes.add(new PhysicalBombNote(bomb));
        });

    }

    void loadObstacles(JsonObject json) {
        JsonArray obstacleMetaData = json.getAsJsonArray("obstaclesData");

        JsonArray obstacles = json.getAsJsonArray("obstacles");

        obstacles.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

        });
    }

    void loadChains(JsonObject json) {
        JsonArray noteMetaData = json.getAsJsonArray("colorNotesData");
        JsonArray chainMetaData = json.getAsJsonArray("chainsData");

        JsonArray chainData = json.getAsJsonArray("chains");

        chainData.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

        });

    }

    void loadArcs(JsonObject json) {
        JsonArray arcMetaData = json.getAsJsonArray("arcsData");
        JsonArray noteMetaData = json.getAsJsonArray("colorNotesData");

        JsonArray arcs = json.getAsJsonArray("arcs");

        arcs.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

        });
    }

    //void loadBasicEvents(JsonObject json) {
    //
    //}
    //
    //void loadRotationEvents(JsonObject json) {
    //
    //}
    //
    //void loadPointDefinitions(JsonObject json) {
    //
    //}
    //
    //void loadCustomEvents(JsonObject json) {
    //
    //}

}
