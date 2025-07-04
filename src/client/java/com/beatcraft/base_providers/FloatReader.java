package com.beatcraft.base_providers;

import org.joml.Quaternionf;

public record FloatReader(float[] values) implements ValueReader<Float> {
    public Float get(Float ignored) {
        return values[0];
    }
    public Float get() {
        return get(0f);
    }
}

