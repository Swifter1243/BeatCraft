package com.beatcraft.lightshow.environment;


import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.lightgroup.LightGroup;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.event.events.LightEvent;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.event.handlers.LightEventHandler;
import com.beatcraft.lightshow.event.handlers.LightGroupEventHandlerV2;
import com.beatcraft.lightshow.event.handlers.ValueEventHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class EnvironmentV2 extends Environment {

    private LightGroupEventHandlerV2 leftRotatingLaserLightHandler = null;
    private LightGroupEventHandlerV2 rightRotatingLaserLightHandler = null;

    private ValueEventHandler leftRotatingLaserValueHandler = null;
    private ValueEventHandler rightRotatingLaserValueHandler = null;

    private LightGroupEventHandlerV2 backLaserLightHandler = null;
    private LightGroupEventHandlerV2 centerLaserLightHandler = null;

    private LightGroupEventHandlerV2 ringLightHandler = null;
    private ValueEventHandler ringZoomHandler = null;
    private ValueEventHandler ringSpinHandler = null;

    private final HashMap<EventGroup, LightGroupV2> lightGroups = new HashMap<>();
    private final ArrayList<LightGroupV2> uniqueGroups = new ArrayList<>();

    public void bindLightGroup(EventGroup eventGroup, LightGroupV2 lightGroup) {
        lightGroups.put(eventGroup, lightGroup);
        if (!uniqueGroups.contains(lightGroup)) {
            uniqueGroups.add(lightGroup);
        }
    }

    @Override
    public void setup() {
        var leftLasers = setupLeftLasers();
        bindLightGroup(EventGroup.LEFT_LASERS, leftLasers);
        bindLightGroup(EventGroup.LEFT_ROTATING_LASERS, leftLasers);

        var rightLasers = setupRightLasers();
        bindLightGroup(EventGroup.RIGHT_LASERS, rightLasers);
        bindLightGroup(EventGroup.RIGHT_ROTATING_LASERS, rightLasers);

        var backLasers = setupBackLasers();
        bindLightGroup(EventGroup.BACK_LASERS, backLasers);

        var centerLasers = setupCenterLasers();
        bindLightGroup(EventGroup.CENTER_LASERS, centerLasers);

        var ringLights = setupRingLights();
        bindLightGroup(EventGroup.RING_LIGHTS, ringLights);
        bindLightGroup(EventGroup.RING_SPIN, ringLights);
        bindLightGroup(EventGroup.RING_ZOOM, ringLights);
    }

    protected abstract LightGroupV2 setupLeftLasers();
    protected abstract LightGroupV2 setupRightLasers();
    protected abstract LightGroupV2 setupBackLasers();
    protected abstract LightGroupV2 setupCenterLasers();
    protected abstract LightGroupV2 setupRingLights();


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

        leftRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.LEFT_LASERS), lrlEvents);
        rightRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RIGHT_LASERS), rrlEvents);

        leftRotatingLaserValueHandler = new ValueEventHandler(lrrEvents);
        rightRotatingLaserValueHandler = new ValueEventHandler(rrrEvents);

        backLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.BACK_LASERS), backEvents);
        centerLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.CENTER_LASERS), centerEvents);

        ringLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RING_LIGHTS), rlEvents);
        ringSpinHandler = new ValueEventHandler(rlsEvents);
        ringZoomHandler = new ValueEventHandler(rlzEvents);

    }

    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);

        leftRotatingLaserLightHandler.update(beat);
        rightRotatingLaserLightHandler.update(beat);
        leftRotatingLaserValueHandler.update(beat); // TODO
        rightRotatingLaserValueHandler.update(beat); // TODO
        backLaserLightHandler.update(beat);
        centerLaserLightHandler.update(beat);
        ringLightHandler.update(beat);
        ringSpinHandler.update(beat); // TODO
        ringZoomHandler.update(beat); // TODO

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
