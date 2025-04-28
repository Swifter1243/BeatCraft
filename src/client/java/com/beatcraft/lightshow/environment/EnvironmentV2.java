package com.beatcraft.lightshow.environment;


import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.event.events.ColorBoostEvent;
import com.beatcraft.lightshow.event.events.LightEventV2;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.event.handlers.ActionEventHandlerV2;
import com.beatcraft.lightshow.event.handlers.ColorBoostEventHandler;
import com.beatcraft.lightshow.event.handlers.LightGroupEventHandlerV2;
import com.beatcraft.utils.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class EnvironmentV2 extends Environment {

    private ColorBoostEventHandler colorBoostEventHandler = null;

    private LightGroupEventHandlerV2 leftRotatingLaserLightHandler = null;
    private LightGroupEventHandlerV2 rightRotatingLaserLightHandler = null;

    private ActionEventHandlerV2 leftRotatingLaserValueHandler = null;
    private ActionEventHandlerV2 rightRotatingLaserValueHandler = null;

    private LightGroupEventHandlerV2 backLaserLightHandler = null;
    private LightGroupEventHandlerV2 centerLaserLightHandler = null;

    private LightGroupEventHandlerV2 ringLightHandler = null;
    private ActionEventHandlerV2 ringZoomHandler = null;
    private ActionEventHandlerV2 ringSpinHandler = null;

    private HashMap<EventGroup, LightGroupV2> lightGroups;
    private ArrayList<LightGroupV2> uniqueGroups;

    public void bindLightGroup(EventGroup eventGroup, LightGroupV2 lightGroup) {
        lightGroups.put(eventGroup, lightGroup);
        if (!uniqueGroups.contains(lightGroup)) {
            uniqueGroups.add(lightGroup);
        }
    }

    @Override
    public void setup() {
        lightGroups = new HashMap<>();
        uniqueGroups = new ArrayList<>();

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
        if (json.has("_events")) {
            loadV2(difficulty, json);
        } else if (json.has("basicBeatmapEvents")) {
            loadV3(difficulty, json);
        } else if (json.has("basicEvents")) {
            loadV4(difficulty, json);
        }
    }

    private void loadV2(Difficulty difficulty, JsonObject json) {
        JsonArray events = json.getAsJsonArray("_events");
        var lrlEvents = new ArrayList<LightEventV2>();
        var rrlEvents = new ArrayList<LightEventV2>();
        var lrrEvents = new ArrayList<ValueEvent>();
        var rrrEvents = new ArrayList<ValueEvent>();
        var backEvents = new ArrayList<LightEventV2>();
        var centerEvents = new ArrayList<LightEventV2>();
        var rlEvents = new ArrayList<LightEventV2>();
        var rlsEvents = new ArrayList<ValueEvent>();
        var rlzEvents = new ArrayList<ValueEvent>();
        var boostEvents = new ArrayList<ColorBoostEvent>();
        boostEvents.add(new ColorBoostEvent(0, false));

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            int eventType = obj.get("_type").getAsInt();

            if (eventType == 5) {
                boostEvents.add(new ColorBoostEvent().loadV2(obj, difficulty));

                return;
            }

            EventGroup group = EventGroup.fromType(eventType);


            switch (group) {
                case LEFT_LASERS -> lrlEvents.add(new LightEventV2().loadV2(obj, difficulty));
                case RIGHT_LASERS -> rrlEvents.add(new LightEventV2().loadV2(obj, difficulty));
                case LEFT_ROTATING_LASERS -> lrrEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case RIGHT_ROTATING_LASERS -> rrrEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case BACK_LASERS -> backEvents.add(new LightEventV2().loadV2(obj, difficulty));
                case CENTER_LASERS -> centerEvents.add(new LightEventV2().loadV2(obj, difficulty));
                case RING_LIGHTS -> rlEvents.add(new LightEventV2().loadV2(obj, difficulty));
                case RING_SPIN -> rlsEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case RING_ZOOM -> rlzEvents.add(new ValueEvent().loadV2(obj, difficulty));
                case null, default -> {}
            }

        });

        boostEvents.sort(Difficulty::compareObjects);
        lrlEvents.sort(Difficulty::compareObjects);
        rrlEvents.sort(Difficulty::compareObjects);
        lrrEvents.sort(Difficulty::compareObjects);
        rrrEvents.sort(Difficulty::compareObjects);
        backEvents.sort(Difficulty::compareObjects);
        centerEvents.sort(Difficulty::compareObjects);
        rlEvents.sort(Difficulty::compareObjects);
        rlsEvents.sort(Difficulty::compareObjects);
        rlzEvents.sort(Difficulty::compareObjects);

        leftRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.LEFT_LASERS), lrlEvents);
        rightRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RIGHT_LASERS), rrlEvents);

        leftRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.LEFT_ROTATING_LASERS), lrrEvents, EventGroup.LEFT_ROTATING_LASERS);
        rightRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RIGHT_ROTATING_LASERS), rrrEvents, EventGroup.RIGHT_ROTATING_LASERS);

        backLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.BACK_LASERS), backEvents);
        centerLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.CENTER_LASERS), centerEvents);

        ringLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RING_LIGHTS), rlEvents);
        ringSpinHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_SPIN), rlsEvents, EventGroup.RING_SPIN);
        ringZoomHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_ZOOM), rlzEvents, EventGroup.RING_ZOOM);

        colorBoostEventHandler = new ColorBoostEventHandler(boostEvents);
    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        var events = json.getAsJsonArray("basicBeatmapEvents");
        var rawBoostEvents = json.getAsJsonArray("colorBoostBeatmapEvents");

        var lrlEvents = new ArrayList<LightEventV2>();
        var rrlEvents = new ArrayList<LightEventV2>();
        var lrrEvents = new ArrayList<ValueEvent>();
        var rrrEvents = new ArrayList<ValueEvent>();
        var backEvents = new ArrayList<LightEventV2>();
        var centerEvents = new ArrayList<LightEventV2>();
        var rlEvents = new ArrayList<LightEventV2>();
        var rlsEvents = new ArrayList<ValueEvent>();
        var rlzEvents = new ArrayList<ValueEvent>();
        var boostEvents = new ArrayList<ColorBoostEvent>();
        boostEvents.add(new ColorBoostEvent(0, false));

        rawBoostEvents.forEach(rawEvent -> {
            var eventData = rawEvent.getAsJsonObject();
            boostEvents.add(new ColorBoostEvent().loadV3(eventData, difficulty));
        });

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            EventGroup group = EventGroup.fromType(obj.get("et").getAsInt());

            switch (group) {
                case LEFT_LASERS -> lrlEvents.add(new LightEventV2().loadV3(obj, difficulty));
                case RIGHT_LASERS -> rrlEvents.add(new LightEventV2().loadV3(obj, difficulty));
                case LEFT_ROTATING_LASERS -> lrrEvents.add(new ValueEvent().loadV3(obj, difficulty));
                case RIGHT_ROTATING_LASERS -> rrrEvents.add(new ValueEvent().loadV3(obj, difficulty));
                case BACK_LASERS -> backEvents.add(new LightEventV2().loadV3(obj, difficulty));
                case CENTER_LASERS -> centerEvents.add(new LightEventV2().loadV3(obj, difficulty));
                case RING_LIGHTS -> rlEvents.add(new LightEventV2().loadV3(obj, difficulty));
                case RING_SPIN -> rlsEvents.add(new ValueEvent().loadV3(obj, difficulty));
                case RING_ZOOM -> rlzEvents.add(new ValueEvent().loadV3(obj, difficulty));
                case null, default -> {}
            }

        });

        lrlEvents.sort(Difficulty::compareObjects);
        rrlEvents.sort(Difficulty::compareObjects);
        lrrEvents.sort(Difficulty::compareObjects);
        rrrEvents.sort(Difficulty::compareObjects);
        backEvents.sort(Difficulty::compareObjects);
        centerEvents.sort(Difficulty::compareObjects);
        rlEvents.sort(Difficulty::compareObjects);
        rlsEvents.sort(Difficulty::compareObjects);
        rlzEvents.sort(Difficulty::compareObjects);
        boostEvents.sort(Difficulty::compareObjects);

        // these stay as V2 since it's a V2 environment
        leftRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.LEFT_LASERS), lrlEvents);
        rightRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RIGHT_LASERS), rrlEvents);

        leftRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.LEFT_ROTATING_LASERS), lrrEvents, EventGroup.LEFT_ROTATING_LASERS);
        rightRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RIGHT_ROTATING_LASERS), rrrEvents, EventGroup.RIGHT_ROTATING_LASERS);

        backLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.BACK_LASERS), backEvents);
        centerLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.CENTER_LASERS), centerEvents);

        ringLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RING_LIGHTS), rlEvents);
        ringSpinHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_SPIN), rlsEvents, EventGroup.RING_SPIN);
        ringZoomHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_ZOOM), rlzEvents, EventGroup.RING_ZOOM);

        colorBoostEventHandler = new ColorBoostEventHandler(boostEvents);
    }

    private void loadV4(Difficulty difficulty, JsonObject json) {
        JsonArray events = json.getAsJsonArray("basicEvents");
        JsonArray eventsData = json.getAsJsonArray("basicEventsData");

        var rawBoostEvents = json.getAsJsonArray("colorBoostEvents");
        var rawBoostEventsData = json.getAsJsonArray("colorBoostEventsData");

        var lrlEvents = new ArrayList<LightEventV2>();
        var rrlEvents = new ArrayList<LightEventV2>();
        var lrrEvents = new ArrayList<ValueEvent>();
        var rrrEvents = new ArrayList<ValueEvent>();
        var backEvents = new ArrayList<LightEventV2>();
        var centerEvents = new ArrayList<LightEventV2>();
        var rlEvents = new ArrayList<LightEventV2>();
        var rlsEvents = new ArrayList<ValueEvent>();
        var rlzEvents = new ArrayList<ValueEvent>();
        var boostEvents = new ArrayList<ColorBoostEvent>();
        boostEvents.add(new ColorBoostEvent(0, false));

        rawBoostEvents.forEach(rawEvent -> {
            var obj = rawEvent.getAsJsonObject();
            boostEvents.add(new ColorBoostEvent().loadV4(obj, rawBoostEventsData, difficulty));
        });

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();

            int index = JsonUtil.getOrDefault(obj, "i", JsonElement::getAsInt, 0);

            JsonObject data = eventsData.get(index).getAsJsonObject();

            EventGroup group = EventGroup.fromType(JsonUtil.getOrDefault(data, "t", JsonElement::getAsInt, 0));

            switch (group) {
                case LEFT_LASERS -> lrlEvents.add(new LightEventV2().loadV4(obj, data, difficulty));
                case RIGHT_LASERS -> rrlEvents.add(new LightEventV2().loadV4(obj, data, difficulty));
                case LEFT_ROTATING_LASERS -> lrrEvents.add(new ValueEvent().loadV4(obj, data, difficulty));
                case RIGHT_ROTATING_LASERS -> rrrEvents.add(new ValueEvent().loadV4(obj, data, difficulty));
                case BACK_LASERS -> backEvents.add(new LightEventV2().loadV4(obj, data, difficulty));
                case CENTER_LASERS -> centerEvents.add(new LightEventV2().loadV4(obj, data, difficulty));
                case RING_LIGHTS -> rlEvents.add(new LightEventV2().loadV4(obj, data, difficulty));
                case RING_SPIN -> rlsEvents.add(new ValueEvent().loadV4(obj, data, difficulty));
                case RING_ZOOM -> rlzEvents.add(new ValueEvent().loadV4(obj, data, difficulty));
                case null, default -> {}
            }

        });


        lrlEvents.sort(Difficulty::compareObjects);
        rrlEvents.sort(Difficulty::compareObjects);
        lrrEvents.sort(Difficulty::compareObjects);
        rrrEvents.sort(Difficulty::compareObjects);
        backEvents.sort(Difficulty::compareObjects);
        centerEvents.sort(Difficulty::compareObjects);
        rlEvents.sort(Difficulty::compareObjects);
        rlsEvents.sort(Difficulty::compareObjects);
        rlzEvents.sort(Difficulty::compareObjects);
        boostEvents.sort(Difficulty::compareObjects);

        // these stay as V2 since it's a V2 environment
        leftRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.LEFT_LASERS), lrlEvents);
        rightRotatingLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RIGHT_LASERS), rrlEvents);

        leftRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.LEFT_ROTATING_LASERS), lrrEvents, EventGroup.LEFT_ROTATING_LASERS);
        rightRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RIGHT_ROTATING_LASERS), rrrEvents, EventGroup.RIGHT_ROTATING_LASERS);

        backLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.BACK_LASERS), backEvents);
        centerLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.CENTER_LASERS), centerEvents);

        ringLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RING_LIGHTS), rlEvents);
        ringSpinHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_SPIN), rlsEvents, EventGroup.RING_SPIN);
        ringZoomHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_ZOOM), rlzEvents, EventGroup.RING_ZOOM);

        colorBoostEventHandler = new ColorBoostEventHandler(boostEvents);
    }


    @Override
    public void update(float beat, double deltaTime) {
        super.update(beat, deltaTime);

        leftRotatingLaserLightHandler.update(beat);
        rightRotatingLaserLightHandler.update(beat);
        leftRotatingLaserValueHandler.update(beat);
        rightRotatingLaserValueHandler.update(beat);
        backLaserLightHandler.update(beat);
        centerLaserLightHandler.update(beat);
        ringLightHandler.update(beat);
        ringSpinHandler.update(beat);
        ringZoomHandler.update(beat);

        colorBoostEventHandler.update(beat);

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

    @Override
    public Environment reset() {

        if (leftRotatingLaserLightHandler == null) return this;

        leftRotatingLaserLightHandler.reset();
        rightRotatingLaserLightHandler.reset();
        leftRotatingLaserValueHandler.reset();
        rightRotatingLaserValueHandler.reset();
        backLaserLightHandler.reset();
        centerLaserLightHandler.reset();
        ringLightHandler.reset();
        ringSpinHandler.reset();
        ringZoomHandler.reset();

        colorBoostEventHandler.reset();

        return this;
    }
}
