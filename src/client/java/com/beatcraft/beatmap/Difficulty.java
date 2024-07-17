package com.beatcraft.beatmap;

import com.beatcraft.beatmap.data.event.AnimateTrack;
import com.beatcraft.beatmap.data.event.RotationEvent;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.animation.track.TrackLibrary;
import com.beatcraft.beatmap.data.*;
import com.beatcraft.event.EventHandler;
import com.beatcraft.beatmap.data.event.RotationEventHandler;
import com.beatcraft.render.PhysicalGameplayObject;
import com.beatcraft.render.PhysicalColorNote;
import com.google.gson.JsonArray;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Difficulty {
    private final Info info;
    private final Info.SetDifficulty setDifficulty;
    private final TrackLibrary trackLibrary = new TrackLibrary();
    public final ArrayList<PhysicalColorNote> colorNotes = new ArrayList<>();
    public final ArrayList<RotationEvent> rotationEvents = new ArrayList<>();
    public final ArrayList<AnimateTrack> animateTracks = new ArrayList<>();
    public final HashMap<String, JsonArray> pointDefinitions = new HashMap<>();

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
        animateTracks.sort(this::compareObjects);
    }

    private void applyRotationEvents() {
        EventHandler<Float, RotationEvent> eventHandler = new RotationEventHandler(rotationEvents);
        applyRotationOnArray(eventHandler, colorNotes);
    }

    private <T extends PhysicalGameplayObject<K>, K extends GameplayObject> void applyRotationOnArray(EventHandler<Float, RotationEvent> eventHandler, ArrayList<T> array) {
        eventHandler.reset();
        array.forEach(o -> {
            float beat = o.getData().getBeat();
            float rotation = eventHandler.update(beat);

            if (rotation != 0) {
                o.setLaneRotation(new Quaternionf().rotateY(Math.toRadians(rotation)));
            }
        });
    }

    private void checkNotesWindowSnap() {
        Map<NoteType, List<PhysicalColorNote>> noteTypes = colorNotes.stream().collect(Collectors.groupingBy(o -> o.getData().getNoteType()));

        noteTypes.forEach((type, typedNotes) -> {
            Map<Float, List<PhysicalColorNote>> timeGroups = typedNotes.stream().collect(Collectors.groupingBy(o -> o.getData().getBeat()));

            timeGroups.forEach((time, notes) -> {
                if (notes.size() != 2) {
                    return;
                }

                PhysicalColorNote a = notes.get(0);
                PhysicalColorNote b = notes.get(1);
                a.checkWindowSnap(b);
                b.checkWindowSnap(a);
            });
        });
    }

    private void finalizeBaseRotations() {
        colorNotes.forEach(PhysicalColorNote::finalizeBaseRotation);
    }

    private void setupAnimatedProperties() {
        animateTracks.forEach(event -> {
            AnimatedPropertyEventContainer animatedPropertyEvents = event.toAnimatedPropertyEvents();
            event.getTracks().forEach(track -> track.loadAnimatedPropertyEvents(animatedPropertyEvents));
        });
    }

    protected void doPostLoad() {
        sortObjectsByTime();
        checkNotesWindowSnap();
        finalizeBaseRotations();
        applyRotationEvents();
        setupAnimatedProperties();
    }

    public Info getInfo() {
        return info;
    }

    public Info.SetDifficulty getSetDifficulty() {
        return setDifficulty;
    }

    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix) {
        colorNotes.forEach(o -> o.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix));
    }

    public void seek(float beat) {
        colorNotes.forEach(PhysicalGameplayObject::reset);
        trackLibrary.getTracks().forEach(track -> track.getAnimatedProperties().seek(beat));
    }

    public void update(float beat) {
        trackLibrary.getTracks().forEach(track -> track.getAnimatedProperties().update(beat));
    }

    public TrackLibrary getTrackLibrary() {
        return trackLibrary;
    }
}
