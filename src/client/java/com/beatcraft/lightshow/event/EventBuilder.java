package com.beatcraft.lightshow.event;

import com.beatcraft.animation.Easing;
import com.beatcraft.data.types.Color;
import com.beatcraft.lightshow.event.events.LightEventV3;
import com.beatcraft.lightshow.event.events.RotationEventV3;
import com.beatcraft.lightshow.event.events.TranslationEvent;
import com.beatcraft.lightshow.lights.LightState;
import com.beatcraft.lightshow.lights.TransformState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EventBuilder {

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
        float beat, int group, int groupLightCount, Filter filter,
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
            var out = new ArrayList<RawLightEventV3>();
            for (var targetSet : filter) {
                var targets = targetSet.getA();
                var durationMod = targetSet.getB();
                var distributionMod = targetSet.getC();

                durationMod *= beatDistributionValue;
                if (beatDistributionType == 0) {
                    durationMod *= filter.chunkCount();
                }

                distributionMod *= brightnessDistributionValue;
                if (brightnessDistributionType == 0) {
                    distributionMod *= filter.chunkCount();
                }

                for (var target : targets) {

                    out.add(new RawLightEventV3(
                        beat, beatOffset, group, target,
                        distributionAffectsFirst || !isFirst ? durationMod : 0,
                        brightnessDistributionEasing.apply(1+distributionMod) * brightness,
                        strobeFrequency, strobeBrightness, strobeFade,
                        eventType, color
                    ));
                }

            }
            return out;
        }

    }

    public record BaseRotationData(
        float beat, int group, int groupLightCount, Filter filter,
        int beatDistributionType, float beatDistributionValue,
        int rotationDistributionType, float rotationDistributionValue, Function<Float, Float> rotationEasing,
        TransformState.Axis axis, boolean invertAxis, boolean distributionAffectsFirst
    ) {

        public List<RawRotationEventV3> buildEvents(
            boolean isFirst,
            float beatOffset, int eventType, float magnitude,
            int direction, int loopCount, Function<Float, Float> easing
        ) {
            var out = new ArrayList<RawRotationEventV3>();
            for (var targetSet : filter) {
                var targets = targetSet.getA();
                var durationMod = targetSet.getB();
                var distributionMod = rotationEasing.apply(targetSet.getC());

                durationMod *= beatDistributionValue;
                if (beatDistributionType == 0) {
                    durationMod *= filter.chunkCount();
                }

                distributionMod *= rotationDistributionValue;
                if (rotationDistributionType == 0) {
                    distributionMod *= filter.chunkCount();
                }

                for (var target : targets) {
                    out.add(new RawRotationEventV3(
                        beat, beatOffset, group, target, axis,
                        distributionAffectsFirst || !isFirst ? durationMod : 0,
                        (magnitude + distributionMod) * (invertAxis ? -1 : 1),
                        direction, easing, loopCount, eventType
                    ));
                }

            }
            return out;
        }

    }

    public record BaseTranslationData(
            float beat, int group, int groupLightCount, Filter filter,
            int beatDistributionType, float beatDistributionValue,
            int gapDistributionType, float gapDistributionValue, Function<Float, Float> gapEasing,
            TransformState.Axis axis, boolean invertAxis, boolean distributionAffectsFirst
    ) {
        public List<RawTranslationEvent> buildEvents(
            boolean isFirst,
            float beatOffset, int eventType, float magnitude,
            Function<Float, Float> easing
        ) {
            var out = new ArrayList<RawTranslationEvent>();
            for (var targetSet : filter) {
                var targets = targetSet.getA();
                var durationMod = targetSet.getB();
                var distributionMod = gapEasing.apply(targetSet.getC());

                durationMod *= beatDistributionValue;
                if (beatDistributionType == 0) {
                    durationMod *= filter.chunkCount();
                }

                distributionMod *= gapDistributionValue;
                if (gapDistributionType == 0) {
                    distributionMod *= filter.chunkCount();
                }

                for (var target : targets) {
                    out.add(new RawTranslationEvent(
                        beat, beatOffset, group, target, axis,
                        distributionAffectsFirst || !isFirst ? durationMod : 0,
                        (magnitude + distributionMod) * (invertAxis ? -1 : 1),
                        easing, eventType
                    ));
                }

            }
            return out;
        }
    }

    public record RawLightEventV3(
        float eventBeat, float beatOffset, int group, int lightID,
        float endOffset, float brightness,
        float strobeFrequency, float strobeBrightness,
        boolean strobeFade, int eventType, int color
    ) implements RawEvent {

        @Override
        public float getBeat() {
            return eventBeat;
        }
    }

    public record RawRotationEventV3(
        float eventBeat, float beatOffset, int group, int lightID, TransformState.Axis axis,
        float endOffset, float rotation, int direction, Function<Float, Float> easing,
        int loopCount, int eventType
    ) implements RawEvent {

        @Override
        public float getBeat() {
            return eventBeat;
        }
    }

    public record RawTranslationEvent(
        float eventBeat, float beatOffset, int group, int lightID, TransformState.Axis axis,
        float endOffset, float delta, Function<Float, Float> easing,
        int eventType
    ) implements RawEvent {

        @Override
        public float getBeat() {
            return eventBeat;
        }
    }

    private final ArrayList<RawLightEventV3> rawLightEvents = new ArrayList<>();
    private final ArrayList<RawRotationEventV3> rawRotationEvents = new ArrayList<>();
    private final ArrayList<RawTranslationEvent> rawTranslationEvents = new ArrayList<>();

    private HashMap<GroupKey, ArrayList<LightEventV3>> lightEvents = new HashMap<>();
    private HashMap<GroupKey, HashMap<TransformState.Axis, ArrayList<RotationEventV3>>> rotationEvents = new HashMap<>();
    private HashMap<GroupKey, HashMap<TransformState.Axis, ArrayList<TranslationEvent>>> translationEvents = new HashMap<>();

    public void addRawLightEvents(List<RawLightEventV3> events) {
        rawLightEvents.addAll(events);
    }

    public void addRawRotationEvents(List<RawRotationEventV3> events) {
        rawRotationEvents.addAll(events);
    }

    public void addRawTranslationEvents(List<RawTranslationEvent> events) {
        rawTranslationEvents.addAll(events);
    }

    public List<LightEventV3> getLightEvents(int group, int lightID) {
        return lightEvents.computeIfAbsent(
            new GroupKey(group, lightID),
            k -> new ArrayList<>()
        );
    }

    public HashMap<TransformState.Axis, ArrayList<RotationEventV3>> getRotationEvents(int group, int lightID) {
        return rotationEvents.computeIfAbsent(
            new GroupKey(group, lightID),
            k -> new HashMap<>()
        );
    }

    public HashMap<TransformState.Axis, ArrayList<TranslationEvent>> getTranslationEvents(int group, int lightID) {
        return translationEvents.computeIfAbsent(
            new GroupKey(group, lightID),
            k -> new HashMap<>()
        );
    }

    public List<RawLightEventV3> getRawLightEvents(int group, int lightID) {
        return rawLightEvents
            .stream()
            .filter(e -> e.group == group && e.lightID == lightID)
            .toList();
    }

    public List<RawRotationEventV3> getRawRotationEvents(int group, int lightID, TransformState.Axis axis) {
        return rawRotationEvents
            .stream()
            .filter(e -> e.group == group && e.lightID == lightID && e.axis == axis)
            .toList();
    }

    public List<RawTranslationEvent> getRawTranslationEvents(int group, int lightID, TransformState.Axis axis) {
        return rawTranslationEvents
            .stream()
            .filter(e -> e.group == group && e.lightID == lightID && e.axis == axis)
            .toList();
    }

    private static int rawEventComparator(RawEvent a, RawEvent b) {
        return Float.compare(a.getBeat(), b.getBeat());
    }

    public void sortEvents() {
        rawLightEvents.sort(EventBuilder::rawEventComparator);
        rawRotationEvents.sort(EventBuilder::rawEventComparator);
        rawTranslationEvents.sort(EventBuilder::rawEventComparator);
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
        ls.add(event);
    }

    public void putEvent(int group, int lightID, TransformState.Axis axis, RotationEventV3 event) {
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
                new RotationEventV3(
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

    public void putEvent(int group, int lightID, TransformState.Axis axis, TranslationEvent event) {
        var key = new GroupKey(group, lightID);
        if (!translationEvents.containsKey(key)) {
            translationEvents.put(key, new HashMap<>());
        }
        var axesMap = translationEvents.get(key);
        if (!axesMap.containsKey(axis)) {
            axesMap.put(axis, new ArrayList<>());
        }
        var ls = axesMap.get(axis);
        if (ls.isEmpty()) {
            ls.add(
                new TranslationEvent(
                    0,
                    new TransformState(axis, 0),
                    new TransformState(axis, 0),
                    0, lightID,
                    Easing::easeStep
                )
            );
        }
        ls.add(event);

    }

    public void applyRotationEventBeatCutoff(int group, float beat, Filter filter) {
        var targets = filter.getTargets();
        rawRotationEvents.sort(EventBuilder::rawEventComparator);
        var filtered = rawRotationEvents.stream().filter(e -> {
            if (e.group == group && targets.contains(e.lightID)) {
                return e.eventBeat + e.beatOffset < beat;
            }
            return true;
        }).toList();
        rawRotationEvents.clear();
        rawRotationEvents.addAll(filtered);
    }

    public void applyLightEventBeatCutoff(int group, float beat, Filter filter) {
        var targets = filter.getTargets();
        rawLightEvents.sort(EventBuilder::rawEventComparator);
        var filtered = rawLightEvents.stream().filter(e -> {
            if (e.group == group && targets.contains(e.lightID)) {
                return e.eventBeat + e.beatOffset < beat;
            }
            return true;
        }).toList();
        rawLightEvents.clear();
        rawLightEvents.addAll(filtered);
    }

    public LightEventV3 getLatestLightEvent(int group, int lightID) {
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

    public RotationEventV3 getLatestRotationEvent(int group, int lightID, TransformState.Axis axis) {
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
                new RotationEventV3(
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

    public TranslationEvent getLatestTranslationEvent(int group, int lightID, TransformState.Axis axis) {
        var key = new GroupKey(group, lightID);
        if (!translationEvents.containsKey(key)) {
            translationEvents.put(key, new HashMap<>());
        }
        var axesMap = translationEvents.get(key);
        if (!axesMap.containsKey(axis)) {
            axesMap.put(axis, new ArrayList<>());
        }
        var ls = axesMap.get(axis);
        if (ls.isEmpty()) {
            ls.add(
                new TranslationEvent(
                    0,
                    new TransformState(axis, 0),
                    new TransformState(axis, 0),
                    0, lightID,
                    Easing::easeStep
                )
            );
        }
        return ls.getLast();
    }



}
