package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.BeatmapObject;
import com.beatcraft.beatmap.data.GameplayObject;
import com.beatcraft.beatmap.data.RotationEvent;
import com.beatcraft.event.EventHandler;
import com.beatcraft.event.RotationEventHandler;
import com.beatcraft.render.PhysicalBeatmapObject;
import com.beatcraft.render.PhysicalColorNote;
import org.joml.Math;
import org.joml.Quaternionf;

import java.util.ArrayList;

public abstract class Difficulty {
    private final Info info;
    private final Info.SetDifficulty setDifficulty;
    public ArrayList<PhysicalColorNote> colorNotes = new ArrayList<>();
    public ArrayList<RotationEvent> rotationEvents = new ArrayList<>();

    public Difficulty(Info info, Info.SetDifficulty setDifficulty) {
        this.info = info;
        this.setDifficulty = setDifficulty;
    }

    protected int compareObjects(BeatmapObject o1, BeatmapObject o2) {
        float a = o1.getBeat();
        float b = o2.getBeat();

        if (a == b) {
            return 0;
        }
        else {
            return a > b ? 1 : -1;
        }
    }

    private void sortObjectsByTime() {
        colorNotes.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        rotationEvents.sort(this::compareObjects);
    }

    private void applyRotationEvents() {
        EventHandler<Float, RotationEvent> eventHandler = new RotationEventHandler(rotationEvents);
        applyRotationOnArray(eventHandler, colorNotes);
    }

    private <T extends PhysicalBeatmapObject<K>, K extends GameplayObject> void applyRotationOnArray(EventHandler<Float, RotationEvent> eventHandler, ArrayList<T> array) {
        eventHandler.reset();
        array.forEach(o -> {
            float beat = o.getData().getBeat();
            float rotation = eventHandler.update(beat);

            if (rotation != 0) {
                o.setLaneRotation(new Quaternionf().rotateY(Math.toRadians(rotation)));
            }
        });
    }

    protected void doPostLoad() {
        sortObjectsByTime();
        applyRotationEvents();
    }

    public Info getInfo() {
        return info;
    }

    public Info.SetDifficulty getSetDifficulty() {
        return setDifficulty;
    }
}
