package com.beatcraft.lightshow.event;

import com.beatcraft.animation.Easing;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.lights.TransformState;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EventBuilderV3 {

    public static class GroupKey {
        private int group;
        private int id;

        public int getLightId() {
            return id;
        }

        public int getGroup() {
            return group;
        }

        public GroupKey(int group, int id) {
            this.group = group;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GroupKey other)) return false;
            return group == other.group && id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, id);
        }
    }

    private interface RawEvent {
        float getBeat();
    }

    public record BaseLightData(
        float beat, int group, Filter filter,
        int beatDistributionType, float beatDistributionValue,
        int brightnessDistributionType, float brightnessDistributionValue,
        Function<Float, Float> brightnessDistributionEasing, boolean distributionAffectsFirst
    ) {

        public List<RawLightEventV3> buildEvents(
            boolean isFirst,
            float beatOffset, int eventType, int color,
            float brightness, float strobeFrequency,
            float strobeBrightness, boolean strobeFade
        ) {

        }

    }

    public record BaseRotationData(
        float beat, int group, Filter filter,
        int beatDistributionType, float beatDistributionValue,
        int rotationDistributionType, float rotationDistributionValue,
        TransformState.Axis axis, boolean invertAxis, boolean distributionAffectsFirst
    ) {

        public List<RawRotationEventV3> buildEvents(
            boolean isFirst,
            float beatOffset, int eventType, float magnitude,
            float direction, float loopCount, Function<Float, Float> easing
        ) {

        }

    }

    public record RawLightEventV3(
        float eventBeat, int group, int lightID,
        float startOffset, float endOffset, float brightness,
        float strobeFrequency, float strobeBrightness,
        boolean strobeFade, int eventType, int color
    ) implements RawEvent {

        @Override
        public float getBeat() {
            return eventBeat;
        }
    }

    public record RawRotationEventV3(
        float eventBeat, int group, int lightID, TransformState.Axis axis,
        float startOffset, float endOffset, float rotation, int direction, int easing,
        int loopCount, int eventType
    ) implements RawEvent {

        @Override
        public float getBeat() {
            return 0;
        }
    }

    public void addRawLightEvents(List<RawLightEventV3> events) {
        rawLightEvents.addAll(events);
    }
    public void addRawLightEvent(RawLightEventV3 event) {
        rawLightEvents.add(event);
    }

    public void addRawRotationEvents(List<RawRotationEventV3> events) {
        rawRotationEvents.addAll(events);
    }
    public void addRawRotationEvent(RawRotationEventV3 event) {
        rawRotationEvents.add(event);
    }

    private final ArrayList<RawLightEventV3> rawLightEvents = new ArrayList<>();
    private final ArrayList<RawRotationEventV3> rawRotationEvents = new ArrayList<>();

    private HashMap<GroupKey, HashMap<TransformState.Axis, ArrayList<RotationEvent>>> rotationEvents = new HashMap<>();
    private HashMap<GroupKey, ArrayList<LightEventV3>> lightEvents = new HashMap<>();

    private static int rawEventComparator(RawEvent a, RawEvent b) {
        return Float.compare(a.getBeat(), b.getBeat());
    }

    public void sortEvents() {
        rawLightEvents.sort(EventBuilderV3::rawEventComparator);
        rawRotationEvents.sort(EventBuilderV3::rawEventComparator);
    }

    public void putEvent(int group, int lightID, TransformState.Axis axis, RotationEvent event) {
        var key = new GroupKey(group, lightID);
        if (!rotationEvents.containsKey(key)) {
            rotationEvents.put(key, new HashMap<>());
        }
        var axesMap = rotationEvents.get(key);
        if (!axesMap.containsKey(axis)) {
            axesMap.put(axis, new ArrayList<>());
        }
        var ls = axesMap.get(axis);
        if (ls.isEmpty()) {
            ls.add(
                new RotationEvent(
                    0,
                    new TransformState(axis, 0),
                    new TransformState(axis, 0),
                    0, lightID,
                    Easing::easeStep,
                    0, 0
                )
            );
        }
        ls.add(event);

    }

    public void putEvent(int group, int lightID, LightEventV3 event) {
        var key = new GroupKey(group, lightID);
        if (!lightEvents.containsKey(key)) {
            lightEvents.put(key, new ArrayList<>());
        }
        var ls = lightEvents.get(key);
        if (ls.isEmpty()) {
            ls.add(
                new LightEventV3(
                    0,
                    new LightState(new Color(0), 0),
                    new LightState(new Color(0), 0),
                    0, lightID
                )
            );
        }
    }

    public RotationEvent getLatestRotationEvent(int group, int lightID, TransformState.Axis axis) {
        var key = new GroupKey(group, lightID);
        if (!rotationEvents.containsKey(key)) {
            rotationEvents.put(key, new HashMap<>());
        }
        var axesMap = rotationEvents.get(key);
        if (!axesMap.containsKey(axis)) {
            axesMap.put(axis, new ArrayList<>());
        }
        var ls = axesMap.get(axis);
        if (ls.isEmpty()) {
            ls.add(
                new RotationEvent(
                    0,
                    new TransformState(axis, 0),
                    new TransformState(axis, 0),
                    0, lightID,
                    Easing::easeStep,
                    0, 0
                )
            );
        }
        return ls.getLast();
    }

    public LightEventV3 getLatestLightEvent(int group, int lightID, TransformState.Axis axis) {
        var key = new GroupKey(group, lightID);
        if (!lightEvents.containsKey(key)) {
            lightEvents.put(key, new ArrayList<>());
        }
        var ls = lightEvents.get(key);
        if (ls.isEmpty()) {
            ls.add(
                new LightEventV3(
                    0,
                    new LightState(new Color(0), 0),
                    new LightState(new Color(0), 0),
                    0, lightID
                )
            );
        }
        return ls.getLast();
    }

}
