package com.beatcraft.beatmap;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.animation.event.AnimatedPathEventContainer;
import com.beatcraft.beatmap.data.event.*;
import com.beatcraft.beatmap.data.object.BeatmapObject;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.animation.track.TrackLibrary;
import com.beatcraft.beatmap.data.*;
import com.beatcraft.event.EventHandler;
import com.beatcraft.lightshow.environment.Environment;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.lightshow_event_visualizer.EventVisualizer;
import com.beatcraft.render.object.*;
import com.beatcraft.replay.PlayRecorder;
import com.beatcraft.replay.Replayer;
import com.google.gson.JsonArray;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Math;
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
    public final ArrayList<PhysicalBombNote> bombNotes = new ArrayList<>();
    public final ArrayList<PhysicalChainNoteHead> chainHeadNotes = new ArrayList<>();
    public final ArrayList<PhysicalChainNoteLink> chainLinkNotes = new ArrayList<>();
    public final ArrayList<PhysicalObstacle> obstacles = new ArrayList<>();
    public final ArrayList<PhysicalArc> arcs = new ArrayList<>();
    public final ArrayList<RotationEvent> rotationEvents = new ArrayList<>();
    public final ArrayList<AnimateTrack> animateTracks = new ArrayList<>();
    public final ArrayList<AssignPathAnimation> assignPathAnimations = new ArrayList<>();
    public final ArrayList<AssignTrackParent> assignTrackParents = new ArrayList<>();
    public final AssignTrackParentHandler parentHandler = new AssignTrackParentHandler(assignTrackParents, trackLibrary);
    public final HashMap<String, JsonArray> pointDefinitions = new HashMap<>();

    public Environment lightShowEnvironment;

    public Difficulty(Info info, Info.SetDifficulty setDifficulty) {
        this.info = info;
        this.setDifficulty = setDifficulty;
    }

    public static int compareObjects(BeatmapObject o1, BeatmapObject o2) {
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
        bombNotes.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        chainHeadNotes.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        chainLinkNotes.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        obstacles.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        arcs.sort((o1, o2) -> compareObjects(o1.getData(), o2.getData()));
        rotationEvents.sort(Difficulty::compareObjects);
        animateTracks.sort(Difficulty::compareObjects);
        assignPathAnimations.sort(Difficulty::compareObjects);
    }

    private void applyRotationEvents() {
        EventHandler<Float, RotationEvent> eventHandler = new RotationEventHandler(rotationEvents);
        applyRotationOnArray(eventHandler, colorNotes);
        applyRotationOnArray(eventHandler, bombNotes);
        applyRotationOnArray(eventHandler, chainHeadNotes);
        applyRotationOnArray(eventHandler, chainLinkNotes);
        applyRotationOnArray(eventHandler, obstacles);
        applyRotationOnArray(eventHandler, arcs);
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
        chainHeadNotes.forEach(PhysicalChainNoteHead::finalizeBaseRotation);
        chainLinkNotes.forEach(PhysicalChainNoteLink::finalizeBaseRotation);
    }

    private void setupAnimatedProperties() {
        animateTracks.forEach(event -> {
            AnimatedPropertyEventContainer animatedPropertyEvents = event.toAnimatedPropertyEvents();
            event.getTracks().forEach(track -> track.loadAnimatedPropertyEvents(animatedPropertyEvents));
        });

        assignPathAnimations.forEach(event -> {
            AnimatedPathEventContainer animatedPathEvents = event.toAnimatedPathEvents();
            event.getTracks().forEach(track -> track.loadAnimatedPathEvents(animatedPathEvents));
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

    public void render(MatrixStack matrices, Camera camera) {
        if (HUDRenderer.scene == HUDRenderer.MenuScene.Paused) return;
        if (lightShowEnvironment != null) {
            lightShowEnvironment.render(matrices, camera);
        }
        if (BeatCraftClient.playerConfig.isModifierActive("Zen Mode")) return;
        colorNotes.forEach(o -> o.render(matrices, camera));
        if (!BeatCraftClient.playerConfig.isModifierActive("No Bombs")) bombNotes.forEach(o -> o.render(matrices, camera));
        chainHeadNotes.forEach(o -> o.render(matrices, camera));
        chainLinkNotes.forEach(o -> o.render(matrices, camera));
        if (!BeatCraftClient.playerConfig.isModifierActive("No Walls")) obstacles.forEach(o -> o.render(matrices, camera));
        arcs.forEach(o -> o.render(matrices, camera));
    }

    public void seek(float beat) {
        trackLibrary.seek(beat);
        parentHandler.seek(beat);
        if (lightShowEnvironment != null) lightShowEnvironment.seek(beat);
        colorNotes.forEach(o -> o.seek(beat));
        bombNotes.forEach(o -> o.seek(beat));
        chainHeadNotes.forEach(o -> o.seek(beat));
        chainLinkNotes.forEach(o -> o.seek(beat));
        obstacles.forEach(o -> o.seek(beat));
        arcs.forEach(o -> o.seek(beat));
        PlayRecorder.seek(beat);
        Replayer.seek(beat);
    }

    public void update(float beat, double deltaTime) {
        trackLibrary.update(beat);
        parentHandler.update(beat);
        if (lightShowEnvironment != null) {
            lightShowEnvironment.update(beat, deltaTime);
            EventVisualizer.update(beat);
        }
        if (BeatCraftClient.playerConfig.isModifierActive("Zen Mode")) return;
        colorNotes.forEach(o -> o.update(beat));
        if (!BeatCraftClient.playerConfig.isModifierActive("No Bombs")) bombNotes.forEach(o -> o.update(beat));
        chainHeadNotes.forEach(o -> o.update(beat));
        chainLinkNotes.forEach(o -> o.update(beat));
        if (!BeatCraftClient.playerConfig.isModifierActive("No Walls")) obstacles.forEach(o -> o.update(beat));
        arcs.forEach(o -> o.update(beat));
        PlayRecorder.update(beat);
        Replayer.update(beat);
    }

    public TrackLibrary getTrackLibrary() {
        return trackLibrary;
    }
}
