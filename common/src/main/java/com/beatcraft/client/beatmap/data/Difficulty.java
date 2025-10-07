package com.beatcraft.client.beatmap.data;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.animation.event.AnimatedPathEventContainer;
import com.beatcraft.client.beatmap.data.event.*;
import com.beatcraft.client.beatmap.object.data.BeatmapObject;
import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.animation.event.AnimatedPropertyEventContainer;
import com.beatcraft.client.animation.track.TrackLibrary;
import com.beatcraft.common.event.EventHandler;
import com.beatcraft.client.lightshow.environment.Environment;
import com.beatcraft.client.render.lightshow_event_visualizer.EventVisualizer;
import com.beatcraft.client.beatmap.object.physical.*;
import com.beatcraft.client.replay.PlayRecorder;
import com.beatcraft.client.replay.Replayer;
import com.google.gson.JsonArray;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Math;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Difficulty {

    public final BeatmapController mapController;

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
    public float firstBeat = Float.MAX_VALUE;

    public Environment lightShowEnvironment;

    public Difficulty(BeatmapController controller, Info info, Info.SetDifficulty setDifficulty) {
        this.mapController = controller;
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

    private void indexObjects() {
        int i = 0;
        for (var note : colorNotes) note.getData().setIndex(i++);
        i = 0;
        for (var bomb : bombNotes) bomb.getData().setIndex(i++);
        i = 0;
        for (var head : chainHeadNotes) head.getData().setIndex(i++);
        i = 0;
        for (var link : chainLinkNotes) link.getData().setIndex(i++);
        i = 0;
        for (var wall : obstacles) wall.getData().setIndex(i++);
        i = 0;
        for (var arc : arcs) arc.getData().setIndex(i++);
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

        if (!colorNotes.isEmpty()) firstBeat = Math.min(firstBeat, colorNotes.getFirst().getData().getBeat());
        if (!chainHeadNotes.isEmpty()) firstBeat = Math.min(firstBeat, chainHeadNotes.getFirst().getData().getBeat());
        if (!chainLinkNotes.isEmpty()) firstBeat = Math.min(firstBeat, chainLinkNotes.getFirst().getData().getBeat());
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
        indexObjects();
    }

    public Info getInfo() {
        return info;
    }

    public Info.SetDifficulty getSetDifficulty() {
        return setDifficulty;
    }

    public void render(PoseStack matrices, Camera camera, float alpha) {
        matrices.pushPose();
        //if (HUDRenderer.scene == HUDRenderer.MenuScene.Paused) return;
        if (lightShowEnvironment != null) {
            lightShowEnvironment.render(matrices, camera, alpha);
        }
        if (mapController.isModifierActive("Zen Mode")) return;
        colorNotes.forEach(o -> o.render(matrices, camera, alpha));
        if (!mapController.isModifierActive("No Bombs")) bombNotes.forEach(o -> o.render(matrices, camera, alpha));
        chainHeadNotes.forEach(o -> o.render(matrices, camera, alpha));
        chainLinkNotes.forEach(o -> o.render(matrices, camera, alpha));
        if (!mapController.isModifierActive("No Walls")) obstacles.forEach(o -> o.render(matrices, camera, alpha));
        arcs.forEach(o -> o.render(matrices, camera, alpha));
        matrices.popPose();
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
        mapController.playRecorder.seek(beat);
        mapController.replayer.seek(beat);
    }

    public void update(float beat, double deltaTime) {
        //if (GameLogicHandler.isTrackingClient() && BeatmapPlayer.isPlaying()) {
        //    ClientPlayNetworking.send(new BeatSyncC2SPayload(BeatmapPlayer.getCurrentBeat()));
        //}
        trackLibrary.update(beat);
        parentHandler.update(beat);
        mapController.baseProvider.update();
        if (lightShowEnvironment != null) {
            lightShowEnvironment.update(beat, deltaTime);
            EventVisualizer.update(beat);
        }
        if (mapController.isModifierActive("Zen Mode")) return;
        colorNotes.forEach(o -> o.update(beat));
        if (!mapController.isModifierActive("No Bombs")) bombNotes.forEach(o -> o.update(beat));
        chainHeadNotes.forEach(o -> o.update(beat));
        chainLinkNotes.forEach(o -> o.update(beat));
        if (!mapController.isModifierActive("No Walls")) obstacles.forEach(o -> o.update(beat));
        arcs.forEach(o -> o.update(beat));
        mapController.playRecorder.update(beat);
        mapController.replayer.update(beat);
    }

    public TrackLibrary getTrackLibrary() {
        return trackLibrary;
    }
}
