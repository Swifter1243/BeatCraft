package com.beatcraft.lightshow.environment;


import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.data.EventGroup;
import com.beatcraft.lightshow.environment.lightgroup.ActionLightGroupV2;
import com.beatcraft.lightshow.environment.lightgroup.LightGroupV2;
import com.beatcraft.lightshow.event.events.LightEventV2;
import com.beatcraft.lightshow.event.events.ValueEvent;
import com.beatcraft.lightshow.event.handlers.ActionEventHandlerV2;
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
        ArrayList<LightEventV2> lrlEvents = new ArrayList<>();
        ArrayList<LightEventV2> rrlEvents = new ArrayList<>();
        ArrayList<ValueEvent> lrrEvents = new ArrayList<>();
        ArrayList<ValueEvent> rrrEvents = new ArrayList<>();
        ArrayList<LightEventV2> backEvents = new ArrayList<>();
        ArrayList<LightEventV2> centerEvents = new ArrayList<>();
        ArrayList<LightEventV2> rlEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlsEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlzEvents = new ArrayList<>();

        events.forEach(o -> {
            JsonObject obj = o.getAsJsonObject();
            EventGroup group = EventGroup.fromType(obj.get("_type").getAsInt());

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

        leftRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.LEFT_ROTATING_LASERS), lrrEvents, EventGroup.LEFT_ROTATING_LASERS);
        rightRotatingLaserValueHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RIGHT_ROTATING_LASERS), rrrEvents, EventGroup.RIGHT_ROTATING_LASERS);

        backLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.BACK_LASERS), backEvents);
        centerLaserLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.CENTER_LASERS), centerEvents);

        ringLightHandler = new LightGroupEventHandlerV2(lightGroups.get(EventGroup.RING_LIGHTS), rlEvents);
        ringSpinHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_SPIN), rlsEvents, EventGroup.RING_SPIN);
        ringZoomHandler = new ActionEventHandlerV2((ActionLightGroupV2) lightGroups.get(EventGroup.RING_ZOOM), rlzEvents, EventGroup.RING_ZOOM);

    }

    private void loadV3(Difficulty difficulty, JsonObject json) {
        JsonArray events = json.getAsJsonArray("basicBeatmapEvents");
        ArrayList<LightEventV2> lrlEvents = new ArrayList<>();
        ArrayList<LightEventV2> rrlEvents = new ArrayList<>();
        ArrayList<ValueEvent> lrrEvents = new ArrayList<>();
        ArrayList<ValueEvent> rrrEvents = new ArrayList<>();
        ArrayList<LightEventV2> backEvents = new ArrayList<>();
        ArrayList<LightEventV2> centerEvents = new ArrayList<>();
        ArrayList<LightEventV2> rlEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlsEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlzEvents = new ArrayList<>();

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

        lrlEvents.sort(difficulty::compareObjects);
        rrlEvents.sort(difficulty::compareObjects);
        lrrEvents.sort(difficulty::compareObjects);
        rrrEvents.sort(difficulty::compareObjects);
        backEvents.sort(difficulty::compareObjects);
        centerEvents.sort(difficulty::compareObjects);
        rlEvents.sort(difficulty::compareObjects);
        rlsEvents.sort(difficulty::compareObjects);
        rlzEvents.sort(difficulty::compareObjects);

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

    }

    private void loadV4(Difficulty difficulty, JsonObject json) {
        JsonArray events = json.getAsJsonArray("basicEvents");
        JsonArray eventsData = json.getAsJsonArray("basicEventsData");

        ArrayList<LightEventV2> lrlEvents = new ArrayList<>();
        ArrayList<LightEventV2> rrlEvents = new ArrayList<>();
        ArrayList<ValueEvent> lrrEvents = new ArrayList<>();
        ArrayList<ValueEvent> rrrEvents = new ArrayList<>();
        ArrayList<LightEventV2> backEvents = new ArrayList<>();
        ArrayList<LightEventV2> centerEvents = new ArrayList<>();
        ArrayList<LightEventV2> rlEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlsEvents = new ArrayList<>();
        ArrayList<ValueEvent> rlzEvents = new ArrayList<>();

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


        lrlEvents.sort(difficulty::compareObjects);
        rrlEvents.sort(difficulty::compareObjects);
        lrrEvents.sort(difficulty::compareObjects);
        rrrEvents.sort(difficulty::compareObjects);
        backEvents.sort(difficulty::compareObjects);
        centerEvents.sort(difficulty::compareObjects);
        rlEvents.sort(difficulty::compareObjects);
        rlsEvents.sort(difficulty::compareObjects);
        rlzEvents.sort(difficulty::compareObjects);

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

        return this;
    }
}
