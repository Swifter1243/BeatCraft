package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.beatmap.data.event.AssignPathAnimation;
import com.beatcraft.beatmap.data.event.AssignTrackParent;
import com.beatcraft.beatmap.data.object.BombNote;
import com.beatcraft.beatmap.data.object.ChainNoteHead;
import com.beatcraft.beatmap.data.object.ChainNoteLink;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.beatmap.data.event.RotationEvent;
import com.beatcraft.render.object.PhysicalBombNote;
import com.beatcraft.render.object.PhysicalChainNoteHead;
import com.beatcraft.render.object.PhysicalChainNoteLink;
import com.beatcraft.render.object.PhysicalColorNote;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.Pair;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DifficultyV3 extends Difficulty {

    public DifficultyV3(Info info, Info.SetDifficulty setDifficulty) {
        super(info, setDifficulty);
    }

    DifficultyV3 load(JsonObject json) {
        loadChains(json);
        loadNotes(json);
        loadBombs(json);
        loadArcs(json);
        loadBasicEvents(json);
        loadRotationEvents(json);
        loadPointDefinitions(json);
        loadCustomEvents(json);
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
        JsonArray rawBombNotes = json.getAsJsonArray("bombNotes");

        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");

            if (customData.has("fakeBombNotes")) {
                rawBombNotes.addAll(customData.getAsJsonArray("fakeBombNotes"));
            }
        }

        rawBombNotes.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            BombNote note = new BombNote().loadV3(obj, this);
            bombNotes.add(new PhysicalBombNote(note));
        });
    }

    void loadChains(JsonObject json) {
        JsonArray rawChainsData = json.getAsJsonArray("burstSliders");

        rawChainsData.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            Pair<ChainNoteHead, List<ChainNoteLink>> chain = ChainNoteHead.buildV3(obj, this);
            chainHeadNotes.add(new PhysicalChainNoteHead(chain.getLeft()));
            chain.getRight().forEach(c -> {
                chainLinkNotes.add(new PhysicalChainNoteLink(c));
            });
        });

    }

    void loadArcs(JsonObject json) {

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

    private void loadPointDefinitions(JsonObject json) {
        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");
            if (customData.has("pointDefinitions")) {
                JsonObject pointDefinitions = customData.getAsJsonObject("pointDefinitions");
                pointDefinitions.asMap().forEach((name, points) -> this.pointDefinitions.put(name, points.getAsJsonArray()));
            }
        }
    }

    private void loadCustomEvents(JsonObject json) {
        if (json.has("customData")) {
            JsonObject customData = json.getAsJsonObject("customData");
            if (customData.has("customEvents")) {
                JsonArray customEvents = customData.getAsJsonArray("customEvents");
                customEvents.forEach(o -> loadCustomEvent(o.getAsJsonObject()));
            }
        }
    }

    private void loadCustomEvent(JsonObject json) {
        String type = json.get("t").getAsString();
        switch (type) {
            case "AnimateTrack" -> animateTracks.add(new AnimateTrack().loadV3(json, this));
            case "AssignPathAnimation" -> assignPathAnimations.add(new AssignPathAnimation().loadV3(json, this));
            case "AssignTrackParent" -> assignTrackParents.add(new AssignTrackParent().loadV3(json, this));
        }
    }
}
