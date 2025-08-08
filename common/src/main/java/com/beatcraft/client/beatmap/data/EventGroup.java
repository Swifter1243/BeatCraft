package com.beatcraft.client.beatmap.data;

public enum EventGroup {
    BACK_LASERS,
    RING_LIGHTS,
    LEFT_LASERS,
    RIGHT_LASERS,
    CENTER_LASERS,
    BOOST,
    LEFT_EXTRA,
    RIGHT_EXTRA,
    RING_SPIN,
    RING_ZOOM,
    BILLIE_LEFT,
    BILLIE_RIGHT,
    LEFT_ROTATING_LASERS,
    RIGHT_ROTATING_LASERS,
    EARLY_ROTATION,
    LATE_ROTATION,
    LOWER_HYDRAULICS,
    RAISE_HYDRAULICS,
    GAGA_LEFT,
    GAGA_RIGHT,
    BPM(100);

    // this is so awesome
    private static class CurrentValue {
        private static int value = 0;
        private static int get() {
            return value++;
        }
        private static void set(int value) {
            CurrentValue.value = value;
        }
    }

    private final int value;
    public int getValue() {
        return value;
    }
    EventGroup() {
        value = CurrentValue.get();
    }
    EventGroup(int value) {
        CurrentValue.set(value);
        this.value = value;
    }

    public static EventGroup fromType(int type) {
        for (EventGroup eventGroup : EventGroup.values()) {
            if (type == eventGroup.getValue()) {
                return eventGroup;
            }
        }

        return null;
    }
}
