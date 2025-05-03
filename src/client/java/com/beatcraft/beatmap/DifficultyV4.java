package com.beatcraft.beatmap;

import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.object.*;
import com.beatcraft.lightshow.environment.EnvironmentUtils;
import com.beatcraft.render.object.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DifficultyV4 extends Difficulty {
    public DifficultyV4(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    DifficultyV4 load(JsonObject json) {
        loadChains(json);
        loadNotes(json);
        loadBombs(json);
        loadObstacles(json);
        loadArcs(json);
        //loadBasicEvents(json);
        //loadRotationEvents(json);
        //loadPointDefinitions(json);
        //loadCustomEvents(json);
        loadLightshow();
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
            AtomicBoolean canAdd = new AtomicBoolean(true);
            chainHeadNotes.forEach(c -> {
                if (
                    note.getBeat() == c.getData().getBeat() &&
                    note.getX() == c.getData().getX() &&
                    note.getY() == c.getData().getY()
                ) {
                    canAdd.set(false);
                }
            });

            if (canAdd.get()) {
                colorNotes.add(new PhysicalColorNote(note));
            }
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
            Obstacle obstacle = new Obstacle().loadV4(obj, obstacleMetaData, this);
            this.obstacles.add(new PhysicalObstacle(obstacle));
        });
    }

    void loadChains(JsonObject json) {
        JsonArray noteMetaData = json.getAsJsonArray("colorNotesData");
        JsonArray chainMetaData = json.getAsJsonArray("chainsData");

        JsonArray chainData = json.getAsJsonArray("chains");

        chainData.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            Pair<ChainNoteHead, List<ChainNoteLink>> chain = ChainNoteHead.buildV4(obj, noteMetaData, chainMetaData, this);
            chainHeadNotes.add(new PhysicalChainNoteHead(chain.getLeft()));
            chain.getRight().forEach(c -> {
                chainLinkNotes.add(new PhysicalChainNoteLink(c));
            });
        });

    }

    void loadArcs(JsonObject json) {
        JsonArray arcMetaData = json.getAsJsonArray("arcsData");
        JsonArray colorNotesData = json.getAsJsonArray("colorNotesData");

        JsonArray rawArcs = json.getAsJsonArray("arcs");

        rawArcs.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            Arc arc = new Arc().loadV4(obj, arcMetaData, colorNotesData, this);
            arcs.add(new PhysicalArc(arc));
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

    private void loadLightshow() {

        var path = getSetDifficulty().getLightshowFile();

        if (path == null) return;

        path = (getInfo().getMapDirectory() + "/" + path).replace("\\", "/");

        //BeatCraft.LOGGER.info("Load lightshow: \"{}\"", path);

        try {
            String jsonString = Files.readString(Paths.get(path));
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

            lightShowEnvironment = EnvironmentUtils.load(this, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
