package com.beatcraft.lightshow.environment;


import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.event.events.LightEvent;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.event.handlers.LightEventHandler;
import com.beatcraft.lightshow.event.handlers.ValueEventHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.HashMap;

public class EnvironmentV2 extends Environment {

    private LightEventHandler leftRotatingLaserLightHandler = null;
    private LightEventHandler rightRotatingLaserLightHandler = null;

    private ValueEventHandler leftRotatingLaserValueHandler = null;
    private ValueEventHandler rightRotatingLaserValueHandler = null;

    private LightEventHandler backLaserLightHandler = null;
    private LightEventHandler centerLaserLightHandler = null;

    private LightEventHandler ringLightHandler = null;
    private ValueEventHandler ringZoomHandler = null;
    private ValueEventHandler ringSpinHandler = null;

    private final HashMap<EventGroup, LightGroup> lightGroups = new HashMap<>();
    private final ArrayList<LightGroup> uniqueGroups = new ArrayList<>();

    public void bindLightGroup(EventGroup eventGroup, LightGroupV2 lightGroup) {
        lightGroups.put(eventGroup, lightGroup);
        if (!uniqueGroups.contains(lightGroup)) {
            uniqueGroups.add(lightGroup);
        }
    }


    public void loadLightshow(Difficulty difficulty, JsonObject json) {
        JsonArray events = json.getAsJsonArray("_events");

        ArrayList<LightEvent> lrlEvents = new ArrayList<>();
        ArrayList<LightEvent> rrlEvents = new ArrayList<>();
        ArrayList<ValueEvent> lrrEvents = new ArrayList<>();
        ArrayList<ValueEvent> rrrEvents = new ArrayList<>();
        ArrayList<LightEvent> backEvents = new ArrayList<>();
        ArrayList<LightEvent> centerEvents = new ArrayList<>();
        ArrayList<LightEvent> rlEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlsEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlzEvents = new ArrayList<>();

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            EventGroup group = EventGroup.fromType(obj.get("_type").getAsInt());


            switch (group) {
                case LEFT_LASERS -> lrlEvents.add(new LightEvent().loadV2(obj, difficulty));
                case RIGHT_LASERS -> rrlEvents.add(new LightEvent().loadV2(obj, difficulty));
                case LEFT_ROTATING_LASERS -> lrrEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case RIGHT_ROTATING_LASERS -> rrrEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case BACK_LASERS -> backEvents.add(new LightEvent().loadV2(obj, difficulty));
                case CENTER_LASERS -> centerEvents.add(new LightEvent().loadV2(obj, difficulty));
                case RING_LIGHTS -> rlEvents.add(new LightEvent().loadV2(obj, difficulty));
                case RING_SPIN -> rlsEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case RING_ZOOM -> rlzEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case null, default -> {}
            }

        });

        lrlEvents.sort(difficulty::compareObjects);
        rrlEvents.sort(difficulty::compareObjects);
        lrrEvents.sort(difficulty::compareObjects);
        rrrEvents.sort(difficulty::compareObjects);
        backEvents.sort(difficulty::compareObjects);
        centerEvents.sort(difficulty::compareObjects);
        rlEvents.sort(difficulty::compareObjects);
        rlsEvents.sort(difficulty::compareObjects);
        rlzEvents.sort(difficulty::compareObjects);

        leftRotatingLaserLightHandler = new LightEventHandler(lrlEvents);
        rightRotatingLaserLightHandler = new LightEventHandler(rrlEvents);

        leftRotatingLaserValueHandler = new ValueEventHandler(lrrEvents);
        rightRotatingLaserValueHandler = new ValueEventHandler(rrrEvents);

        backLaserLightHandler = new LightEventHandler(backEvents);
        centerLaserLightHandler = new LightEventHandler(centerEvents);

        ringLightHandler = new LightEventHandler(rlEvents);
        ringSpinHandler = new ValueEventHandler(rlsEvents);
        ringZoomHandler = new ValueEventHandler(rlzEvents);

    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);

        LightGroup lrlGroup = lightGroups.get(EventGroup.LEFT_LASERS);
        LightGroup rrlGroup = lightGroups.get(EventGroup.RIGHT_LASERS);
        LightGroup lrrGroup = lightGroups.get(EventGroup.LEFT_ROTATING_LASERS);
        LightGroup rrrGroup = lightGroups.get(EventGroup.RIGHT_ROTATING_LASERS);
        LightGroup backGroup = lightGroups.get(EventGroup.BACK_LASERS);
        LightGroup centerGroup = lightGroups.get(EventGroup.CENTER_LASERS);

        LightGroup rlGroup = lightGroups.get(EventGroup.RING_LIGHTS);
        LightGroup rlsGroup = lightGroups.get(EventGroup.RING_SPIN);
        LightGroup rlzGroup = lightGroups.get(EventGroup.RING_ZOOM);

        if (lrlGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.LEFT_LASERS, leftRotatingLaserLightHandler.update(beat));
        }
        if (rrlGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.RIGHT_LASERS, rightRotatingLaserLightHandler.update(beat));
        }
        if (lrrGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.LEFT_ROTATING_LASERS, leftRotatingLaserValueHandler.update(beat));
        }
        if (rrrGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.RIGHT_ROTATING_LASERS, rightRotatingLaserValueHandler.update(beat));
        }
        if (backGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.BACK_LASERS, backLaserLightHandler.update(beat));
        }
        if (centerGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.CENTER_LASERS, centerLaserLightHandler.update(beat));
        }
        if (rlGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.RING_LIGHTS, ringLightHandler.update(beat));
        }
        if (rlsGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.RING_SPIN, ringSpinHandler.update(beat));
        }
        if (rlzGroup instanceof LightGroupV2 lgv2) {
            lgv2.handleEvent(EventGroup.RING_ZOOM, ringZoomHandler.update(beat));
        }
        for (var group : uniqueGroups) {
            group.update(beat, deltaTime);
        }

    }

    @Override
    public void render(MatrixStack matrices, Camera camera) {
        super.render(matrices, camera);
        uniqueGroups.forEach(v -> {
            v.render(matrices, camera);
        });
    }
}
